package org.jepria.tomcat.manager.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.core.jdbc.ResourceInitialParams;

/**
 * Basic production environment
 */
public class BasicEnvironment implements Environment {
  
  private final File contextXml;
  private final File serverXml;
  
  private final File logsDirectory;
  
  private final EnvironmentPropertyFactory envPropertyFactory;
  
  /**
   * @param request
   * @return tomcat 'conf' directory
   */
  protected File getConfDirectory(HttpServletRequest request) {
    return Paths.get(request.getServletContext().getRealPath("")).getParent().getParent().resolve("conf2").toFile();
  }
  
  /**
   * @param request
   * @return tomcat 'logs' directory
   */
  protected File getLogsDirectory(HttpServletRequest request) {
    return Paths.get(request.getServletContext().getRealPath("")).getParent().getParent().resolve("logs").toFile();
  }
  
  public BasicEnvironment(HttpServletRequest request) {
    envPropertyFactory = new EnvironmentPropertyFactory(new File(request.getServletContext().getRealPath("/WEB-INF/app-conf-default.properties")));
    
    File confDir = getConfDirectory(request);
    
    contextXml = confDir.toPath().resolve("context.xml").toFile();
    serverXml = confDir.toPath().resolve("server.xml").toFile();
    
    logsDirectory = getLogsDirectory(request);
  }
  
  @Override
  public OutputStream getServerXmlOutputStream() {
    try {
      return new FileOutputStream(serverXml);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public InputStream getServerXmlInputStream() {
    try {
      return new FileInputStream(serverXml);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public OutputStream getContextXmlOutputStream() {
    try {
      return new FileOutputStream(contextXml);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public InputStream getContextXmlInputStream() {
    try {
      return new FileInputStream(contextXml);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public File getLogsDirectory() {
    return logsDirectory;
  }
  
  @Override
  public String getProperty(String name) {
    return envPropertyFactory.getProperty(name);
  }
  
  @Override
  public ResourceInitialParams getResourceInitialParams() {
    
    return new ResourceInitialParams() {
      
      private Map<String, String> getPropertyList(String propertyListName) {
        final Map<String, String> ret = new HashMap<>();
        String names = getProperty(propertyListName);
        if (names != null) {
          String[] namesSplit = names.split("\\s*,\\s*");
          for (String name: namesSplit) {
            if (name != null) {
              String value = getProperty(name);
              if (value != null) {
                if (name.startsWith(propertyListName + ".")) {
                  name = name.substring(propertyListName.length() + 1);
                }
                ret.put(name, value);
              }
            }
          }
        }
        return ret;
      }
      
      @Override
      public Map<String, String> getServerResourceAttrs() {
        return getPropertyList("org.jepria.tomcat.manager.web.jdbc.initial_attrs.ServerResource");
      }
      
      @Override
      public Map<String, String> getContextResourceLinkAttrs() {
        return getPropertyList("org.jepria.tomcat.manager.web.jdbc.initial_attrs.ContextResourceLink");
      }
      
      @Override
      public Map<String, String> getContextResourceAttrs() {
        return getPropertyList("org.jepria.tomcat.manager.web.jdbc.initial_attrs.ContextResource");
      }
      
      @Override
      public String getJdbcProtocol() {
        return getProperty("org.jepria.tomcat.manager.web.jdbc.protocol"); 
      }
    };
  }
}