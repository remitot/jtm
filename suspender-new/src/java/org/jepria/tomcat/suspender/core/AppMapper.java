package org.jepria.tomcat.suspender.core;

public interface AppMapper {
  /**
   * Try to map the uri to an alive (unsuspended/deployed) application 
   * @param uri
   * @return null if no match found
   */
  String mapToAppAlive(String uri);
  
  /**
   * Try to map the uri to a suspended application 
   * @param uri
   * @return null if no match found
   */
  String mapToAppSuspended(String uri);
}
