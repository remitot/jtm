package org.jepria.tomcat.manager.web.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.core.jdbc.ConnectionInitialParams;

/**
 * Arbitrary environment-dependent configuration parameters
 */
public class ConfigurationEnvironment {
  
  private final File contextXml;
  private final File serverXml;
  
  private final File contextResourceDefaultAttrs;
  private final File contextResourceLinkDefaultAttrs;
  private final File serverResourceDefaultAttrs;
  
  public ConfigurationEnvironment(HttpServletRequest request) {
    Path confPath = Paths.get(request.getServletContext().getRealPath("")).getParent().getParent().resolve("conf");
    
    contextXml = confPath.resolve("context.xml").toFile();
    serverXml = confPath.resolve("server.xml").toFile();
    
    contextResourceDefaultAttrs = new File(request.getServletContext().getRealPath(
        "/WEB-INF/jdbc_connection_default_attrs/context_Resource.default_attrs.properties"));
    contextResourceLinkDefaultAttrs = new File(request.getServletContext().getRealPath(
        "/WEB-INF/jdbc_connection_default_attrs/context_ResourceLink.default_attrs.properties"));
    serverResourceDefaultAttrs = new File(request.getServletContext().getRealPath(
        "/WEB-INF/jdbc_connection_default_attrs/server_Resource.default_attrs.properties"));
  }
  
  public OutputStream getServerXmlOutputStream() throws FileNotFoundException {
    return new FileOutputStream(serverXml);
  }
  
  public InputStream getServerXmlInputStream() throws FileNotFoundException {
    return new FileInputStream(serverXml);
  }
  
  public OutputStream getContextXmlOutputStream() throws FileNotFoundException {
    return new FileOutputStream(contextXml);
  }
  
  public InputStream getContextXmlInputStream() throws FileNotFoundException {
    return new FileInputStream(contextXml);
  }
  
  public ConnectionInitialParams getConnectionInitialParams() {
    
    // TODO reload each time?
    Properties contextResourceDefaultAttrsProps = new Properties();
    Properties contextResourceLinkDefaultAttrsProps = new Properties();
    Properties serverResourceDefaultAttrsProps = new Properties();
    
    try {
      contextResourceDefaultAttrsProps.load(new InputStreamReader(
          new FileInputStream(contextResourceDefaultAttrs), "UTF-8"));
      contextResourceLinkDefaultAttrsProps.load(new InputStreamReader(
          new FileInputStream(contextResourceLinkDefaultAttrs), "UTF-8"));
      serverResourceDefaultAttrsProps.load(new InputStreamReader(
          new FileInputStream(serverResourceDefaultAttrs), "UTF-8"));
    } catch (Throwable e) {
      // fail first
      throw new RuntimeException("Failed to load connection default attributes", e);
    }
    
    return new ConnectionInitialParams() {
      @Override
      public Map<String, String> serverResourceDefaultAttrs() {
        return serverResourceDefaultAttrsProps.entrySet().stream()
            .collect(Collectors.toMap(e -> (String)e.getKey(), e -> (String)e.getValue()));
      }
      
      @Override
      public Map<String, String> contextResourceLinkDefaultAttrs() {
        return contextResourceLinkDefaultAttrsProps.entrySet().stream()
            .collect(Collectors.toMap(e -> (String)e.getKey(), e -> (String)e.getValue()));
      }
      
      @Override
      public Map<String, String> contextResourceDefaultAttrs() {
        return contextResourceDefaultAttrsProps.entrySet().stream()
            .collect(Collectors.toMap(e -> (String)e.getKey(), e -> (String)e.getValue()));
      }
    };
  }
}