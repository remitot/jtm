package org.jepria.ahttpd.manager.web.modjk;

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

import org.jepria.ahttpd.manager.core.modjk.AhttpdConfModjk;
import org.jepria.ahttpd.manager.core.modjk.Binding;
import org.jepria.ahttpd.manager.web.Environment;
import org.jepria.ahttpd.manager.web.EnvironmentFactory;
import org.jepria.ahttpd.manager.web.modjk.dto.ModRequestBodyDto;
import org.jepria.ahttpd.manager.web.modjk.dto.ModRequestDto;
import org.jepria.ahttpd.manager.web.modjk.dto.ModjkDto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ModjkApiServlet extends HttpServlet {

  private static final long serialVersionUID = -3831454096594936484L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    resp.setContentType("application/json; charset=UTF-8");
    
    String path = req.getPathInfo();
    
    if ("/list".equals(path)) {
      
      try {
        
        Environment environment = EnvironmentFactory.get(req);
        
        AhttpdConfModjk ahttpdConf = new AhttpdConfModjk(environment.getModjkConfInputStream(), 
            environment.getWorkerPropertiesInputStream());
        
        List<ModjkDto> bindings = getBindings(ahttpdConf);

        Map<String, Object> responseJsonMap = new HashMap<>();
        responseJsonMap.put("_list", bindings);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(responseJsonMap, new PrintStream(resp.getOutputStream()));
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
        return;
        
      } catch (Throwable e) {
        e.printStackTrace();

        resp.getOutputStream().println("Oops! Something went wrong.");//TODO
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.flushBuffer();
        return;
      }

    } else {
      
      // TODO set content type for the error case?
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
  
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String path = req.getPathInfo();
    
    if ("/mod".equals(path)) {
      mod(req, resp);
      return;
      
    } else {
      
      // TODO set content type for the error case?
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
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
      
      final AhttpdConfModjk ahttpdConf = new AhttpdConfModjk(environment.getModjkConfInputStream(), 
          environment.getWorkerPropertiesInputStream());

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

          ModStatus modStatus = updateBinding(mreq, ahttpdConf);
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

          ModStatus modStatus = deleteBinding(mreq, ahttpdConf);
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

          ModStatus modStatus = createBinding(mreq, ahttpdConf);
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
        ahttpdConf.save(environment.getModjkConfOutputStream(), environment.getWorkerPropertiesOutputStream());
      }
      
      
      // no conf save, just write response
      List<ModjkDto> bindingDtos = getBindings(ahttpdConf);
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
      ModRequestBodyDto mreq, AhttpdConfModjk ahttpdConf) {
    
    ModStatus ret;
    
    try {
      String location = mreq.getLocation();

      if (location == null) {

        ret = ModStatus.errLocationIsEmpty();
            
      } else {

        Map<String, Binding> bindings = ahttpdConf.getBindings();
        Binding binding = bindings.get(location);

        if (binding == null) {

          ret = ModStatus.errItemNotFoundByLocation(location);

        } else {
          ModjkDto bindingDto = mreq.getData();

          if (bindingDto.getActive() != null) {
            if (!binding.isActive() && bindingDto.getActive()) {
              binding.onActivate();
            } else if (binding.isActive() && !bindingDto.getActive()) {
              binding.onDeactivate();
            }
          }
          
          if (bindingDto.getAppname() != null) {
            binding.setAppname(bindingDto.getAppname());
          }
          if (bindingDto.getInstance() != null) {
            binding.setInstance(bindingDto.getInstance());
          }

          ret = ModStatus.success();
          
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      ret = ModStatus.errInternalError();
    }
    
    return ret;
  }
  
  private static ModStatus deleteBinding(
      ModRequestBodyDto mreq, AhttpdConfModjk ahttpdConf) {
    
    ModStatus ret;

    try {
      String location = mreq.getLocation();

      if (location == null) {

        ret = ModStatus.errLocationIsEmpty();

      } else {

        Map<String, Binding> bindings = ahttpdConf.getBindings();
        Binding binding = bindings.get(location);

        if (binding == null) {

          ret = ModStatus.errItemNotFoundByLocation(location);

        } else {
          ahttpdConf.delete(location);

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
      ModRequestBodyDto mreq, AhttpdConfModjk ahttpdConf) {
    
    ModStatus ret;

    try {
      ModjkDto bindingDto = mreq.getData();

      // check mandatory fields of a new connection
      List<String> emptyFields = getEmptyMandatoryFields(bindingDto);

      if (!emptyFields.isEmpty()) {

        ret = ModStatus.errMandatoryFieldsEmpty(emptyFields);

      } else {

        Binding newBinding = ahttpdConf.create();

        newBinding.setAppname(bindingDto.getAppname());
        newBinding.setInstance(bindingDto.getInstance());

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
  private static List<String> getEmptyMandatoryFields(ModjkDto bindingDto) {
    List<String> emptyFields = new ArrayList<>();

    if (bindingDto.getAppname() == null) {
      emptyFields.add("appname");
    }
    if (bindingDto.getInstance() == null) {
      emptyFields.add("instance");
    }

    return emptyFields;
  }
  
  private static List<ModjkDto> getBindings(AhttpdConfModjk ahttpdConf) {
    Map<String, Binding> bindings = ahttpdConf.getBindings();

    // list all bindings
    return bindings.entrySet().stream().map(
        entry -> bindingToDto(entry.getKey(), entry.getValue()))
        .sorted(bindingSorter()).collect(Collectors.toList());
  }
  
  private static ModjkDto bindingToDto(String location, Binding binding) {
    ModjkDto dto = new ModjkDto();
    dto.setActive(binding.isActive());
    dto.setLocation(location);
    dto.setAppname(binding.getAppname());
    dto.setInstance(binding.getInstance());
    return dto;
  }
  
  private static Comparator<ModjkDto> bindingSorter() {
    return new Comparator<ModjkDto>() {
      @Override
      public int compare(ModjkDto o1, ModjkDto o2) {
        int appnameCmp = o1.getAppname().compareTo(o2.getAppname());
        if (appnameCmp == 0) {
          // the active is the first
          if (o1.getActive() && !o2.getActive()) {
            return -1;
          } else if (o2.getActive() && !o1.getActive()) {
            return 1;
          } else {
            return 0;
          }
        } else {
          return appnameCmp;
        }
      }
    };
  }
}
