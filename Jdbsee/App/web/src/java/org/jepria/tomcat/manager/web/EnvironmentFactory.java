package org.jepria.tomcat.manager.web;

import java.io.File;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;

/**
 * Factory that produces appropriate Environment instances.
 * <br/><br/>
 * A general accessor method (factory method) 
 * helps to easily control the environment 
 * and switch between debug and production modes
 */
public final class EnvironmentFactory {
  
  private EnvironmentFactory() {}
  
  public static Environment get(HttpServletRequest request) {
    return new BasicEnvironment(request) {
      @Override
      protected Path getConfPath(HttpServletRequest request) {
        return new File("/home/roma/Desktop/conf-debug").toPath();
      }
    };
  }
}
