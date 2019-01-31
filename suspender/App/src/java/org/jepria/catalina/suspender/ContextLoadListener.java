package org.jepria.catalina.suspender;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class ContextLoadListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent e) {
  }

  @Override
  public void contextInitialized(ServletContextEvent e) {
    String appContextName = getAppContextName(e);
    ContextLoadAwaiter.contextLoaded(appContextName);
  }
  
  
  
  private String getAppContextName(ServletContextEvent e) {
    String contextPath = e.getServletContext().getContextPath();
    if (contextPath.startsWith("/")) {
      return contextPath.split("/")[1];
    } else {
      return "";
    }
  }
}