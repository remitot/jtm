package org.jepria.tomcat.suspender;

public interface AppMapper {
  /**
   * Try to map the uri to an unsuspended (alive, deployed) application 
   * @param uri
   * @return null if no match found
   */
  String mapToAppUnsuspended(String uri);
  
  /**
   * Try to map the uri to a suspended application 
   * @param uri
   * @return null if no match found
   */
  String mapToAppSuspended(String uri);
}
