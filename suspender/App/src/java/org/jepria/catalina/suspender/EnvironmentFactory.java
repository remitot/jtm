package org.jepria.catalina.suspender;

import java.nio.file.Path;

/**
 * Factory producing appropriate {@link Environment} instances.
 * <br/><br/>
 * A general accessor method (factory method) 
 * helps to easily control the environment 
 * and switch between debug and production modes
 */
public final class EnvironmentFactory {
  
  private EnvironmentFactory() {}
  
  public static Environment get(Path webapps) {
    return new BasicEnvironment(webapps);
  }
}