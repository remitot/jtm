package org.jepria.tomcat.manager.web;

import java.io.File;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

/**
 * Factory producing appropriate {@link Environment} instances.
 * <br/><br/>
 * A general accessor method (factory method) 
 * helps to easily control the environment 
 * and switch between debug and production modes
 */
public final class EnvironmentFactory {
  
  private EnvironmentFactory() {}
  
  public static Environment get(HttpServletRequest request) {
    return new BasicEnvironment(request) {
      protected File getConfDirectory(HttpServletRequest request) {
        return Paths.get(request.getServletContext().getRealPath("")).getParent().getParent().resolve("conf2").toFile();
      }
    }; // production
  }
}
