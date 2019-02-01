package org.jepria.catalina.suspender;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Basic production environment
 */
public class BasicEnvironment implements Environment {
  
  /**
   * 'webapps' server folder
   */
  private final File webapps;
  
  /**
   * 
   * @param webapps Path to 'webapps' war-deployment folder
   */
  public BasicEnvironment(File webapps) {
    this.webapps = webapps;
  }
  
  @Override
  public Set<String> getWarAppContexts() {
    return Arrays.stream(webapps.listFiles(file -> file.isFile() && file.getName().endsWith(".war")))
        .map(file -> {
          String fileName = file.getName();
          return fileName.substring(0, fileName.length() - ".war".length());
        }).collect(Collectors.toSet());
  }
  
  @Override
  public String getMatchingDeployedAppContext(String requestUri) {
    final Set<String> deployedAppContexts = Arrays.stream(webapps.listFiles(file -> file.isDirectory()))
        .map(file -> file.getName()).collect(Collectors.toSet());
    return getMatchingAppContext(requestUri, deployedAppContexts);
  }
  
  @Override
  public String getMatchingWarSuspendedAppContext(String requestUri) {
    final Set<String> warSuspendedAppContexts = Arrays.stream(webapps.listFiles(file -> file.isFile() && file.getName().endsWith(".war.suspended")))
        .map(file -> {
          String fileName = file.getName();
          return fileName.substring(0, fileName.length() - ".war.suspended".length());
        }).collect(Collectors.toSet());
    return getMatchingAppContext(requestUri, warSuspendedAppContexts);
  }
  
  private static String getMatchingAppContext(String requestUri, Set<String> appContexts) {
    if (requestUri == null || appContexts == null || appContexts.size() == 0) {
      return null;
    }
    
    String requestContext = requestUri.replaceAll("/", "#"); 
    
    while (true) {
      if (appContexts.contains(requestContext)) {
        return requestContext;
      }
      
      int lastHash = requestContext.lastIndexOf('#');
      if (lastHash != -1) {
        requestContext = requestContext.substring(0, lastHash);
      } else {
        break;
      }
    }
    
    return null;
  }
  
  @Override
  public File getWar(String appContext) {
    return new File(webapps, appContext + ".war");
  }
  
  @Override
  public File getWarSuspended(String appContext) {
    return new File(webapps, appContext + ".war.suspended");
  }
  
  @Override
  public File getDeployedDirectory(String appContext) {
    return new File(webapps, appContext);
  }
}