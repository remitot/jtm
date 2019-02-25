package org.jepria.tomcat.suspender.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

/*package*/class AppMapperImpl implements AppMapper {

  
  /**
   * {@code CATALINA_HOME/webapps} folder
   */
  private final Path webapps;
  
  public AppMapperImpl(Path webapps) {
    this.webapps = webapps;
  }
  
  @Override
  public String mapToAppAlive(String uri) {
    final Set<String> apps;
    try {
      apps = Files.walk(webapps).filter(path -> Files.isDirectory(path))
          .map(path -> path.getFileName().toString()).collect(Collectors.toSet());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return getMatchingApp(uri, apps);
  }

  @Override
  public String mapToAppSuspended(String uri) {
    final Set<String> apps;
    try {
      apps = Files.walk(webapps).filter(path -> Files.isRegularFile(path) 
          && path.getFileName().toString().endsWith(".war.sus"))
          .map(path -> {
            String name = path.getFileName().toString();
            return name.substring(0, name.length() - ".war.sus".length());
          }).collect(Collectors.toSet());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return getMatchingApp(uri, apps);
  }
  
  protected String getMatchingApp(String requestUri, Set<String> apps) {
    if (requestUri == null || apps == null || apps.size() == 0) {
      return null;
    }
    
    String requestContext = requestUri.replaceAll("/", "#"); 
    
    while (true) {
      if (apps.contains(requestContext)) {
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

}
