package org.jepria.httpd.apache.manager.web.jk;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.core.jk.ApacheConfJk;
import org.jepria.httpd.apache.manager.core.jk.Binding;
import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.httpd.apache.manager.web.ajp.SimpleAjpConnection;
import org.jepria.httpd.apache.manager.web.jk.dto.JkDto;
import org.jepria.httpd.apache.manager.web.jk.dto.ModRequestBodyDto;
import org.jepria.httpd.apache.manager.web.jk.dto.ModRequestDto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JkApiServlet extends HttpServlet {

  private static final long serialVersionUID = -3831454096594936484L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    String path = request.getPathInfo();
    
    if ("/list".equals(path)) {
      list(request, response);
      return;

    } else if ("/get-http-port".equals(path)) {
      getHttpPort(request, response);
      return;
      
    } else {
      
      // TODO set content type for the error case?
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      response.flushBuffer();
      return;
    }
  }
  
  private static void list(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    
    // the content type is defined for the entire method
    response.setContentType("application/json; charset=UTF-8");
    
    try {
      
      Environment environment = EnvironmentFactory.get(request);
      
      ApacheConfJk apacheConf = new ApacheConfJk(
          () -> environment.getMod_jk_confInputStream(), 
          () -> environment.getWorkers_propertiesInputStream());
      
      List<JkDto> bindings = getBindings(apacheConf);
      
      Map<String, Object> responseJsonMap = new HashMap<>();
      responseJsonMap.put("_list", bindings);
      
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(responseJsonMap, new PrintStream(response.getOutputStream()));
      
      response.setStatus(HttpServletResponse.SC_OK);
      response.flushBuffer();
      return;
      
    } catch (Throwable e) {
      e.printStackTrace();

      response.getOutputStream().println("Oops! Something went wrong.");//TODO
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.flushBuffer();
      return;
    }
  }
  
  private static void getHttpPort(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    
    // the content type is defined for the entire method
    response.setContentType("text/plain; charset=UTF-8");

    String host = request.getParameter("host");
    String ajpPort = request.getParameter("ajp-port");
    
    if (host == null || ajpPort == null || !ajpPort.matches("\\d+")) {
      // TODO set content type for the error case?
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      response.flushBuffer();
      return;
    }
    
    int ajpPortNumber = Integer.parseInt(ajpPort);
    
    try {
      SimpleAjpConnection connection = SimpleAjpConnection.open(
          host, ajpPortNumber, "/manager-ext/api/port/http", 2000);
      
      connection.connect();
      
      int status = connection.getStatus();
      
      if (status == 200) {
        String httpPort = connection.getResponseBody();
        
        response.getWriter().print(httpPort);
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
        return;
        
      } else {
        
        response.setStatus(status);
        response.flushBuffer();
        return;
      }
      
    } catch (Throwable e) {
      // access to a protected resource will result java.net.SocketTimeoutException,
      
      // log but not rethrow
      e.printStackTrace();
      
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.flushBuffer();
      return;
    }
  }
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String path = request.getPathInfo();
    
    if ("/mod".equals(path)) {
      mod(request, response);
      return;
      
    } else {
      
      // TODO set content type for the error case?
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      response.flushBuffer();
      return;
    }
  }
  
  private static void mod(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("application/json; charset=UTF-8");

    try {
      
      // read list from request body
      final List<ModRequestDto> modRequests;
      
      try {
        Type mapType = new TypeToken<ArrayList<ModRequestDto>>(){}.getType();
        modRequests = new Gson().fromJson(new InputStreamReader(req.getInputStream()), mapType);
        
      } catch (Throwable e) {
        e.printStackTrace();

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        resp.flushBuffer();
        return;
      } 
      
      
      // convert list to map
      final Map<String, ModRequestBodyDto> modRequestBodyMap = new HashMap<>();
      
      if (modRequests != null) {
        for (ModRequestDto modRequest: modRequests) {
          final String modRequestId = modRequest.getModRequestId();
          
          if (modRequestId == null || "".equals(modRequestId)
              || modRequestBodyMap.put(modRequestId, modRequest.getModRequestBody()) != null) {
            // duplicate or empty modRequestId values
            
            // TODO log?
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            resp.flushBuffer();
            return;
          }
        }
      }
      

      
      final Environment environment = EnvironmentFactory.get(req);
      
      final ApacheConfJk apacheConf = new ApacheConfJk(
          () -> environment.getMod_jk_confInputStream(), 
          () -> environment.getWorkers_propertiesInputStream());

      // collect processed modRequests
      final Set<String> processedModRequestIds = new HashSet<>();
      
      
      // response map
      final Map<String, ModStatus> modStatusMap = new HashMap<>();
      
      
      boolean confModified = false; 
      
      // 1) perform all updates
      for (String modRequestId: modRequestBodyMap.keySet()) {
        ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);

        if ("update".equals(mreq.getAction())) {
          processedModRequestIds.add(modRequestId);

          ModStatus modStatus = updateBinding(mreq, apacheConf);
          if (modStatus.code == ModStatus.CODE_SUCCESS) {
            confModified = true;
          }

          modStatusMap.put(modRequestId, modStatus);
        }
      }


      // 2) perform all deletions
      for (String modRequestId: modRequestBodyMap.keySet()) {
        ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);

        if ("delete".equals(mreq.getAction())) {
          processedModRequestIds.add(modRequestId);

          ModStatus modStatus = deleteBinding(mreq, apacheConf);
          if (modStatus.code == ModStatus.CODE_SUCCESS) {
            confModified = true;
          }
          
          modStatusMap.put(modRequestId, modStatus);
        }
      }


      // 3) perform all creations
      for (String modRequestId: modRequestBodyMap.keySet()) {
        ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);

        if ("create".equals(mreq.getAction())) {
          processedModRequestIds.add(modRequestId);

          ModStatus modStatus = createBinding(mreq, apacheConf);
          if (modStatus.code == ModStatus.CODE_SUCCESS) {
            confModified = true;
          }
          
          modStatusMap.put(modRequestId, modStatus);
        }
      }



      // 4) process illegal actions
      for (String modRequestId: modRequestBodyMap.keySet()) {
        if (!processedModRequestIds.contains(modRequestId)) {
          
          String action = modRequestBodyMap.get(modRequestId).getAction();
          ModStatus modStatus = ModStatus.errIllegalAction(action);
          
          modStatusMap.put(modRequestId, modStatus);
        }
      }


      // prepare response map
      final Map<String, Object> responseJsonMap = new HashMap<>();
      
      // convert map to list
      final List<Map<String, Object>> modStatusList = modStatusMap.entrySet().stream().map(
          entry -> {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("modRequestId", entry.getKey());
            jsonMap.put("modStatusCode", entry.getValue().code);
            // TODO maybe to check some URL parameter (such as 'verbose=1') and to put or not to put modStatusMessages into a response?
            jsonMap.put("modStatusMessage", entry.getValue().message);
            return jsonMap;
          }).collect(Collectors.toList());
      
      responseJsonMap.put("modStatusList", modStatusList);
      
      
      if (confModified) {
        apacheConf.save(environment.getMod_jk_confOutputStream(), 
            environment.getWorkers_propertiesOutputStream());
      }
      
      
      // no conf save, just write response
      List<JkDto> bindingDtos = getBindings(apacheConf);
      responseJsonMap.put("_list", bindingDtos);
      
      try (OutputStreamWriter osw = new OutputStreamWriter(resp.getOutputStream(), "UTF-8")) {
        new Gson().toJson(responseJsonMap, osw);
      }
      
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.flushBuffer();
      
      return;
      
    } catch (Throwable e) {
      e.printStackTrace();

      // response body must either be empty or match the declared content type
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }
  }
  
  private static ModStatus updateBinding(
      ModRequestBodyDto mreq, ApacheConfJk apacheConf) {
    
    ModStatus ret;
    
    try {
      String location = mreq.getLocation();

      if (location == null) {

        ret = ModStatus.errLocationIsEmpty();
            
      } else {

        Map<String, Binding> bindings = apacheConf.getBindings();
        Binding binding = bindings.get(location);

        if (binding == null) {

          ret = ModStatus.errItemNotFoundByLocation(location);

        } else {
          JkDto bindingDto = mreq.getData();

          ret = updateFields(bindingDto, binding);
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      ret = ModStatus.errInternalError();
    }
    
    return ret;
  }
  
  /**
   * Updates target's fields with source's values
   * @param sourceDto
   * @param target non null
   * @return
   */
  private static ModStatus updateFields(JkDto sourceDto, Binding target) {
    if (sourceDto.getActive() != null) {
      target.setActive(sourceDto.getActive());
    }
    if (sourceDto.getApplication() != null) {
      target.setApplication(sourceDto.getApplication());
    }
    if (sourceDto.getHost() != null) {
      // TODO convert from http to ajp
      target.setWorkerHost(sourceDto.getHost());
    }
    if (sourceDto.getHttpPort() != null) {
      // TODO convert from http to ajp
      int ajpPort = Integer.parseInt(sourceDto.getHttpPort()); 
      target.setWorkerAjpPort(ajpPort);
    }
    return ModStatus.success();
  }
  
  private static ModStatus deleteBinding(
      ModRequestBodyDto mreq, ApacheConfJk apacheConf) {
    
    ModStatus ret;

    try {
      String location = mreq.getLocation();

      if (location == null) {

        ret = ModStatus.errLocationIsEmpty();

      } else {

        Map<String, Binding> bindings = apacheConf.getBindings();
        Binding binding = bindings.get(location);

        if (binding == null) {

          ret = ModStatus.errItemNotFoundByLocation(location);

        } else {
          apacheConf.delete(location);

          ret = ModStatus.success();
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      ret = ModStatus.errInternalError();
    }
    
    return ret;
  }
  
  private static ModStatus createBinding(
      ModRequestBodyDto mreq, ApacheConfJk apacheConf) {
    
    ModStatus ret;

    try {
      JkDto bindingDto = mreq.getData();

      // check mandatory fields of a new connection
      List<String> emptyFields = getEmptyMandatoryFields(bindingDto);

      if (!emptyFields.isEmpty()) {

        ret = ModStatus.errMandatoryFieldsEmpty(emptyFields);

      } else {

        Binding newBinding = apacheConf.create();

        updateFields(bindingDto, newBinding);

        ret = ModStatus.success();
      }
    } catch (Throwable e) {
      e.printStackTrace();
      ret = ModStatus.errInternalError();
    }
    
    return ret;
  }
  
  /**
   * 
   * @param connection
   * @return or empty list
   */
  private static List<String> getEmptyMandatoryFields(JkDto bindingDto) {
    List<String> emptyFields = new ArrayList<>();

    if (bindingDto.getApplication() == null) {
      emptyFields.add("application");
    }
    if (bindingDto.getHost() == null) {
      emptyFields.add("host");
    }
    if (bindingDto.getHttpPort() == null) {
      emptyFields.add("httpPort");
    }

    return emptyFields;
  }
  
  private static List<JkDto> getBindings(ApacheConfJk apacheConf) {
    Map<String, Binding> bindings = apacheConf.getBindings();

    // list all bindings
    return bindings.entrySet().stream().map(
        entry -> bindingToDto(entry.getKey(), entry.getValue()))
        .sorted(bindingSorter()).collect(Collectors.toList());
  }
  
  private static JkDto bindingToDto(String location, Binding binding) {
    JkDto dto = new JkDto();
    dto.setActive(binding.isActive());
    dto.setLocation(location);
    dto.setApplication(binding.getApplication());
    String host = binding.getWorkerHost(); 
    dto.setHost(host);
    String ajpPort = Integer.toString(binding.getWorkerAjpPort());
    dto.setAjpPort(ajpPort);
    dto.setGetHttpPortLink("api/jk/get-http-port?host=" + host + "&ajp-port=" + ajpPort);
    return dto;
  }
  
  private static Comparator<JkDto> bindingSorter() {
    return new Comparator<JkDto>() {
      @Override
      public int compare(JkDto o1, JkDto o2) {
        int applicationCmp = o1.getApplication().toLowerCase().compareTo(o2.getApplication().toLowerCase());
        if (applicationCmp == 0) {
          // the active is the first
          if (o1.getActive() && !o2.getActive()) {
            return -1;
          } else if (o2.getActive() && !o1.getActive()) {
            return 1;
          } else {
            return 0;
          }
        } else {
          return applicationCmp;
        }
      }
    };
  }
}
