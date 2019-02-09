package org.jepria.httpd.apache.manager.core.jk;

/**
 * Class representing a worker
 * consisting of three worker properties: 
 * {@code worker.name.type}, {@code worker.name.host} and {@code worker.name.port}
 */
/*package*/interface Worker {
  boolean isCommented();
  /**
   * The common worker name for all three properties
   */
  String name();
  
  String type();
  /**
   * Reference to the line with {@code worker.name.type} property
   */
  TextLineReference typePropertyLine();
  
  String host();
  /**
   * Reference to the line with {@code worker.name.host} property
   */
  TextLineReference hostPropertyLine();
  
  String port();
  /**
   * Reference to the line with {@code worker.name.port} property
   */
  TextLineReference portPropertyLine();
}
