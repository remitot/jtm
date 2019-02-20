package org.jepria.tomcat.manager.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class EnvironmentPropertyFactory {
  private final File confDefault;

  public EnvironmentPropertyFactory(File confDefault) {
    this.confDefault = confDefault;
  }
  
  protected String getEnv(String name) {
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      return (String) envCtx.lookup(name);
    } catch (NamingException e) {
      // TODO fail-fast or fail-safe? what if the user configured the env variable, but the exception occurred?
      return null;
    }
  }
  
  public String getProperty(String propertyName) {
    
    String value = getEnv(propertyName);
    
    if (value != null) {
      return value;
    }
    
    final String confEnv = getEnv("org.jepria.tomcat.manager.web.conf.file");
    
    if (confEnv != null) {
      File conf = new File(confEnv);
      
      value = getProperty(propertyName, conf);
      
      if (value != null) {
        return value;
      }
    }
    
    return getProperty(propertyName, confDefault);
  }
  
  protected String getProperty(String name, File file) {
    try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
      
      final Properties properties = new Properties();
      properties.load(reader);
      return properties.getProperty(name);
      
    } catch (IOException e) {
      throw new RuntimeException("Misconfiguration exception: file [" + file + "]: ", e);
    }
  }
}
