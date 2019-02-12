package org.jepria.httpd.apache.manager.core.jk;

/**
 * Facade interface representing a "complete" worker
 * consisting of three worker properties: 
 * {@code worker.name.type}, {@code worker.name.host} and {@code worker.name.port}.
 */
/*package*/interface Worker {
  
  /**
   * Service method.
   * @return JkMount's location to be used as a part of {@link Binding}'s location  
   */
  String getLocation();
  
  boolean isActive();
  void setActive(boolean active);
  
  /**
   * The common worker name for all three properties
   */
  String getName();
  void setName(String name);
  
  String type();
  
  String host();
  void setHost(String host);
  
  String port();
  void setPort(String port);
}
