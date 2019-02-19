package org.jepria.tomcat.manager.web;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.jepria.tomcat.manager.core.jdbc.ResourceInitialParams;

/**
 * Environment-dependent configuration parameters
 */
public interface Environment {
  
  /**
   * @return new output stream for the server.xml configuration file 
   * (normally at TOMCAT_HOME/conf/server.xml)
   */
  OutputStream getServerXmlOutputStream();
  
  /**
   * @return new input stream for the server.xml configuration file
   * (normally at TOMCAT_HOME/conf/server.xml)
   */
  InputStream getServerXmlInputStream();
  
  /**
   * @return new output stream for the context.xml configuration file
   * (normally at TOMCAT_HOME/conf/context.xml)
   */
  OutputStream getContextXmlOutputStream();
  
  /**
   * @return new input stream for the context.xml configuration file
   * (normally at TOMCAT_HOME/conf/context.xml)
   */
  InputStream getContextXmlInputStream();
  
  /**
   * @return new {@link File} representing the {@code logs} directory
   * (normally at TOMCAT_HOME/logs). Normally the file is an existing readable directory.
   */
  File getLogsDirectory();
  
  /**
   * @return initial params for the newly created resources
   */
  ResourceInitialParams getResourceInitialParams();
  
  /**
   * Retrieve an application configuration property
   * @param name
   * @return
   */
  String getApplicationProperty(String name);
}