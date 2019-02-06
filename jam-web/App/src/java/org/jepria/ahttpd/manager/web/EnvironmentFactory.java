package org.jepria.ahttpd.manager.web;

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
    return new BasicEnvironment(request); // production
  }
}
