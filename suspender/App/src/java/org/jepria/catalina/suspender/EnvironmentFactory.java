package org.jepria.catalina.suspender;

import java.io.File;

/**
 * Factory producing appropriate {@link Environment} instances.
 * <br/><br/>
 * A general accessor method (factory method) 
 * helps to easily control the environment 
 * and switch between debug and production modes
 */
public final class EnvironmentFactory {
  
  private EnvironmentFactory() {}
  
  public static Environment get(File webapps) {
    return new BasicEnvironment(webapps);
  }
}