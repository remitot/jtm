package org.jepria.httpd.apache.manager.web.jk;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.jepria.httpd.apache.manager.core.jk.ApacheConfJk;
import org.jepria.httpd.apache.manager.core.jk.JkMount;
import org.jepria.httpd.apache.manager.core.jk.Worker;
import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.jk.AjpAdapter.AjpException;
import org.jepria.httpd.apache.manager.web.jk.JkApi.ModStatus.Code;
import org.jepria.httpd.apache.manager.web.jk.dto.BindingDto;
import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.httpd.apache.manager.web.jk.dto.WorkerDto;

public class JkApi {

  public List<JkMountDto> getJkMounts(Environment environment) {

    final ApacheConfJk apacheConf = new ApacheConfJk(
        () -> environment.getMod_jk_confInputStream(), 
        () -> environment.getWorkers_propertiesInputStream());

    return getJkMounts(apacheConf);
  }

  /**
   * 
   * @param environment
   * @param jkMountId
   * @return or else {@code null}
   */
  public BindingDto getBinding(Environment environment, String jkMountId) {
    return getBinding(environment, jkMountId, true);
  }
  
  /**
   * 
   * @param environment
   * @param jkMountId
   * @param renameLocalhost whether to rename {@code localhost} value of the {@link Worker#getHost()} with the real hostname
   * @return or else {@code null}
   */
  public BindingDto getBinding(Environment environment, String jkMountId, boolean renameLocalhost) {

    final ApacheConfJk apacheConf = new ApacheConfJk(
        () -> environment.getMod_jk_confInputStream(), 
        () -> environment.getWorkers_propertiesInputStream());

    Binding binding = getBinding(apacheConf, jkMountId);
    
    if (binding == null) {
      return null;
    }
    
    
    JkMountDto jkMountDto = mountToDto(jkMountId, binding.jkMount());
    WorkerDto workerDto = workerToDto(binding.workerId(), binding.worker(), renameLocalhost);
    
    final BindingDto bindingDto = new BindingDto(jkMountDto, workerDto);
    
    
    
    // request http port over ajp
    Integer httpPort = null;
    if (binding.worker() != null) {
      
      final String host = binding.worker().getHost();
      final String ajpPort = binding.worker().getPort();
      
      if (host != null && ajpPort != null) {
        Integer ajpPortInt = Integer.parseInt(ajpPort);
        String tomcatManagerExtCtxPath = lookupTomcatManagerPath(environment, host, ajpPortInt);
        
        try {
          httpPort = AjpAdapter.requestHttpPortOverAjp(host, ajpPortInt, tomcatManagerExtCtxPath);
          bindingDto.httpPort = String.valueOf(httpPort);
          
        } catch (AjpException e) {
          e.printStackTrace();
          
          httpPort = null;
          bindingDto.httpErrorCode = 1;
        }
      }
    }
    
    
    
    // build http link
    if (bindingDto.jkMount != null && bindingDto.worker != null && httpPort != null) {
      
      final String application = bindingDto.jkMount.map.get("application");
      final String host = bindingDto.worker.map.get("host"); 
          
      if (application != null) {
        StringBuilder link = new StringBuilder();
        link.append("http://").append(host);
        if (httpPort != 80) {
          link.append(':').append(httpPort);
        }
        link.append("/").append(application);
        
        bindingDto.httpLink = link.toString();
      }
    }
    
    
    return bindingDto;
  }
  
  public static class ModStatus {
    
    public enum Code {
      /**
       * Modification succeeded
       */
      SUCCESS,
      /**
       * Client field data is invalid (incorrect format, or value processing exception)
       */
      INVALID_FIELD_DATA,
    }
    
    public final Code code;
    
    /**
     * Only in case of {@link #code} == {@link InvalidFieldDataCode#INVALID_FIELD_DATA}: invalid field names mapped to error codes
     */
    public final Map<String, InvalidFieldDataCode> invalidFieldDataMap;
    
    private ModStatus(Code code, Map<String, InvalidFieldDataCode> invalidFieldDataMap) {
      this.code = code;
      this.invalidFieldDataMap = invalidFieldDataMap;
    }

    public static ModStatus success() {
      return new ModStatus(Code.SUCCESS, null); 
    }
    
    /**
     * Field invalidity description code
     */
    public static enum InvalidFieldDataCode {
      MANDATORY_EMPTY,
      DUPLICATE_APPLICATION,
      BOTH_HTTP_AJP_PORT_EMPTY,
      BOTH_HTTP_AJP_PORT,
      /**
       * Failed to request HTTP port over AJP
       */
      HTTP_PORT_REQUEST_FAILED,
    }
    
    /**
     * 
     * @param invalidFieldDataMap {@code Map<fieldName, errorCode>}
     */
    public static ModStatus errInvalidFieldData(Map<String, InvalidFieldDataCode> invalidFieldDataMap) {
      return new ModStatus(Code.INVALID_FIELD_DATA, invalidFieldDataMap);
    }
  }

  public ModStatus updateBinding(Environment environment, String mountId, Map<String, String> fields) {

    Objects.requireNonNull(mountId, "mountId must not be null");
    
    final Map<String, ModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
    
    // validate mandatory empty fields
    List<String> emptyFields = validateEmptyFieldsForUpdate(fields);
    if (!emptyFields.isEmpty()) {
      for (String fieldName: emptyFields) {
        invalidFieldDataMap.put(fieldName, ModStatus.InvalidFieldDataCode.MANDATORY_EMPTY);
      }
    }
    
    // validate httpPort and ajpPort dependency
    // one and only one field must be not empty
    String httpPort = fields.get("httpPort");
    String ajpPort = fields.get("ajpPort");
    if (httpPort != null && !"".equals(httpPort) && ajpPort != null && !"".equals(ajpPort)) {
      invalidFieldDataMap.put("httpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT);
      invalidFieldDataMap.put("ajpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT);
    }
    
    if (!invalidFieldDataMap.isEmpty()) {
      return ModStatus.errInvalidFieldData(invalidFieldDataMap);
    }
    
    
    final ApacheConfJk conf = new ApacheConfJk(
        () -> environment.getMod_jk_confInputStream(), 
        () -> environment.getWorkers_propertiesInputStream());
    
    final Binding binding = getBinding(conf, mountId);
    
    if (binding == null) {
      throw new IllegalStateException("No binding found by such mountId=[" + mountId + "]");
    }
    
    ModStatus modStatus = updateFields(environment, conf, fields, binding);
    
    if (modStatus.code == Code.SUCCESS) {
      conf.save(environment.getMod_jk_confOutputStream(), 
          environment.getWorkers_propertiesOutputStream());
    }
    
    return modStatus;
  }
  
  

  /**
   * Updates target's fields with source's values
   * @param fields
   * @param target non null
   * @return
   */
  protected ModStatus updateFields(Environment environment, ApacheConfJk conf, Map<String, String> fields, Binding target) {
    
    // validate application
    final String application = fields.get("application");
    Map<String, JkMount> mounts = conf.getMounts();
    if (mounts.values().stream().anyMatch(jkMount -> application.equals(jkMount.getApplication()))) {
      // duplicate application
      final Map<String, ModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
      invalidFieldDataMap.put("application", ModStatus.InvalidFieldDataCode.DUPLICATE_APPLICATION);
      return ModStatus.errInvalidFieldData(invalidFieldDataMap);
    }
    
    

    // obtain ajpPort
    final String ajpPort;
    {
      String ajpPort0 = fields.get("ajpPort");
      if (ajpPort0 == null || "".equals(ajpPort0)) {
        // request ajp port over http
        
        final String host = fields.get("host");
        final String httpPort = fields.get("httpPort");
        
        if (host != null && httpPort != null) {
          Integer httpPortInt = Integer.parseInt(httpPort);
          String tomcatManagerExtCtxPath = lookupTomcatManagerPath(environment, host, httpPortInt);
          
          try {
            int ajpPortInt = requestAjpPortOverHttp(host, httpPortInt, tomcatManagerExtCtxPath + "/api/port/ajp");
            ajpPort0 = String.valueOf(ajpPortInt);
            
          } catch (Exception e) {
            e.printStackTrace();
            
            ajpPort0 = null;
          }
        }
        
        
        if (ajpPort0 == null) {
          Map<String, ModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
          invalidFieldDataMap.put("host", ModStatus.InvalidFieldDataCode.HTTP_PORT_REQUEST_FAILED);
          invalidFieldDataMap.put("httpPort", ModStatus.InvalidFieldDataCode.HTTP_PORT_REQUEST_FAILED);
          return ModStatus.errInvalidFieldData(invalidFieldDataMap);
        }
      }
      
      ajpPort = ajpPort0;
    }
    
    
    // ajpPort is not null at this point
    

    throw new UnsupportedOperationException("Not impl yet. AjpPort ready: ajp=" + ajpPort);
    
    // rebind first
    
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
  }
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
  public ModStatus createBinding(Environment environment, Map<String, String> fields) {

    { // validation
      final Map<String, ModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
      
      // validate mandatory empty fields
      List<String> emptyFields = validateEmptyFieldsForCreate(fields);
      if (!emptyFields.isEmpty()) {
        for (String fieldName: emptyFields) {
          invalidFieldDataMap.putIfAbsent(fieldName, ModStatus.InvalidFieldDataCode.MANDATORY_EMPTY);
        }
      }
  
      
      String httpPort = fields.get("httpPort");
      String ajpPort = fields.get("ajpPort");
      
      
      // validate httpPort and ajpPort dependency
      // one and only one field must be not empty
      if ((httpPort == null || "".equals(httpPort)) && (ajpPort == null || "".equals(ajpPort))) {
        invalidFieldDataMap.putIfAbsent("httpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT_EMPTY);
        invalidFieldDataMap.putIfAbsent("ajpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT_EMPTY);
      }
      if (httpPort != null && !"".equals(httpPort) && ajpPort != null && !"".equals(ajpPort)) {
        invalidFieldDataMap.putIfAbsent("httpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT);
        invalidFieldDataMap.putIfAbsent("ajpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT);
      }
      
      
      if (!invalidFieldDataMap.isEmpty()) {
        return ModStatus.errInvalidFieldData(invalidFieldDataMap);
      }
    }
    
    
    final ApacheConfJk conf = new ApacheConfJk(
        () -> environment.getMod_jk_confInputStream(), 
        () -> environment.getWorkers_propertiesInputStream());
    
    
    Binding binding = createBinding();
    
    ModStatus modStatus = updateFields(environment, conf, fields, binding);
    
    if (modStatus.code == Code.SUCCESS) {
      conf.save(environment.getMod_jk_confOutputStream(), 
          environment.getWorkers_propertiesOutputStream());
    }
    
    return modStatus;
  }
  
  protected Binding createBinding() {
    // TODO stopped here
    return new BindingImpl(null, null, null, null);
  }
  
  protected int requestAjpPortOverHttp(String host, int httpPort, String uri) throws Exception {
    try {
      
      final URL url;
      try {
        url = new URL("http", host, httpPort, uri);
      } catch (MalformedURLException e) {
        // impossible: trusted url
        throw new RuntimeException(e);
      }
    
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
  
      // both timeouts necessary 
      connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
      connection.setReadTimeout(CONNECT_TIMEOUT_MS);
  
      connection.connect();
  
      int status = connection.getResponseCode();
  
      String responseBody = null;
      if (status == 200) {// TODO or check 2xx?
        try (Scanner sc = new Scanner(connection.getInputStream())) {
          sc.useDelimiter("\\Z");
          if (sc.hasNext()) {
            responseBody = sc.next();
          } else {
            throw new RuntimeException("Empty response body");
          }
        }
      }
      
      return Integer.parseInt(responseBody);
      
    } catch (Throwable e) {
      throw new Exception(e);
    }
  }
  
  /**
   * For requesting AJP port over HTTP
   */
  private static final int CONNECT_TIMEOUT_MS = 2000;
  
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

  /**
   * Validate empty fields for create
   * @param dto
   * @return list of invalidly empty or missing mandatory fields, or else empty list, not null
   */
  protected List<String> validateEmptyFieldsForCreate(Map<String, String> fields) {
    List<String> emptyFields = new ArrayList<>();

    // the fields must be neither null, nor empty
    String application = fields.get("application");
    if (application == null || "".equals(application)) {
      emptyFields.add("application");
    }
    String host = fields.get("host");
    if (host == null || "".equals(host)) {
      emptyFields.add("host");
    }
    
    return emptyFields;
  }
  
  /**
   * Validate empty fields for update
   * @param fields
   * @return list of invalidly empty fields, or else empty list, not null
   */
  protected List<String> validateEmptyFieldsForUpdate(Map<String, String> fields) {
    List<String> emptyFields = new ArrayList<>();

    // the fields may be null, but if not null then not empty
    if ("".equals(fields.get("application"))) {
      emptyFields.add("application");
    }
    if ("".equals(fields.get("host"))) {
      emptyFields.add("host");
    }
    return emptyFields;
  }
  
  protected List<JkMountDto> getJkMounts(ApacheConfJk apacheConf) {
    Map<String, JkMount> mounts = apacheConf.getMounts();

    // list all bindings
    return mounts.entrySet().stream().map(
        entry -> mountToDto(entry.getKey(), entry.getValue()))
        .sorted(mountSorter()).collect(Collectors.toList());
  }


  /**
   * Null-safe
   * @param mountId
   * @param mount
   * @return
   */
  protected JkMountDto mountToDto(String mountId, JkMount mount) {
    if (mountId == null && mount == null) {
      return null;
    }

    JkMountDto dto = new JkMountDto();
    dto.map.put("id", mountId);
    if (mount != null) {
      dto.map.put("active", Boolean.FALSE.equals(mount.isActive()) ? "false" : "true");
      dto.map.put("application", mount.getApplication());
    }
    return dto;
  }

  protected Comparator<JkMountDto> mountSorter() {
    return new Comparator<JkMountDto>() {
      @Override
      public int compare(JkMountDto o1, JkMountDto o2) {
        int applicationCmp = o1.map.get("application").toLowerCase().compareTo(o2.map.get("application").toLowerCase());
        if (applicationCmp == 0) {
          // the active is the first
          if ("true".equals(o1.map.get("active")) && "false".equals(o2.map.get("active"))) {
            return -1;
          } else if ("true".equals(o2.map.get("active")) && "false".equals(o1.map.get("active"))) {
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

  /**
   * 
   * @param apacheConf
   * @param jkMountId
   * @return or else {@code null}
   */
  protected Binding getBinding(ApacheConfJk apacheConf, String jkMountId) {
    JkMount jkMount = apacheConf.getMounts().get(jkMountId);

    if (jkMount == null) {
      return null;
    }


    // lookup worker by name from jkMount
    Map<String, Worker> workers = apacheConf.getWorkers();

    String workerId = null;
    Worker worker = null;
    for (Map.Entry<String, Worker> workerEntry: workers.entrySet()) {
      if (workerEntry.getValue().getName().equals(jkMount.workerName())) {
        workerId = workerEntry.getKey();
        worker = workerEntry.getValue();
        break;
      }
    }
    
    return new BindingImpl(jkMountId, jkMount, workerId, worker);
  }
  
  /**
   * @return hostname of the localhost
   */
  protected String getLocalhostName() {
    try {
      return InetAddress.getLocalHost().getHostName().toLowerCase();
    } catch (UnknownHostException e) {
      e.printStackTrace();
      // TODO fail-fast or fail-safe?
      return "localhost"; // fallback
    }
  }
  
  /**
   * Null-safe
   * @param workerId
   * @param worker
   * @param renameLocalhost whether to rename {@code localhost} value of the {@link Worker#getHost()} with the real hostname
   * @return
   */
  protected WorkerDto workerToDto(String workerId, Worker worker, boolean renameLocalhost) {
    if (workerId == null && worker == null) {
      return null;
    }

    WorkerDto dto = new WorkerDto();
    dto.map.put("id", workerId);
    if (worker != null) {
      dto.map.put("name", worker.getName());
      dto.map.put("type", worker.getType());
      
      String host = worker.getHost();
      if (renameLocalhost && "localhost".equals(host)) {
        host = getLocalhostName();
      }
      dto.map.put("host", host);
      
      dto.map.put("port", worker.getPort());
    }
    return dto;
  }
  
  protected String lookupTomcatManagerPath(Environment environment, String host, int port) {
    String tomcatManagerPath = environment.getProperty("org.jepria.httpd.apache.manager.web.TomcatManager." + host + "." + port + ".path");
    if (tomcatManagerPath == null) {
      tomcatManagerPath = environment.getProperty("org.jepria.httpd.apache.manager.web.TomcatManager.default.path");
      if (tomcatManagerPath == null) {
        throw new RuntimeException("Misconfiguration exception: "
            + "mandatory configuration property \"org.jepria.httpd.apache.manager.web.TomcatManager.default.path\" is not defined");
      }
    }
    return tomcatManagerPath;
  }
}
