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
  
  private final File modjkConf;
  private final File workerProperties;
  
  /**
   * @param request
   * @return Apache HTTPD 'conf' directory
   */
  protected File getConfDirectory(HttpServletRequest request) {
    return new File(request.getServletContext().getInitParameter("org.jepria.httpd.apache.manager.web.apacheHttpdConfDirectory"));
  }
  
  public BasicEnvironment(HttpServletRequest request) {
    File confDir = getConfDirectory(request);
    
    modjkConf = confDir.toPath().resolve("jk").resolve("mod_jk.conf").toFile();
    workerProperties = confDir.toPath().resolve("jk").resolve("workers.properties").toFile();
  }
  
  @Override
  public OutputStream getModjkConfOutputStream() {
    try {
      return new FileOutputStream(modjkConf);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public InputStream getModjkConfInputStream() {
    try {
      return new FileInputStream(modjkConf);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public OutputStream getWorkerPropertiesOutputStream() {
    try {
      return new FileOutputStream(workerProperties);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public InputStream getWorkerPropertiesInputStream() {
    try {
      return new FileInputStream(workerProperties);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
}