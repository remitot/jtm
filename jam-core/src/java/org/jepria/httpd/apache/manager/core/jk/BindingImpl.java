package org.jepria.httpd.apache.manager.core.jk;

import java.util.Optional;

/*package*/class BindingImpl extends BaseBinding {
  
  private final JkMount jkMount;
  /**
   * Not final, may be rebound with {@link #rebind(String, int)} or {@code null} in a newly created Binding
   */
  private Worker worker;
  
  private final ApacheConfJk apacheConf;
  
  public BindingImpl(JkMount jkMount, Worker worker, ApacheConfJk apacheConf) {
    this.jkMount = jkMount;
    this.worker = worker;
    this.apacheConf = apacheConf;
  }

  @Override
  public boolean isActive() {
    return jkMount.isActive();
  }
  
  @Override
  public void setActive(boolean active) {
    jkMount.setActive(active);
  }
  
  @Override
  public String getApplication() {
    return jkMount.getApplication();
  }
  
  @Override
  public void setApplication(String application) {
    jkMount.setApplication(application);
  }
  
  @Override
  public String getWorkerHost() {
    if (worker != null) {
      return worker.getHost();
    } else {
      return null;
    }
  }
  
  @Override
  public Integer getWorkerAjpPort() {
    if (worker != null) {
      // TODO assume "ajp13".equals(worker.getType()), see WorkerFactory.tryParseWorkerProperty
      return worker.getPort();
    } else {
      return null;
    }
  }
  
  @Override
  public void rebind(String host, int ajpPort) {
    final String ajpPortStr = Integer.toString(ajpPort);
    
    Worker existingWorker = findWorker(host, ajpPort);
    if (existingWorker != null) {
      worker = existingWorker;
    } else {
      final String newWorkerName0 = getNewWorkerName(host, ajpPortStr);
      
      // find unique name by appending _index
      String newWorkerName = newWorkerName0;
      int i = 2;
      while (!apacheConf.validateNewWorkerName(newWorkerName)) {
        newWorkerName = newWorkerName0 + "_" + i++;
      }
      
      
      Worker newWorker = apacheConf.createWorker(newWorkerName);
      newWorker.setHost(host);
      newWorker.setPort(ajpPort);
      worker = newWorker;
    }
    
    jkMount.setWorkerName(worker.getName());
  }
  
  // TODO extract upwards or parametrize
  private String getNewWorkerName(String host, String ajpPort) {
    return (host + "_" + ajpPort).replaceAll("\\.|\\-|/|:", "_");
  }
  
  /**
   * Lookup existing worker by host and AJP port
   * @param host
   * @param ajpPort
   * @return or else null
   */
  private Worker findWorker(String host, int ajpPort) {
    Optional<Worker> workerOpt = apacheConf.getWorkers().stream().filter(
        // TODO assume "ajp13".equals(worker.getType()), see WorkerFactory.tryParseWorkerProperty
        worker -> host.equals(worker.getHost()) && ajpPort == worker.getPort()).findAny();
    if (workerOpt.isPresent()) {
      return workerOpt.get();
    } else {
      return null;
    }
  }
  
  @Override
  void delete() {
    jkMount.delete();
    
    // TODO cleanup workers: check whether or not any other jkMount is bound to the same worker, and if none, delete also the worker 
  }
}