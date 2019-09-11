package org.jepria.tomcat.manager.web;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jepria.tomcat.manager.core.jdbc.ResourceInitialParams;

/**
 * Environment-dependent configuration parameters
 */
public interface Environment {
  
  /**
   * @return new output stream for the server.xml configuration file 
   */
  default OutputStream getServerXmlOutputStream() {
    try {
      return new FileOutputStream(getServerXml().toFile());
    } catch (FileNotFoundException e) {
      // impossible: the file existence must be checked in getServerXml() by contract
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return new input stream for the server.xml configuration file
   */
  default InputStream getServerXmlInputStream() {
    try {
      return new FileInputStream(getServerXml().toFile());
    } catch (FileNotFoundException e) {
      // impossible: the file existence must be checked in getServerXml() by contract
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return new output stream for the context.xml configuration file
   */
  default OutputStream getContextXmlOutputStream() {
    try {
      return new FileOutputStream(getContextXml().toFile());
    } catch (FileNotFoundException e) {
      // impossible: the file existence must be checked in getContextXml() by contract
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return new input stream for the context.xml configuration file
   */
  default InputStream getContextXmlInputStream() {
    try {
      return new FileInputStream(getContextXml().toFile());
    } catch (FileNotFoundException e) {
      // impossible: the file existence must be checked in getContextXml() by contract
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return new {@link Path} representing the {@code conf} directory
   * (normally at TOMCAT_HOME/conf). Normally the file is an existing readable directory.
   */
  default Path getConfDirectory() {
    return getHomeDirectory().resolve("conf");
  }

  /**
   * Normally at TOMCAT_HOME/conf/server.xml.
   * Must fail with some misconfiguration exception if the file could not be found or read, etc.
   */
  default Path getServerXml() {
    Path path = getConfDirectory().resolve("server.xml");
    if (!Files.isRegularFile(path)) {
      throw new RuntimeException("Misconfiguration exception: could not initialize server.xml file: [" + path + "] is not a file");
    }
    return path;
  }

  /**
   * Normally at TOMCAT_HOME/conf/context.xml.
   * Must fail with some misconfiguration exception if the file could not be found or read, etc.
   */
  default Path getContextXml() {
    Path path = getConfDirectory().resolve("context.xml");
    if (!Files.isRegularFile(path)) {
      throw new RuntimeException("Misconfiguration exception: could not initialize context.xml file: [" + path + "] is not a file");
    }
    return path;
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