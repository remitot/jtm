package org.jepria.tomcat.suspender;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent e) {
  }

  @Override
  public void contextInitialized(ServletContextEvent e) {
    String appContextName = getAppName(e.getServletContext().getContextPath());
    ContextLoadAwaiter.contextLoaded(appContextName);
  }

  protected String getAppName(String contextPath) {
    if (contextPath != null) {
      if (contextPath.startsWith("/")) {
        contextPath = contextPath.substring(1);
        return contextPath.replaceAll("/", "#");
      }
    }
    return null;
  }
}
