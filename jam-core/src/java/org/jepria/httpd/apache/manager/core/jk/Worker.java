package org.jepria.httpd.apache.manager.core.jk;

/**
 * Facade interface representing a "complete" worker
 * consisting of three worker properties: 
 * {@code worker.name.type}, {@code worker.name.host} and {@code worker.name.port}.
 */
/*package*/interface Worker {
  
  /**
   * Service method.
   * @return Worker's id to be used as a part of {@link Binding}'s id  
   */
  String getId();
  
  /**
   * The common worker name for all three properties
   */
  String getName();
  
  String getType();
  
  String getHost();
  void setHost(String host);
  
  Integer getPort();
  void setPort(Integer port);
}