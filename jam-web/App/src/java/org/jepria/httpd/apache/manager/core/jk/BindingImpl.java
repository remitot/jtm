package org.jepria.httpd.apache.manager.core.jk;

import java.util.Optional;

/*package*/class BindingImpl implements Binding {
  
  private final JkMount jkMount;
  /**
   * Not final, may be rebound with {@link #rebind(String, int)}
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
    return jkMount.isActive() && worker.isActive();
  }
  
  @Override
  public void setActive(boolean active) {
    jkMount.setActive(active);
    worker.setActive(active);
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
    return worker.getHost();
  }
  
  @Override
  public int getWorkerAjpPort() {
    // TODO assume "ajp13".equals(worker.getType())
    return Integer.parseInt(worker.getPort());//TODO is this the best place to parse? what if exception?; 
  }
  
  @Override
  public void rebind(String host, int ajpPort) {
    final String ajpPortStr = Integer.toString(ajpPort);
    
    final boolean wasActive = isActive();
    
    Worker existingWorker = findWorker(host, ajpPortStr);
    if (existingWorker != null) {
      worker = existingWorker;
    } else {
      final String workerName = getNewWorkerName(host, ajpPortStr);
      Worker newWorker = apacheConf.createWorker(workerName);
      newWorker.setActive(wasActive);
      newWorker.setHost(host);
      newWorker.setPort(ajpPortStr);
      worker = newWorker;
    }
    
    jkMount.setWorkerName(worker.getName());
  }
  
  // TODO extract upwards or parametrize
  private String getNewWorkerName(String host, String ajpPort) {
    return host + "_" + ajpPort;
  }
  
  /**
   * Lookup existing worker by host and AJP port
   * @param host
   * @param ajpPort
   * @return or else null
   */
  private Worker findWorker(String host, String ajpPort) {
    Optional<Worker> workerOpt = apacheConf.getWorkers().stream().filter(
        // TODO assume "ajp13".equals(worker.getType())
        worker -> host.equals(worker.getHost()) && ajpPort.equals(worker.getPort())).findAny();
    if (workerOpt.isPresent()) {
      return workerOpt.get();
    } else {
      return null;
    }
  }
}