package org.jepria.httpd.apache.manager.core.modjk;

/**
 * Interface representing a complete Jk mount
 * consisting of two JkMount directives: 
 * root mount ('/Application') and asterisk mount ('/Application/*')
 */
/*package*/interface JkMount {
  
  boolean isCommented();
  
  /**
   * Common application for both root and asterisk mounts
   * (a part of url pattern between initial '/' and trailing '/*')
   */
  String application();
  
  /**
   * Reference to the line with root mount
   */
  TextLineReference rootMountLine();
  
  /**
   * Reference to the line with asterisk mount
   */
  TextLineReference asteriskMountLine();
  
  /**
   * The common worker name for both root and asterisk mounts
   */
  String workerName();
}
