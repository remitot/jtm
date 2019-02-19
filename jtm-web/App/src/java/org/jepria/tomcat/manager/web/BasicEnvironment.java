package org.jepria.tomcat.manager.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.core.jdbc.ResourceInitialParams;

/**
 * Basic production environment
 */
public class BasicEnvironment implements Environment {
  
  private final File contextXml;
  private final File serverXml;
  
  private final File logsDirectory;
  
  private final String appConfDirectoryDefault;
  
  /**
   * @param request
   * @return tomcat 'conf' directory
   */
  protected File getConfDirectory(HttpServletRequest request) {
    return Paths.get(request.getServletContext().getRealPath("")).getParent().getParent().resolve("conf").toFile();
  }
  
  /**
   * @param request
   * @return tomcat 'logs' directory
   */
  protected File getLogsDirectory(HttpServletRequest request) {
    return Paths.get(request.getServletContext().getRealPath("")).getParent().getParent().resolve("logs").toFile();
  }
  
  public BasicEnvironment(HttpServletRequest request) {
    File confDir = getConfDirectory(request);
    
    contextXml = confDir.toPath().resolve("context.xml").toFile();
    serverXml = confDir.toPath().resolve("server.xml").toFile();
    
    logsDirectory = getLogsDirectory(request);
    
    appConfDirectoryDefault = request.getServletContext().getRealPath("/WEB-INF/conf-default");
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
  

  protected String getAppConfDirectoryFilename() {
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      return (String) envCtx.lookup("org.jepria.tomcat.manager.web.jdbc.InitialAttrs");
    } catch (NamingException e) {
      return null;
    }
  }
  
  @Override
  public Map<String, String> getApplicationProperties() {
    return readProperties("application.properties");
  }
  
  /**
   * 
   * @param filepath relative to the application conf directory
   * @return
   */
  private Map<String, String> readProperties(String filepath) {
    
    String appConfDirectoryPath;
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      String appConfDirectoryEnv = (String) envCtx.lookup("org.jepria.tomcat.manager.web.appConfDirectory");
      
      if (appConfDirectoryEnv != null) {
        appConfDirectoryPath = appConfDirectoryEnv;
            
      } else {
        // TODO fail-fast or fail-safe?
        // fail-fast:
//          throw new RuntimeException("Misconfiguration exception: "
//              + "failed to obtain application conf directory "
//              + "from [java:comp/env/org.jepria.tomcat.manager.web.jdbc.InitialAttrs]", 
//              new NullPointerException());
        appConfDirectoryPath = appConfDirectoryDefault;
      }
      
    } catch (NamingException e) {
      
      // TODO fail-fast or fail-safe?
      // fail-fast:
//      throw new RuntimeException("Misconfiguration exception: "
//          + "failed to obtain application conf directory "
//          + "from [java:comp/env/org.jepria.tomcat.manager.web.jdbc.InitialAttrs]", e);
      appConfDirectoryPath = appConfDirectoryDefault;
    }
    
    final File appConfDirectory = new File(appConfDirectoryPath); 
    
    try (Reader reader = new InputStreamReader(new FileInputStream(new File(appConfDirectory, filepath)), "UTF-8")) {
      final Properties properties = new Properties();
      properties.load(reader);
      return properties.entrySet().stream()
          .collect(Collectors.toMap(e -> (String)e.getKey(), e -> (String)e.getValue()));
    } catch (IOException e) {
      throw new RuntimeException("Misconfiguration exception: "
          + "application conf directory [" + appConfDirectory + "]: ", 
          e);
    }
  }
  
  
  @Override
  public ResourceInitialParams getResourceInitialParams() {
    return new ResourceInitialParams() {
      @Override
      public Map<String, String> getServerResourceAttrs() {
        return readProperties("jdbc.initial/ServerResource.properties");
      }
      
      @Override
      public Map<String, String> getContextResourceLinkAttrs() {
        return readProperties("jdbc.initial/ContextResourceLink.properties");
      }
      
      @Override
      public Map<String, String> getContextResourceAttrs() {
        return readProperties("jdbc.initial/ContextResource.properties");
      }
      
      @Override
      public String getJdbcProtocol() {
        return getApplicationProperties().get("jdbc.protocol"); 
      }
    };
  }
}