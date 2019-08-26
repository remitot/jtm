package org.jepria.tomcat.manager.web;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

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
   * @return new {@link Path} representing the {@code conf} directory
   * (normally at TOMCAT_HOME/conf). Normally the file is an existing readable directory.
   */
  default Path getConfDirectory() {
    return getHomeDirectory().resolve("conf");
  }

  default Path getServerXml() {
    return getConfDirectory().resolve("server.xml");
  }

  default Path getContextXml() {
    return getConfDirectory().resolve("context.xml");
  }
  
  /**
   * @return new {@link Path} representing the {@code logs} directory
   * (normally at TOMCAT_HOME/logs). Normally the file is an existing readable directory.
   */
  default Path getLogsDirectory() {
    return getHomeDirectory().resolve("logs");
  }
  
  /**
   * @return Path known as TOMCAT_HOME. Normally the file is an existing readable directory.
   */
  Path getHomeDirectory();
  
  /**
   * @return initial params for the newly created resources
   */
  ResourceInitialParams getResourceInitialParams();
  
  /**
   * Retrieve an application configuration property.
   * 1. Look up the property defined in {@code java:comp/env/name} JNDI entry. If the entry is defined, return the value.
   * 2. Look up the property in the custom {@code app-conf.properties} file, if such file is defined in 
   * {@code java:comp/env/org.jepria.tomcat.manager.web.conf.file} JNDI entry. If the property is defined in that file, return the value.
   * 3. Look up the property in internal {@code app-conf-default.properties} file. If the property is defined, return the value. 
   * 4. return {@code null}.
   */
  String getProperty(String name);
}