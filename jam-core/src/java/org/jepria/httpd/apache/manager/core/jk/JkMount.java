package org.jepria.httpd.apache.manager.core.jk;

/**
 * Facade interface representing a "complete" Jk mount
 * consisting of two JkMount directives: 
 * root mount ('/Application') and asterisk mount ('/Application/*').
 */
public interface JkMount {
  
  boolean isActive();
  void setActive(boolean active);
  
  /**
   * Common application for both root and asterisk mounts
   * (a part of url pattern between initial '/' and trailing '/*')
   */
  String getApplication();
  void setApplication(String application);
  
  /**
   * The common worker name for both root and asterisk mounts
   */
  String workerName();
  void setWorkerName(String workerName);
  
  void delete();
}
