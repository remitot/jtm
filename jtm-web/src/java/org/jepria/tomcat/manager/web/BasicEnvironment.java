package org.jepria.tomcat.manager.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.core.jdbc.ResourceInitialParams;

/**
 * Basic production environment
 */
public class BasicEnvironment implements Environment {
  
  private final Path home;
  
  private final File contextXml;
  private final File serverXml;
  
  private final EnvironmentPropertyFactory envPropertyFactory;
  
  protected Path getTomcatHome(HttpServletRequest request) {
    return Paths.get(request.getServletContext().getRealPath("")).getParent().getParent();
  }
  
  public BasicEnvironment(HttpServletRequest request) {
    envPropertyFactory = new EnvironmentPropertyFactory(new File(request.getServletContext().getRealPath("/WEB-INF/app-conf-default.properties")));
    
    home = getHomeDirectory(request);
    
    contextXml = getConfDirectory().resolve("context.xml").toFile();
    serverXml = getConfDirectory().resolve("server.xml").toFile();
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
  
  public Path getHomeDirectory(HttpServletRequest request) {
    return Paths.get(request.getServletContext().getRealPath("")).getParent().getParent();
  }
  
  @Override
  public Path getHomeDirectory() {
    return home;
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