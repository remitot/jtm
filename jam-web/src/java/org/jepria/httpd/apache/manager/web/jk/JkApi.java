package org.jepria.httpd.apache.manager.web.jk;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
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
import org.jepria.httpd.apache.manager.web.jk.JkApi.ModStatus.InvalidFieldDataCode;
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
      PORT_SYNTAX,
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
    
    // either one of two may be (not null and not empty)
    String ajpPortForUpdate = null;
    String httpPortForUpdate = null;
    
    { // validation
      final Map<String, ModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
      
      
      final String application = fields.get("application");
      final String host = fields.get("host");
      final String httpPort = fields.get("httpPort");
      final String ajpPort = fields.get("ajpPort");      
      
      
      // validate empty fields
      
      if ((application == null && (target.jkMount().getApplication() == null || "".equals(target.jkMount().getApplication())))
          || "".equals(application)) {
        invalidFieldDataMap.putIfAbsent("application", InvalidFieldDataCode.MANDATORY_EMPTY);
      }
      if ((host == null && (target.worker().getHost() == null || "".equals(target.worker().getHost())))
          || "".equals(host)) {
        invalidFieldDataMap.putIfAbsent("host", InvalidFieldDataCode.MANDATORY_EMPTY);
      }
      
      
      // validate duplicate application
      
      if (application != null) {
        Map<String, JkMount> mounts = conf.getMounts();
        if (mounts.values().stream().anyMatch(jkMount -> application.equals(jkMount.getApplication()))) {
          // duplicate application
          invalidFieldDataMap.putIfAbsent("application", ModStatus.InvalidFieldDataCode.DUPLICATE_APPLICATION);
        }
      }
      
      
      // port dependency: one and the only port field must be not empty
      
      if (httpPort == null || "".equals(httpPort)) {
        if ("".equals(ajpPort)) {
          if (httpPort == null) {
            // no changes
          } else {
            // report both empty
            invalidFieldDataMap.putIfAbsent("httpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT_EMPTY);
            invalidFieldDataMap.putIfAbsent("ajpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT_EMPTY);
          }
          
        } else if (ajpPort == null) {
          // check target ajpPort
          if (target.worker().getPort() == null || "".equals(target.worker().getPort())) {
            // report both empty
            invalidFieldDataMap.putIfAbsent("httpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT_EMPTY);
            invalidFieldDataMap.putIfAbsent("ajpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT_EMPTY);
            
          } else {
            // no changes
          }
          
        } else {
          // apply ajp13 field value
          ajpPortForUpdate = ajpPort;
          httpPortForUpdate = null;
        }
        
      } else {
        if (ajpPort == null || "".equals(ajpPort)) {
          // apply httpfield value
          ajpPortForUpdate = null;
          httpPortForUpdate = httpPort;
          
        } else {
          // report both not empty
          invalidFieldDataMap.putIfAbsent("httpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT);
          invalidFieldDataMap.putIfAbsent("ajpPort", ModStatus.InvalidFieldDataCode.BOTH_HTTP_AJP_PORT);
        }
      }
          

      // validate port syntax
      if (httpPortForUpdate != null && !httpPortForUpdate.matches("\\d+")) {
        invalidFieldDataMap.putIfAbsent("httpPort", ModStatus.InvalidFieldDataCode.PORT_SYNTAX);
      }
      if (ajpPortForUpdate != null && !ajpPortForUpdate.matches("\\d+")) {
        invalidFieldDataMap.putIfAbsent("ajpPort", ModStatus.InvalidFieldDataCode.PORT_SYNTAX);
      }
      
      
      if (!invalidFieldDataMap.isEmpty()) {
        return ModStatus.errInvalidFieldData(invalidFieldDataMap);
      }
    }      

    
    
    if (ajpPortForUpdate == null && httpPortForUpdate != null) {
      // request ajp port over http
      final String host = fields.get("host");
      
      if (host != null && httpPortForUpdate != null) {
        Integer httpPort = Integer.parseInt(httpPortForUpdate);
        String tomcatManagerExtCtxPath = lookupTomcatManagerPath(environment, host, httpPort);
        
        try {
          int ajpPort = requestAjpPortOverHttp(host, httpPort, tomcatManagerExtCtxPath + "/api/port/ajp");
          ajpPortForUpdate = String.valueOf(ajpPort);
          
        } catch (Exception e) {
          e.printStackTrace();
          
          ajpPortForUpdate = null;
        }
      }
      
      
      if (ajpPortForUpdate == null) {
        Map<String, ModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
        invalidFieldDataMap.put("host", ModStatus.InvalidFieldDataCode.HTTP_PORT_REQUEST_FAILED);
        invalidFieldDataMap.put("httpPort", ModStatus.InvalidFieldDataCode.HTTP_PORT_REQUEST_FAILED);
        return ModStatus.errInvalidFieldData(invalidFieldDataMap);
      }
    }
    
    
    // apply changes
    // TODO stopped here
//    String active = fields.get("active");
//    if (active != null) {
//      target.jkMount().setActive(!"false".equals(active));
//    }
//    
//    String application = fields.get("application");
//    if (application != null) {
//      target.jkMount().setApplication(application);
//    }
//    
//    String host = fields.get("host");
//    if (host == null && target.getWorkerHost()) || ajpPortNumber != target.getWorkerAjpPort()) {
//      target.rebind(host, ajpPortNumber);
//    }
      
//    // TODO worker name? rebind? optimize?
//    
//    String host = fields.get("host");
//    if (host != null) {
//      target.worker().setHost(host);
//    }
//    
//    if (ajpPort != null) {
//      target.worker().setType("ajp13");
//      target.worker().setPort(ajpPort);
//    }
    
    
    
    throw new UnsupportedOperationException("Not impl yet: update "+fields+"; ajp=" + ajpPortForUpdate + "; http=" + httpPortForUpdate);
    
    // rebind first
    
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
    JkMount newJkMount = new JkMount() {
      @Override
      public String workerName() {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public void setWorkerName(String workerName) {
        // TODO Auto-generated method stub
        
      }
      @Override
      public void setApplication(String application) {
        // TODO Auto-generated method stub
      }
      @Override
      public void setActive(boolean active) {
        // TODO Auto-generated method stub
      }
      @Override
      public boolean isActive() {
        // TODO Auto-generated method stub
        return false;
      }
      @Override
      public String getApplication() {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public void delete() {
        // TODO Auto-generated method stub
      }
    };
    
    Worker newWorker = new Worker() {
      @Override
      public void setType(String type) {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void setPort(String port) {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void setName(String name) {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void setHost(String host) {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public String getType() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getPort() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getName() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getHost() {
        // TODO Auto-generated method stub
        return null;
      }
    };
    return new BindingImpl(null, newJkMount, null, newWorker);
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
