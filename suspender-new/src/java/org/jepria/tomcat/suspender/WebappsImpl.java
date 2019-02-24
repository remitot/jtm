package org.jepria.tomcat.suspender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

/*package*/class WebappsImpl implements Webapps {

  
  /**
   * {@code CATALINA_HOME/webapps} folder
   */
  private final Path webapps;
  
  public WebappsImpl(Path webapps) {
    this.webapps = webapps;
  }
  
  @Override
  public String mapToAppUnsuspended(String uri) {
    Set<String> appsUnsus;
    try {
      appsUnsus = Files.walk(webapps).filter(path -> Files.isDirectory(path))
          .map(path -> path.getFileName().toString()).collect(Collectors.toSet());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return getMatchingAppContext(uri, appsUnsus);
  }

  @Override
  public String mapToAppSuspended(String uri) {
    Set<String> appsSus;
    try {
      appsSus = Files.walk(webapps).filter(path -> Files.isRegularFile(path) 
          && path.getFileName().toString().endsWith(".war.sus"))
          .map(path -> {
            String name = path.getFileName().toString();
            return name.substring(0, name.length() - ".war.sus".length());
          }).collect(Collectors.toSet());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return getMatchingAppContext(uri, appsSus);
  }
  
  protected String getMatchingAppContext(String requestUri, Set<String> apps) {
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
