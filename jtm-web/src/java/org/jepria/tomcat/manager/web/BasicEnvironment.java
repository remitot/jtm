package org.jepria.tomcat.manager.web;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.core.jdbc.ResourceInitialParams;

/**
 * Basic production environment
 */
public class BasicEnvironment implements Environment {
  
  private final Path home;
  
  private final EnvironmentPropertyFactory envPropertyFactory;
  
  public BasicEnvironment(HttpServletRequest request) {
    // TODO avoid using getRealPath 
    envPropertyFactory = new EnvironmentPropertyFactory(new File(request.getServletContext().getRealPath("/WEB-INF/app-conf-default.properties")));
    
    home = Paths.get(System.getProperty("catalina.home"));
  }
  
  @Override
  public Path getHomeDirectory() {
    return home;
  }
  
  @Override
  public String getProperty(String name) {
    return envPropertyFactory.getProperty(name);
  }
  
  @Override
  public ResourceInitialParams getResourceInitialParams() {
    
    return new ResourceInitialParams() {
      
      private Map<String, String> getPropertyList(String propertyListName) {
        final Map<String, String> ret = new HashMap<>();
        String names = getProperty(propertyListName);
        if (names != null) {
          String[] namesSplit = names.split("\\s*,\\s*");
          for (String name: namesSplit) {
            if (name != null) {
              String value = getProperty(name);
              if (value != null) {
                if (name.startsWith(propertyListName + ".")) {
                  name = name.substring(propertyListName.length() + 1);
                }
                ret.put(name, value);
              }
            }
          }
        }
        return ret;
      }
      
      @Override
      public Map<String, String> getServerResourceAttrs() {
        return getPropertyList("org.jepria.tomcat.manager.web.jdbc.initial_attrs.ServerResource");
      }
      
      @Override
      public Map<String, String> getContextResourceLinkAttrs() {
        return getPropertyList("org.jepria.tomcat.manager.web.jdbc.initial_attrs.ContextResourceLink");
      }
      
      @Override
      public Map<String, String> getContextResourceAttrs() {
        return getPropertyList("org.jepria.tomcat.manager.web.jdbc.initial_attrs.ContextResource");
      }
      
      @Override
      public String getJdbcProtocol() {
        return getProperty("org.jepria.tomcat.manager.web.jdbc.protocol"); 
      }
    };
  }
}