package org.jepria.httpd.apache.manager.web;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Basic production environment
 */
public class BasicEnvironment implements Environment {
  
  private final EnvironmentPropertyFactory envPropertyFactory;
  
  public BasicEnvironment(HttpServletRequest request) {
    envPropertyFactory = new EnvironmentPropertyFactory(new File(request.getServletContext().getRealPath("/WEB-INF/app-conf-default.properties")));
  }
  
  @Override
  public String getProperty(String name) {
    return envPropertyFactory.getProperty(name);
  }
  
  @Override
  public Path getHomeDirectory() {

    Path path;

    final String apacheHomeEnv = getProperty("org.jepria.httpd.apache.manager.web.apacheHome");
    if (apacheHomeEnv == null) {
      throw new RuntimeException("Misconfiguration exception: "
              + "mandatory configuration property \"org.jepria.httpd.apache.manager.web.apacheHome\" is not defined");
    }

    path = Paths.get(apacheHomeEnv);

    if (path == null || !Files.isDirectory(path)) {
      throw new RuntimeException("Misconfiguration exception: "
              + "the configuration property \"org.jepria.httpd.apache.manager.web.apacheHome\" does not represent a directory: "
              + "[" + path + "]");
    }

    return path;
  }
}