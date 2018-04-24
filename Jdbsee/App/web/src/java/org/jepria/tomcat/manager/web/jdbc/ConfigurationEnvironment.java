package org.jepria.tomcat.manager.web.jdbc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class ConfigurationEnvironment {
  
  protected final Path confPath;
  
  public ConfigurationEnvironment(Path confPath) {
    this.confPath = confPath;
  }
  
  public OutputStream getServerXmlOutputStream() throws FileNotFoundException {
    return new FileOutputStream(confPath.resolve("server.xml").toFile());
  }
  
  public InputStream getServerXmlInputStream() throws FileNotFoundException {
    return new FileInputStream(confPath.resolve("server.xml").toFile());
  }
  
  public OutputStream getContextXmlOutputStream() throws FileNotFoundException {
    return new FileOutputStream(confPath.resolve("context.xml").toFile());
  }
  
  public InputStream getContextXmlInputStream() throws FileNotFoundException {
    return new FileInputStream(confPath.resolve("context.xml").toFile());
  }
}