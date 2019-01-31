package org.jepria.catalina.suspender;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Basic production environment
 */
public class BasicEnvironment implements Environment {
  
  /**
   * Path to 'webapps' war-deployment folder
   */
  private final Path webapps;
  
  /**
   * 
   * @param webapps Path to 'webapps' war-deployment folder
   */
  public BasicEnvironment(Path webapps) {
    this.webapps = webapps;
  }
  
  @Override
  public Path getWar(String appContextName) {
    return webapps.resolve(appContextName + ".war");
  }
  
  @Override
  public Path getWarSus(String appContextName) {
    return webapps.resolve(appContextName + ".war.suspended");
  }
  
  @Override
  public Path getDeployedApp(String appContextName) {
    return webapps.resolve(appContextName);
  }
  
  @Override
  public List<Path> listWars() {
    return Arrays.stream(webapps.toFile().listFiles(file -> file.isFile() && file.getName().endsWith(".war")))
        .map(file -> file.toPath()).collect(Collectors.toList());
  }
}