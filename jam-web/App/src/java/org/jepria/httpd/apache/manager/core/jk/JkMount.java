package org.jepria.httpd.apache.manager.core.jk;

/**
 * Facade interface representing a "complete" Jk mount
 * consisting of two JkMount directives: 
 * root mount ('/Application') and asterisk mount ('/Application/*').
 */
/*package*/interface JkMount {
  
  /**
   * Service method.
   * @return JkMount's location to be used as a part of {@link Binding}'s location  
   */
  String getLocation();
  
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
}
