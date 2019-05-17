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
  
  String getWorkerHost();
  // TODO is this legal to bing the name of the method to AJP type?
  String getWorkerAjpPort();
  
  /**
   * Rebind this Binding to another Worker (existing or potentially new).
   * If rebinding succeeded, the subsequent binding modifications 
   * will affect the new worker (if they intend), but not the old (which is unbound)  
   */
  void rebind(String host, String ajpPort);
}
