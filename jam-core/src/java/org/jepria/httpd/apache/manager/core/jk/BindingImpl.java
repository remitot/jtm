package org.jepria.httpd.apache.manager.core.jk;

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
  public String getWorkerAjpPort() {
    // TODO assume "ajp13".equals(worker.getType()), see WorkerFactory.tryParseWorkerProperty
    return worker.getPort();
  }
  
  @Override
  public void rebind(String host, String ajpPort) {
    
    Worker existingWorker = findWorker(host, ajpPort);
    if (existingWorker != null) {
      worker = existingWorker;
    } else {
      final String newWorkerName0 = getNewWorkerName(host, ajpPort);
      
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
   * @param host not null
   * @param ajpPort not null
   * @return or else null
   */
  private Worker findWorker(String host, String ajpPort) {
    for (Worker worker: apacheConf.getWorkers().values()) {
      // TODO assume "ajp13".equals(worker.getType()), see WorkerFactory.tryParseWorkerProperty
      if (host.equals(worker.getHost()) && ajpPort.equals(worker.getPort())) {
        return worker;
      }
    }
    return null;
  }
  
  @Override
  void delete() {
    jkMount.delete();
    
    // TODO cleanup workers: check whether or not any other jkMount is bound to the same worker, and if none, delete also the worker 
  }
}