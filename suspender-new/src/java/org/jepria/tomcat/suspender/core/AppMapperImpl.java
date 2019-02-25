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
  
  protected String getMatchingApp(String uri, Set<String> apps) {
    if (uri == null) {
      return null;
    }
    
    if (uri.startsWith("/")) {
      uri = uri.substring(1);
    }
    
    String uri1 = uri.replaceAll("/", "#"); 
    
    while (true) {
      if (apps.contains(uri1)) {
        return uri1;
      }
      
      int lastHash = uri1.lastIndexOf('#');
      if (lastHash != -1) {
        uri1 = uri1.substring(0, lastHash);
      } else {
        break;
      }
    }
    
    return null;
  }

}
