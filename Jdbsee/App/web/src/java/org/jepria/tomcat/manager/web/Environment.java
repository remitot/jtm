package org.jepria.tomcat.manager.web;

import java.io.InputStream;
import java.io.OutputStream;

import org.jepria.tomcat.manager.core.ConnectionInitialParams;

/**
 * Environment-dependent configuration parameters
 */
public interface Environment {
  
  /**
   * @return new output stream for the server.xml configuration file
   */
  OutputStream getServerXmlOutputStream();
  
  /**
   * @return new input stream for the server.xml configuration file
   */
  InputStream getServerXmlInputStream();
  
  /**
   * @return new output stream for the context.xml configuration file
   */
  OutputStream getContextXmlOutputStream();
  
  /**
   * @return new input stream for the context.xml configuration file
   */
  InputStream getContextXmlInputStream();
  
  /**
   * @return initial (default) params for the newly created JDBC connection
   */
  ConnectionInitialParams getJdbcConnectionInitialParams();
}