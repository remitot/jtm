package org.jepria.httpd.apache.manager.core.jk;

public interface Binding {
  
  boolean isActive();
  void setActive(boolean active);

  String getApplication();
  void setApplication(String application);
  
  String getWorker();
  void setWorker(String worker);
  
  String getInstance();
  void setInstance(String instance);
}
