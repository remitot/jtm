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
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.core.jdbc.ConnectionInitialParams;

/**
 * Arbitrary environment-dependent configuration parameters
 */
public class ConfigurationEnvironment {
  
  private File contextXml;
  private File serverXml;
  private ConnectionInitialParams connectionInitialParams;
  
  public ConfigurationEnvironment(HttpServletRequest request) {
    Path confPath = Paths.get(request.getServletContext().getRealPath("")).getParent().getParent().resolve("conf");
    contextXml = confPath.resolve("context.xml").toFile();
    serverXml = confPath.resolve("server.xml").toFile();
    
    // read initial params from files
    try {
      Properties properties = new Properties();
      properties.load(new InputStreamReader(new FileInputStream(new File("")), "UTF-8"));
      // TODO stopped here!
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
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
    return connectionInitialParams;
  }
}