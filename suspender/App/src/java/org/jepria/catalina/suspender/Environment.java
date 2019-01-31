package org.jepria.catalina.suspender;

import java.nio.file.Path;
import java.util.List;

/**
 * Environment-dependent configuration parameters
 */
public interface Environment {
  
  /**
   * @param appContextName
   * @return a war for the application context name (possibly non-existing yet)
   */
  Path getWar(String appContextName);
  
  /**
   * @param appContextName
   * @return a suspended war for the application context name (possibly non-existing yet)
   */
  Path getWarSus(String appContextName);
  
  /**
   * @param appContextName
   * @return a deployed application folder for the application context name (possibly non-existing yet)
   */
  Path getDeployedApp(String appContextName);
  
  /**
   * @return list of Paths for all {@code .war} files within the the war-folder, or an empty list
   */
  List<Path> listWars();
}