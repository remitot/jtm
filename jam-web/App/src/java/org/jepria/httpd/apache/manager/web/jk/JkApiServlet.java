package org.jepria.httpd.apache.manager.web.jk;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.jepria.httpd.apache.manager.web.jk.dto.AjpRequestDto;
import org.jepria.httpd.apache.manager.web.jk.dto.AjpResponseDto;
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
      
      final Environment environment = EnvironmentFactory.get(request);
      
      final ApacheConfJk apacheConf = new ApacheConfJk(environment);
      
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
    response.setContentType("application/json; charset=UTF-8");

    String host = request.getParameter("host");
    String ajpPort = request.getParameter("ajp-port");
    
    if (host == null || ajpPort == null || !ajpPort.matches("\\d+")) {
      // TODO set content type for the error case?
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      response.flushBuffer();
      return;
    }
    
    
    final int ajpPortNumber;
    try {
      ajpPortNumber = Integer.parseInt(ajpPort);
    } catch (NumberFormatException e) {
      // impossible
      throw e;
    }
    final String uri = "/manager-ext/api/port/http";// TODO extract
    
    
    
    
    final AjpRequestDto ajpRequest = new AjpRequestDto();
    ajpRequest.setHost(host);
    ajpRequest.setPort(ajpPortNumber);
    ajpRequest.setUri(uri);
    
    final AjpResponseDto ajpResponse = new AjpResponseDto();
    
    try {
      Response subresponse = subrequestHttpPortByAjp(host, ajpPortNumber, uri);
      
      ajpResponse.setStatus(subresponse.status);
      ajpResponse.setStatusMessage(subresponse.statusMessage);
      ajpResponse.setResponseBody(subresponse.responseBody);
      
    } catch (SubrequestException e) {
      e.printStackTrace();
      
      ajpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      ajpResponse.setStatusMessage(e.getClass().getName() + ": " + e.getMessage());
    }
      
    Map<String, Object> responseJsonMap = new HashMap<>();
    responseJsonMap.put("ajpRequest", ajpRequest);
    responseJsonMap.put("ajpResponse", ajpResponse);
    
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    gson.toJson(responseJsonMap, new PrintStream(response.getOutputStream()));
  
    response.setStatus(HttpServletResponse.SC_OK);
    response.flushBuffer();
    return;
      
  }
  
  private static class Response {
    public static final String SM_UNKNOWN_HOST = "UNKNOWN_HOST";
    public static final String SM_CONNECT_EXCEPTION = "CONNECT_EXCEPTION";
    public static final String SM_SOCKET_EXCEPTION = "SOCKET_EXCEPTION";
    public static final String SM_CONNECT_TIMEOUT = "CONNECT_TIMEOUT";
    
    public final int status;
    public final String statusMessage;
    public final String responseBody;
    
    public Response(int status, String statusMessage, String responseBody) {
      this.status = status;
      this.statusMessage = statusMessage;
      this.responseBody = responseBody;
    }
  }
  
  private static class SubrequestException extends Exception {
    private static final long serialVersionUID = 1177260562719083892L;
    public SubrequestException(String message, Throwable cause) {
      super(message, cause);
    }
  }
  
  /**
   * 
   * @param host
   * @param ajpPort
   * @param uri begins with '/'
   * @return
   * @throws SubrequestException
   */
  private static Response subrequestHttpPortByAjp(String host, int ajpPort, String uri) throws SubrequestException {
    
    if (false) {return new Response(200, null, ""+(ajpPort + 1000));}
    
    try {
      SimpleAjpConnection connection = SimpleAjpConnection.open(
          host, ajpPort, uri, CONNECT_TIMEOUT_MS);
      
      connection.connect();
      
      return new Response(
          connection.getStatus(), 
          connection.getStatusMessage(), 
          connection.getResponseBody());
      
    } catch (SocketTimeoutException e) {
      // non-authorized request to a protected resource will result java.net.SocketTimeoutException
      return new Response(
          HttpServletResponse.SC_GATEWAY_TIMEOUT,
          null,
          null);
      
    } catch (Throwable e) {
      throw new SubrequestException("Failed to make subrequest to [" + host + ":" + ajpPort + uri + "]", e);
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
      
      final ApacheConfJk apacheConf = new ApacheConfJk(environment);

      // collect processed modRequests
      final Set<String> processedModRequestIds = new HashSet<>();
      
      
      // response map
      final Map<String, ModStatus> modStatusMap = new HashMap<>();
      
      // all modifications succeeded
      boolean allModSuccess = true; 
      
      // 1) perform all updates
      for (String modRequestId: modRequestBodyMap.keySet()) {
        ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);

        if ("update".equals(mreq.getAction())) {
          processedModRequestIds.add(modRequestId);

          ModStatus modStatus = updateBinding(mreq, apacheConf);
          if (modStatus.code != ModStatus.SC_SUCCESS) {
            allModSuccess = false;
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
          if (modStatus.code != ModStatus.SC_SUCCESS) {
            allModSuccess = false;
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
          if (modStatus.code != ModStatus.SC_SUCCESS) {
            allModSuccess = false;
          }
          
          modStatusMap.put(modRequestId, modStatus);
        }
      }


      // 4) ignore illegal actions


      // prepare response map
      final Map<String, Object> responseJsonMap = new HashMap<>();
      
      // convert map to list of JSON objects
      List<Map<String, Object>> modStatusList = new ArrayList<>();
      for (Map.Entry<String, ModStatus> entry: modStatusMap.entrySet()) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("modRequestId", entry.getKey());
        int code = entry.getValue().code;
        jsonMap.put("modStatusCode", code);
        if (code == ModStatus.SC_INVALID_FIELD_DATA) {
          jsonMap.put("invalidFieldData", entry.getValue().invalidFieldDataMap);
        }
        modStatusList.add(jsonMap);
      }
      
      responseJsonMap.put("modStatusList", modStatusList);
      
      
      if (allModSuccess) {
        // save modifications
        
        apacheConf.save(environment.getMod_jk_confOutputStream(), 
            environment.getWorkers_propertiesOutputStream());
        
        
        // add the new list to the response
        
        final ApacheConfJk apacheConfAfterSave = new ApacheConfJk(environment);
        
        
        final List<JkDto> bindingsAfterSave = getBindings(apacheConfAfterSave);
        responseJsonMap.put("_list", bindingsAfterSave);
      }
      
      
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
    
    try {
      String location = mreq.getLocation();

      if (location == null) {
        return ModStatus.errLocationIsEmpty();
      }

      Map<String, Binding> bindings = apacheConf.getBindings();
      Binding binding = bindings.get(location);

      if (binding == null) {
        return ModStatus.errItemNotFoundByLocation();
      }
      
      JkDto bindingDto = mreq.getData();
      
      
      // validate 'instance' field value
      if (!validateInstanceFieldValue(bindingDto)) {
        return ModStatus.errInvalidFieldData("instance", "INVALID", null);
      }
      

      return updateFields(bindingDto, binding);
      
    } catch (Throwable e) {
      e.printStackTrace();
      
      return ModStatus.errServerException();
    }
  }
  
  /**
   * Updates target's fields with source's values
   * @param sourceDto already valid
   * @param target non null
   * @return
   */
  private static ModStatus updateFields(JkDto sourceDto, Binding target) {

    // rebinding comes first
    
    if (sourceDto.getInstance() != null) {
      final InstanceValueParser.ParseResult parseResult = InstanceValueParser.tryParse(sourceDto.getInstance());
      
      final String host = parseResult.host;
      final int httpPortNumber = parseResult.port;
      final String uri = "/manager-ext/api/port/ajp";// TODO parametrize 'manager-ext'
      
      
      final int ajpPortNumber;
      try {
        Response response = subrequestAjpPortByHttp(host, httpPortNumber, uri);
      
        if (response.status == 200) {
          ajpPortNumber = Integer.parseInt(response.responseBody);
          
        } else {
          final String errorMessage = "Failed to make request to http://" + host + ":" + httpPortNumber + uri + ": " + response.status;
          
          if (response.status == 400 
              && Response.SM_UNKNOWN_HOST.equals(response.statusMessage)) {
            return ModStatus.errInvalidFieldData("instance", "UNKNOWN_HOST", errorMessage);
            
          } else if (response.status == 400 
              && Response.SM_CONNECT_EXCEPTION.equals(response.statusMessage)) {
            return ModStatus.errInvalidFieldData("instance", "CONNECT_EXCEPTION", errorMessage);
            
          } else if (response.status == 400 
              && Response.SM_SOCKET_EXCEPTION.equals(response.statusMessage)) {
            return ModStatus.errInvalidFieldData("instance", "SOCKET_EXCEPTION", errorMessage);
            
          } else if (response.status == 400 
              && Response.SM_CONNECT_TIMEOUT.equals(response.statusMessage)) {
            return ModStatus.errInvalidFieldData("instance", "CONNECT_TIMEOUT", errorMessage);
            
          } else if (response.status == 401) {
            return ModStatus.errInvalidFieldData("instance", "UNAUTHORIZED", errorMessage);
            
          } else if (response.status == 404) {
            return ModStatus.errInvalidFieldData("instance", "LINK_BROKEN__NOT_FOUND", errorMessage);
            
          } else {
            return ModStatus.errInvalidFieldData("instance", "LINK_BROKEN", errorMessage);
          }
        }
        
      } catch (SubrequestException e) {
        e.printStackTrace();
        
        return ModStatus.errServerException();
      }
        
      
      if (!host.equals(target.getWorkerHost()) || ajpPortNumber != target.getWorkerAjpPort()) {
        target.rebind(host, ajpPortNumber);
      }
    }
    
    
    if (sourceDto.getActive() != null) {
      target.setActive(sourceDto.getActive());
    }
    if (sourceDto.getApplication() != null) {
      target.setApplication(sourceDto.getApplication());
    }
    
    return ModStatus.success();
  }
  
  private static final int CONNECT_TIMEOUT_MS = 2000; // TODO parametrize?
  
  /**
   * 
   * @param host
   * @param httpPort
   * @param uri begins with '/'
   * @return
   * @throws SubrequestException
   */
  private static Response subrequestAjpPortByHttp(String host, int httpPort, String uri) throws SubrequestException {
     
    if (false) {return new Response(200, null, ""+(httpPort - 1000));}
    
    try {
      final URL url = new URL("http://" + host + ":" + httpPort + uri);
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      
      // both timeouts necessary 
      connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
      connection.setReadTimeout(CONNECT_TIMEOUT_MS);
      
      try {
        connection.connect();
      } catch (UnknownHostException e) {
        // wrong host
        return new Response(400, Response.SM_UNKNOWN_HOST, null);
        
      } catch (ConnectException e) {
        // host OK, port is not working at all
        return new Response(400, Response.SM_CONNECT_EXCEPTION, null);
        
      }
      
      int status = connection.getResponseCode();
      String statusMessage = connection.getResponseMessage();

      String responseBody = null;
      if (status == 200) {// TODO or check 2xx?
        try (Scanner sc = new Scanner(connection.getInputStream())) {
          sc.useDelimiter("\\Z");
          if (sc.hasNext()) {
            responseBody = sc.next();
          }
        }
      }
      
      return new Response(status, statusMessage, responseBody);
      
    } catch (SocketException e) {// this may be thrown not only from connection.connect(), but also from connection.getResponseCode() etc
      // host OK, port OK, but it is not http port
      return new Response(400, Response.SM_SOCKET_EXCEPTION, null);
      
    } catch (SocketTimeoutException e) {// this may be thrown not only from connection.connect(), but also from connection.getResponseCode() etc
      // host OK, port OK, but it is not http port
      return new Response(400, Response.SM_CONNECT_TIMEOUT, null);
      
    } catch (Throwable e) {
      throw new SubrequestException("Failed to make subrequest to [" + host + ":" + httpPort + uri + "]", e);
    }
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

          ret = ModStatus.errItemNotFoundByLocation();

        } else {
          apacheConf.delete(location);

          ret = ModStatus.success();
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      
      ret = ModStatus.errServerException();
    }
    
    return ret;
  }
  
  private static ModStatus createBinding(
      ModRequestBodyDto mreq, ApacheConfJk apacheConf) {
    
    try {
      JkDto bindingDto = mreq.getData();

      
      // validate mandatory fields
      List<String> emptyMandatoryFields = validateMandatoryFields(bindingDto);
      if (!emptyMandatoryFields.isEmpty()) {
        String[] invalidFields = new String[emptyMandatoryFields.size() * 3];
        int i = 0;
        for (String fieldName: emptyMandatoryFields) {
          invalidFields[i++] = fieldName;
          invalidFields[i++] = "MANDATORY_EMPTY";
          invalidFields[i++] = null;
        }
        return ModStatus.errInvalidFieldData(invalidFields);
      }
      // validate 'instance' field value
      if (!validateInstanceFieldValue(bindingDto)) {
        return ModStatus.errInvalidFieldData("instance", "INVALID", null);
      }

      
      Binding newBinding = apacheConf.create();
      
      return updateFields(bindingDto, newBinding);

    } catch (Throwable e) {
      e.printStackTrace();
      
      return ModStatus.errServerException();
    }
  }
  
  /**
   * Validate mandatory fields
   * @param dto
   * @return list of field names whose values are empty (but must not be empty), or else empty list
   */
  private static List<String> validateMandatoryFields(JkDto dto) {
    List<String> emptyFields = new ArrayList<>();

    if (empty(dto.getApplication())) {
      emptyFields.add("application");
    }
    if (empty(dto.getInstance())) {
      emptyFields.add("instance");
    }
    return emptyFields;
  }
    
  
  private static class InstanceValueParser {
    public static final Pattern PATTERN = Pattern.compile("([^:]+):(\\d+)");
    
    public static ParseResult tryParse(String instance) {
      if (instance != null) {
        Matcher m = PATTERN.matcher(instance);
        if (m.matches()) {
          try {
            int port = Integer.parseInt(m.group(2)); 
            if (port >= 0 && port <= 65535) {
              return new ParseResult(true, m.group(1), port);
            } 
          } catch (NumberFormatException e) {
          }
        }
      }
      return new ParseResult(false, null, 0);
    }
    
    public static class ParseResult {
      public final boolean success;
      public final String host;
      public final int port;
      
      private ParseResult(boolean success, String host, int port) {
        this.success = success;
        this.host = host;
        this.port = port;
      }
    }
  }
  
  /**
   * Validate 'instance' field value
   * @param dto
   * @return true if dto has no 'instance' field or the 'instance' field value is not empty and valid 
   */
  private static boolean validateInstanceFieldValue(JkDto dto) {
    return dto.getInstance() == null || InstanceValueParser.tryParse(dto.getInstance()).success;
  }
  
  private static boolean empty(String string) {
    return string == null || "".equals(string);
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
    Integer ajpPort = binding.getWorkerAjpPort();
    if (ajpPort != null) {
      final String ajpPortStr = Integer.toString(binding.getWorkerAjpPort());
      dto.setAjpPort(ajpPortStr);
    }
    if (host != null && ajpPort != null) {
      dto.setGetHttpPortLink("api/jk/get-http-port?host=" + host + "&ajp-port=" + ajpPort);
    }
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
