package org.jepria.httpd.apache.manager.web.jk;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jepria.httpd.apache.manager.core.jk.ApacheConfJk;
import org.jepria.httpd.apache.manager.core.jk.Binding;
import org.jepria.httpd.apache.manager.core.jk.JkMount;
import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.jk.dto.BindingDto;
import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;

public class JkApi {

  public List<JkMountDto> getJkMounts(Environment environment) {
    
    final ApacheConfJk apacheConf = new ApacheConfJk(
        () -> environment.getMod_jk_confInputStream(), 
        () -> environment.getWorkers_propertiesInputStream());
      
    return getJkMounts(apacheConf);
  }
  
  public BindingDto getBinding(Environment environment, String jkMountId) {
    
    final ApacheConfJk apacheConf = new ApacheConfJk(
        () -> environment.getMod_jk_confInputStream(), 
        () -> environment.getWorkers_propertiesInputStream());
      
    return getBinding(apacheConf, jkMountId);
  }
  
//  private enum ModType {
//    HTTP,
//    AJP
//  }
//  
//  private static final String MANAGER_EXT_AJP_PORT_URI = "/api/port/ajp";
//  private static final String MANAGER_EXT_HTTP_PORT_URI = "/api/port/http";
//  
//  
//  
//  private static String lookupTomcatManagerPath(Environment environment, String host, int port) {
//    String tomcatManagerPath = environment.getProperty("org.jepria.httpd.apache.manager.web.TomcatManager." + host + "." + port + ".path");
//    if (tomcatManagerPath == null) {
//      tomcatManagerPath = environment.getProperty("org.jepria.httpd.apache.manager.web.TomcatManager.default.path");
//      if (tomcatManagerPath == null) {
//        throw new RuntimeException("Misconfiguration exception: "
//            + "mandatory configuration property \"org.jepria.httpd.apache.manager.web.TomcatManager.default.path\" is not defined");
//      }
//    }
//    return tomcatManagerPath;
//  }
//  
//  private static class Subresponse {
//    public static final int SC_UNKNOWN_HOST = 461;
//    public static final int SC_CONNECT_EXCEPTION = 462;
//    public static final int SC_SOCKET_EXCEPTION = 463;
//    public static final int SC_CONNECT_TIMEOUT = 464;
//    
//    public final int status;
//    public final String responseBody;
//    
//    public Subresponse(int status, String responseBody) {
//      this.status = status;
//      this.responseBody = responseBody;
//    }
//  }
//  
//  @Override
//  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//    String path = request.getPathInfo();
//    
//    if ("/mod/http".equals(path)) {
//      mod(request, response, ModType.HTTP);
//      return;
//      
//    } else if ("/mod/ajp".equals(path)) {
//      mod(request, response, ModType.AJP);
//      return;
//      
//    } else {
//      
//      response.sendError(HttpServletResponse.SC_NOT_FOUND);
//      response.flushBuffer();
//      return;
//    }
//  }
//  
//  /**
//   * 
//   * @param req
//   * @param resp
//   * @param modType {@link ModType#HTTP} for create or update requests by http instance (like "tomcat-server:8080");
//   * {@link ModType#AJP} for create or update requests by ajp instance (like "tomcat-server:8009"). 
//   * @throws ServletException
//   * @throws IOException
//   */
//  private void mod(HttpServletRequest req, HttpServletResponse resp, ModType modType) throws ServletException, IOException {
//    
//    // the content type is defined for the entire method
//    resp.setContentType("application/json; charset=UTF-8");
//
//    try {
//      
//      // read list from request body
//      final List<ModRequestDto> modRequests;
//      
//      try {
//        Type mapType = new TypeToken<ArrayList<ModRequestDto>>(){}.getType();
//        modRequests = new Gson().fromJson(new InputStreamReader(req.getInputStream()), mapType);
//        
//      } catch (Throwable e) {
//        e.printStackTrace();
//
//        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
//        resp.flushBuffer();
//        return;
//      } 
//      
//      
//      // convert list to map
//      final Map<String, ModRequestBodyDto> modRequestBodyMap = new HashMap<>();
//      
//      if (modRequests != null) {
//        for (ModRequestDto modRequest: modRequests) {
//          final String modRequestId = modRequest.getModRequestId();
//          
//          // validate modRequestId fields
//          if (modRequestId == null || "".equals(modRequestId)) {
//            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
//                "Found missing or empty modRequestId fields");
//            resp.flushBuffer();
//            return;
//            
//          } else if (modRequestBodyMap.put(modRequestId, modRequest.getModRequestBody()) != null) {
//            
//            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
//                "Duplicate modRequestId field values found: [" + modRequestId + "]");
//            resp.flushBuffer();
//            return;
//          }
//        }
//      }
//      
//
//      
//      final Environment environment = EnvironmentFactory.get(req);
//      
//      final ApacheConfJk apacheConf = new ApacheConfJk(
//          () -> environment.getMod_jk_confInputStream(), 
//          () -> environment.getWorkers_propertiesInputStream());
//
//      // collect processed modRequests
//      final Set<String> processedModRequestIds = new HashSet<>();
//      
//      
//      // response map
//      final Map<String, ModStatus> modStatusMap = new HashMap<>();
//      
//      // all modifications succeeded
//      boolean allModSuccess = true; 
//      
//      // 1) perform all updates
//      for (String modRequestId: modRequestBodyMap.keySet()) {
//        ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);
//
//        if ("update".equals(mreq.getAction())) {
//          processedModRequestIds.add(modRequestId);
//
//          ModStatus modStatus = updateBinding(mreq, apacheConf, modType, environment);
//          if (modStatus.code != ModStatus.SC_SUCCESS) {
//            allModSuccess = false;
//          }
//
//          modStatusMap.put(modRequestId, modStatus);
//        }
//      }
//
//
//      // 2) perform all deletions
//      for (String modRequestId: modRequestBodyMap.keySet()) {
//        ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);
//
//        if ("delete".equals(mreq.getAction())) {
//          processedModRequestIds.add(modRequestId);
//
//          ModStatus modStatus = deleteBinding(mreq, apacheConf);
//          if (modStatus.code != ModStatus.SC_SUCCESS) {
//            allModSuccess = false;
//          }
//          
//          modStatusMap.put(modRequestId, modStatus);
//        }
//      }
//
//
//      // 3) perform all creations
//      for (String modRequestId: modRequestBodyMap.keySet()) {
//        ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);
//
//        if ("create".equals(mreq.getAction())) {
//          processedModRequestIds.add(modRequestId);
//
//          ModStatus modStatus = createBinding(mreq, apacheConf, modType, environment);
//          if (modStatus.code != ModStatus.SC_SUCCESS) {
//            allModSuccess = false;
//          }
//          
//          modStatusMap.put(modRequestId, modStatus);
//        }
//      }
//
//
//      // 4) ignore illegal actions
//
//
//      // prepare response map
//      final Map<String, Object> responseJsonMap = new HashMap<>();
//      
//      // convert map to list of JSON objects
//      List<Map<String, Object>> modStatusList = new ArrayList<>();
//      for (Map.Entry<String, ModStatus> entry: modStatusMap.entrySet()) {
//        Map<String, Object> jsonMap = new HashMap<>();
//        jsonMap.put("modRequestId", entry.getKey());
//        int code = entry.getValue().code;
//        jsonMap.put("modStatusCode", code);
//        if (code == ModStatus.SC_INVALID_FIELD_DATA) {
//          jsonMap.put("invalidFieldData", entry.getValue().invalidFieldDataMap);
//        }
//        modStatusList.add(jsonMap);
//      }
//      
//      responseJsonMap.put("modStatusList", modStatusList);
//      
//      
//      if (allModSuccess) {
//        // save modifications and add a new _list to the response
//        
//        apacheConf.save(environment.getMod_jk_confOutputStream(), 
//            environment.getWorkers_propertiesOutputStream());
//        
//        
//        // add the new list to the response
//        
//        final ApacheConfJk apacheConfAfterSave = new ApacheConfJk(
//            () -> environment.getMod_jk_confInputStream(), 
//            () -> environment.getWorkers_propertiesInputStream());
//        
//        
//        final List<BindingListDto> bindingsAfterSave = getBindings(apacheConfAfterSave, renameLocalhost(environment));
//        responseJsonMap.put("_list", bindingsAfterSave);
//      }
//      
//      
//      try (OutputStreamWriter osw = new OutputStreamWriter(resp.getOutputStream(), "UTF-8")) {
//        new Gson().toJson(responseJsonMap, osw);
//      }
//      
//      resp.setStatus(HttpServletResponse.SC_OK);
//      resp.flushBuffer();
//      
//      return;
//      
//    } catch (Throwable e) {
//      e.printStackTrace();
//
//      // response body must either be empty or match the declared content type
//      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//      resp.flushBuffer();
//      return;
//    }
//  }
//  
//  protected boolean renameLocalhost(Environment environment) {
//    return "true".equals(environment.getProperty("org.jepria.httpd.apache.manager.web.jk.renameLocalhost"));
//  }
//  
//  private static ModStatus updateBinding(
//      ModRequestBodyDto mreq, ApacheConfJk apacheConf, ModType modType,
//      Environment environment) {
//    
//    try {
//      String id = mreq.getId();
//
//      if (id == null) {
//        return ModStatus.errEmptyId();
//      }
//
//      Map<String, Binding> bindings = apacheConf.getBindings();
//      Binding binding = bindings.get(id);
//
//      if (binding == null) {
//        return ModStatus.errNoItemFoundById();
//      }
//      
//      BindingModDto bindingDto = mreq.getData();
//      
//      
//      // validate 'instance' field value
//      if (!validateInstanceFieldValue(bindingDto)) {
//        return ModStatus.errInvalidFieldData("instance", "INVALID", null);
//      }
//      
//      // validate application
//      final String application = bindingDto.getApplication();
//      if (application != null) {
//        if (!apacheConf.validateNewApplication(bindingDto.getApplication())) {
//          return ModStatus.errInvalidFieldData("application", "DUPLICATE_NAME", null);
//        }
//      }
//      
//
//      return updateFields(bindingDto, binding, modType, environment);
//      
//    } catch (Throwable e) {
//      e.printStackTrace();
//      
//      return ModStatus.errServerException();
//    }
//  }
//  
//  /**
//   * Updates target's fields with source's values
//   * @param sourceDto
//   * @param target non null
//   * @return
//   */
//  private static ModStatus updateFields(BindingModDto sourceDto, Binding target, ModType modType, 
//      Environment environment) {
//
//    // rebinding comes first
//    
//    if (sourceDto.getInstance() != null) {
//        
//      final InstanceValueParser.ParseResult parseResult = InstanceValueParser.tryParse(sourceDto.getInstance());
//        
//      final String host = parseResult.host;
//      final int ajpPortNumber;
//      
//      switch (modType) {
//      case AJP: {
//        ajpPortNumber = parseResult.port;
//        break;
//      }
//      
//      case HTTP: {
//        // get ajp port by http
//        final int httpPortNumber = parseResult.port;
//        
//        final String tomcatManagerPath = lookupTomcatManagerPath(environment, host, httpPortNumber);
//        
//        final URL url;
//        try {
//          url = new URL("http", host, httpPortNumber, tomcatManagerPath + MANAGER_EXT_AJP_PORT_URI);
//        } catch (MalformedURLException e) {
//          // impossible: trusted url
//          throw new RuntimeException(e);
//        }
//        
//        final Subresponse subresponse = wrapSubrequest(new Subrequest() {
//          @Override
//          public Subresponse execute() throws IOException {
//            
//            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
//            
//            // both timeouts necessary 
//            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
//            connection.setReadTimeout(CONNECT_TIMEOUT_MS);
//            
//            connection.connect();
//            
//            int status = connection.getResponseCode();
//  
//            String responseBody = null;
//            if (status == 200) {// TODO or check 2xx?
//              try (Scanner sc = new Scanner(connection.getInputStream())) {
//                sc.useDelimiter("\\Z");
//                if (sc.hasNext()) {
//                  responseBody = sc.next();
//                }
//              }
//            }
//            
//            return new Subresponse(status, responseBody);
//          }
//        });
//        
//        
//        
//        if (subresponse.status == HttpServletResponse.SC_OK) {
//          ajpPortNumber = Integer.parseInt(subresponse.responseBody);
//          
//        } else {
//          final String errorCode = getSubresponseStatus(subresponse, url.toString());
//          
//          return ModStatus.errInvalidFieldData("instance", errorCode, null);
//        }
//        break;
//      }
//      default: {
//        throw new IllegalArgumentException("Unknown modType [" + modType + "]");
//      }
//      }
//        
//      
//      if (!host.equals(target.getWorkerHost()) || ajpPortNumber != target.getWorkerAjpPort()) {
//        target.rebind(host, ajpPortNumber);
//      }
//    }
//    
//    
//    if (sourceDto.getActive() != null) {
//      target.setActive(sourceDto.getActive());
//    }
//    if (sourceDto.getApplication() != null) {
//      target.setApplication(sourceDto.getApplication());
//    }
//    
//    return ModStatus.success();
//  }
//  
//  /**
//   * Convert {@link Subresponse} to an errorCode String to pass to the client
//   * @param subresponse
//   * @param url url of the subrequest, for client logging/messaging
//   * @return
//   */
//  private static String getSubresponseStatus(Subresponse subresponse, String url) {
//    if (subresponse.status == HttpServletResponse.SC_OK) {
//      return "SUCCESS";
//    } else if (subresponse.status == Subresponse.SC_UNKNOWN_HOST) { 
//      return "UNKNOWN_HOST@@" + url;
//    } else if (subresponse.status == Subresponse.SC_CONNECT_EXCEPTION) { 
//      return "CONNECT_EXCEPTION@@" + url;
//    } else if (subresponse.status == Subresponse.SC_SOCKET_EXCEPTION) {
//      return "SOCKET_EXCEPTION@@" + url;
//    } else if (subresponse.status == Subresponse.SC_CONNECT_TIMEOUT) { 
//      return "CONNECT_TIMEOUT@@" + url;
//    } else {
//      // @@ is a safe delimiter
//      return "UNSUCCESS_STATUS@@" + subresponse.status + "@@" + url;
//    }
//  }
//  
//  private static final int CONNECT_TIMEOUT_MS = 2000; // TODO parametrize?
//  
//  private static interface Subrequest {
//    Subresponse execute() throws IOException;
//  }
//  
//  private static Subresponse wrapSubrequest(Subrequest subrequest) {
//    try {
//      return subrequest.execute();
//      
//    } catch (UnknownHostException e) {
//      // wrong host
//      return new Subresponse(Subresponse.SC_UNKNOWN_HOST, null);
//      
//    } catch (ConnectException e) {
//      // host OK, port is not working at all
//      return new Subresponse(Subresponse.SC_CONNECT_EXCEPTION, null);
//      
//    } catch (SocketException e) {
//      // host OK, port OK, invalid protocol
//      return new Subresponse(Subresponse.SC_SOCKET_EXCEPTION, null);
//      
//    } catch (SocketTimeoutException e) {
//      // host OK, port OK, invalid protocol
//      return new Subresponse(Subresponse.SC_CONNECT_TIMEOUT, null);
//      
//    } catch (Throwable e) {
//      e.printStackTrace();
//      
//      return new Subresponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
//    }
//  }
//  
//  private static ModStatus deleteBinding(
//      ModRequestBodyDto mreq, ApacheConfJk apacheConf) {
//
//    try {
//      String id = mreq.getId();
//
//      if (id == null) {
//        return ModStatus.errEmptyId();
//      }
//
//      Binding binding = apacheConf.getBindings().get(id);
//
//      if (binding == null) {
//        return ModStatus.errNoItemFoundById();
//      }
//      
//      apacheConf.delete(id);
//      
//      return ModStatus.success();
//      
//    } catch (Throwable e) {
//      e.printStackTrace();
//      
//      return ModStatus.errServerException();
//    }
//  }
//  
//  private static ModStatus createBinding(
//      ModRequestBodyDto mreq, ApacheConfJk apacheConf, ModType modType,
//      Environment environment) {
//    
//    try {
//      BindingModDto bindingDto = mreq.getData();
//
//      
//      // validate mandatory fields
//      List<String> emptyMandatoryFields = validateMandatoryFields(bindingDto);
//      if (!emptyMandatoryFields.isEmpty()) {
//        String[] invalidFields = new String[emptyMandatoryFields.size() * 3];
//        int i = 0;
//        for (String fieldName: emptyMandatoryFields) {
//          invalidFields[i++] = fieldName;
//          invalidFields[i++] = "MANDATORY_EMPTY";
//          invalidFields[i++] = null;
//        }
//        return ModStatus.errInvalidFieldData(invalidFields);
//      }
//      // validate 'instance' field value
//      if (!validateInstanceFieldValue(bindingDto)) {
//        return ModStatus.errInvalidFieldData("instance", "INVALID", null);
//      }
//
//      // validate application
//      if (!apacheConf.validateNewApplication(bindingDto.getApplication())) {
//        return ModStatus.errInvalidFieldData("application", "DUPLICATE_NAME", null);
//      }
//      
//      
//      Binding newBinding = apacheConf.create();
//      
//      return updateFields(bindingDto, newBinding, modType, environment);
//
//    } catch (Throwable e) {
//      e.printStackTrace();
//      
//      return ModStatus.errServerException();
//    }
//  }
//  
//  /**
//   * Validate mandatory fields
//   * @param dto
//   * @return list of field names whose values are empty (but must not be empty), or else empty list
//   */
//  private static List<String> validateMandatoryFields(BindingModDto dto) {
//    List<String> emptyFields = new ArrayList<>();
//
//    if (empty(dto.getApplication())) {
//      emptyFields.add("application");
//    }
//    if (empty(dto.getInstance())) {
//      emptyFields.add("instance");
//    }
//    return emptyFields;
//  }
//    
//  
//  private static class InstanceValueParser {
//    public static final Pattern PATTERN = Pattern.compile("([^:]+):(\\d+)");
//    
//    public static ParseResult tryParse(String instance) {
//      if (instance != null) {
//        Matcher m = PATTERN.matcher(instance);
//        if (m.matches()) {
//          try {
//            int port = Integer.parseInt(m.group(2)); 
//            if (port >= 0 && port <= 65535) {
//              return new ParseResult(true, m.group(1), port);
//            } 
//          } catch (NumberFormatException e) {
//          }
//        }
//      }
//      return new ParseResult(false, null, 0);
//    }
//    
//    public static class ParseResult {
//      public final boolean success;
//      public final String host;
//      public final int port;
//      
//      private ParseResult(boolean success, String host, int port) {
//        this.success = success;
//        this.host = host;
//        this.port = port;
//      }
//    }
//  }
//  
//  /**
//   * Validate 'instance' field value
//   * @param dto
//   * @return true if dto has no 'instance' field or the 'instance' field value is not empty and valid 
//   */
//  protected boolean validateInstanceFieldValue(BindingModDto dto) {
//    return dto.getInstance() == null || InstanceValueParser.tryParse(dto.getInstance()).success;
//  }
//  
//  protected boolean empty(String string) {
//    return string == null || "".equals(string);
//  }
  
  protected List<JkMountDto> getJkMounts(ApacheConfJk apacheConf) {
    Map<String, JkMount> mounts = apacheConf.getMounts();

    // list all bindings
    return mounts.entrySet().stream().map(
        entry -> mountToDto(entry.getKey(), entry.getValue()))
        .sorted(mountSorter()).collect(Collectors.toList());
  }
  
//  protected String getLocalhostName() {
//    try {
//      return InetAddress.getLocalHost().getHostName().toLowerCase();
//    } catch (UnknownHostException e) {
//      e.printStackTrace();
//      // TODO fail-fast or fail-safe?
//      return "localhost"; // fallback
//    }
//  }
  
  protected JkMountDto mountToDto(String id, JkMount mount) {
    JkMountDto dto = new JkMountDto();
    dto.put("active", Boolean.FALSE.equals(mount.isActive()) ? "false" : "true");
    dto.put("id", id);
    dto.put("application", mount.getApplication());
    return dto;
  }
  
  protected Comparator<JkMountDto> mountSorter() {
    return new Comparator<JkMountDto>() {
      @Override
      public int compare(JkMountDto o1, JkMountDto o2) {
        int applicationCmp = o1.get("application").toLowerCase().compareTo(o2.get("application").toLowerCase());
        if (applicationCmp == 0) {
          // the active is the first
          if ("true".equals(o1.get("active")) && "false".equals(o2.get("active"))) {
            return -1;
          } else if ("true".equals(o2.get("active")) && "false".equals(o1.get("active"))) {
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
  
  protected BindingDto getBinding(ApacheConfJk apacheConf, String jkMountId) {
    Binding binding = apacheConf.getBinding(jkMountId);
    
    if (binding == null) {
      return null;
    } else {
      return bindingToDto(binding);
    }
  }
  
  protected BindingDto bindingToDto(Binding binding) {
    BindingDto dto = new BindingDto();
    dto.put("active", Boolean.FALSE.equals(binding.isActive()) ? "false" : "true");
    dto.put("application", binding.getApplication());
    dto.put("workerHost", binding.getWorkerHost());
    dto.put("workerAjpPort", binding.getWorkerAjpPort());
    return dto;
  }
}
