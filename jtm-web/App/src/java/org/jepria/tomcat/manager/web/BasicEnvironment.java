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
  
  @Override
  /**
   * 1. Looks up the property defined in {@code java:comp/env/propertyName} JNDI entry. If the entry is defined, returns the value.
   * 2. Looks up the property in {@code appConfDirectory/application.properties} file 
   * (if an application configuration directory is defined in {@code java:comp/env/org.jepria.tomcat.manager.web.appConfDirectory} JNDI entry). 
   * If the entry is defined, returns the value.
   * 3. Looks up the property in internal {@code conf-default/application.properties} file. If the property is defined, returns the value. 
   * 4. returns {@code null}.
   */
  public String getApplicationProperty(String propertyName) {
    
    final String env = getEnv(propertyName);
    
    if (env == null) {
      return readProperties("application.properties").get(propertyName);
    } else {
      return env;
    }
  }
  
  /**
   * 
   * @param filepath relative to the application conf directory
   * @return
   */
  protected Map<String, String> readProperties(String filepath) {
    
    final String env = getEnv("org.jepria.tomcat.manager.web.appConfDirectory");
    
    // target file to read properties from
    File file;
    if (env != null) {
      file = new File(env, filepath);
      
      if (!file.exists()) {
        file = new File(appConfDirectoryDefault, filepath);
      }
    } else {
      file = new File(appConfDirectoryDefault, filepath);
    }
    
    try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
      
      final Properties properties = new Properties();
      properties.load(reader);
      
      return properties.entrySet().stream()
          .collect(Collectors.toMap(e -> (String)e.getKey(), e -> (String)e.getValue()));
      
    } catch (IOException e) {
      throw new RuntimeException("Misconfiguration exception: file [" + file + "]: ", e);
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
        return getApplicationProperty("org.jepria.tomcat.manager.web.jdbc.protocol"); 
      }
    };
  }
}