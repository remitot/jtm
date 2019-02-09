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
   */
  protected File getConfDirectory(HttpServletRequest request) {
    return new File(request.getServletContext().getInitParameter("org.jepria.httpd.apache.manager.web.confDirectory"));
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
      throw new RuntimeException(e);//TODO?
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
}