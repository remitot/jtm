package org.jepria.httpd.apache.manager.core.jk;

/*package*/class BindingImpl implements Binding {
  
  private final JkMount jkMount;
  private final Worker worker;
  
  public BindingImpl(JkMount jkMount, Worker worker) {
    this.jkMount = jkMount;
    this.worker = worker;
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
  public String getWorkerName() {
    return worker.getName();
  }
  
  @Override
  public void setWorkerName(String workerName) {
    jkMount.setWorkerName(workerName);
    worker.setName(workerName);
  }
  
  @Override
  public String getWorkerHost() {
    return worker.host();
  }
  
  @Override
  public void setWorkerHost(String workerHost) {
    worker.setHost(workerHost);
  }
  
  @Override
  public int getWorkerAjpPort() {
    // TODO how to guarantee the ajp13 type here?
    if (!"ajp13".equals(worker.type())) {
      throw new IllegalStateException("Expected ajp13 worker type, but actual: " + worker.type());
    }
    return Integer.parseInt(worker.port());//TODO is this the best place to parse? what if exception?; 
  }
  
  @Override
  public void setWorkerAjpPort(int workerAjpPort) {
    // TODO how to guarantee the ajp13 type here?
    if (!"ajp13".equals(worker.type())) {
      throw new IllegalStateException("Expected ajp13 worker type, but actual: " + worker.type());
    }
    worker.setPort(Integer.toString(workerAjpPort));
  }
}