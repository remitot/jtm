package org.jepria.httpd.apache.manager.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * Basic production environment
 */
public class BasicEnvironment implements Environment {
  
  private final File mod_jk_conf;
  private final File workers_properties;
  
  /**
   * @param request
   * @return Apache HTTPD 'conf' directory
   * @throws ConfigurationException 
   */
  protected File getConfDirectory(HttpServletRequest request) {
    final String confDirectory = request.getServletContext().getInitParameter("org.jepria.httpd.apache.manager.web.confDirectory");
    if (confDirectory == null) {
      throw new RuntimeException("Misconfiguration exception: "
          + "org.jepria.httpd.apache.manager.web.confDirectory context-param not specified in web.xml");
    }
    final File confDirectoryFile = new File(confDirectory);
    if (!confDirectoryFile.isDirectory() || !confDirectoryFile.canRead()) {
      throw new RuntimeException("Misconfiguration exception: "
          + "org.jepria.httpd.apache.manager.web.confDirectory context-param value does not represent an existing readable directory");
    }
    return new File(confDirectory);
  }
  
  public BasicEnvironment(HttpServletRequest request) {
    File confDir = getConfDirectory(request);
    
    mod_jk_conf = confDir.toPath().resolve("jk").resolve("mod_jk.conf").toFile();
    workers_properties = confDir.toPath().resolve("jk").resolve("workers.properties").toFile();
  }
  
  @Override
  public OutputStream getMod_jk_confOutputStream() {
    try {
      return new FileOutputStream(mod_jk_conf);
    } catch (FileNotFoundException e) {
      throw misconfiguration("");
    }
  }
  
  @Override
  public InputStream getMod_jk_confInputStream() {
    try {
      return new FileInputStream(mod_jk_conf);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public OutputStream getWorkers_propertiesOutputStream() {
    try {
      return new FileOutputStream(workers_properties);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public InputStream getWorkers_propertiesInputStream() {
    try {
      return new FileInputStream(workers_properties);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  protected static RuntimeException misconfiguration(String message) {
    throw new RuntimeException("Misconfiguration exception" 
        + (message == null ? "" : (": " + message)));
  }
}