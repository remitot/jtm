package org.jepria.catalina.suspender;

import java.io.File;
import java.util.Set;

/**
 * Environment-dependent configuration parameters
 */
public interface Environment {
  
  Set<String> getWarAppContexts();
  
  String getMatchingDeployedAppContext(String requestUri);
  
  String getMatchingWarSuspendedAppContext(String requestUri);
  
  /**
   * @param appContext
   * @return may not exist
   */
  File getWar(String appContext);
  
  /**
   * @param appContext
   * @return may not exist
   */
  File getWarSuspended(String appContext);
  
  /**
   * @param appContext
   * @return may not exist
   */
  File getDeployedDirectory(String appContext);
}