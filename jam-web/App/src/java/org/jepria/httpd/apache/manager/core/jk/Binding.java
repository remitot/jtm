package org.jepria.httpd.apache.manager.core.jk;

/**
 * Facade interface representing a "complete" Jk binding
 * consisting of {@link JkMount} and {@link Worker}. 
 */
public interface Binding {
  
  boolean isActive();
  void setActive(boolean active);
  
  String getApplication();
  void setApplication(String application);
  
  String getWorkerName();
  void setWorkerName(String workerName);
  
  String getWorkerHost();
  void setWorkerHost(String workerHost);
  
  // TODO is this legal to bing the name of the method to AJP type?
  int getWorkerAjpPort();
  void setWorkerAjpPort(int workerAjpPort);
}
