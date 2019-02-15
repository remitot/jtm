package org.jepria.httpd.apache.manager.web.service;

import java.util.Objects;

import javax.servlet.ServletContext;

public class ApacheServiceFactory {
  
  /**
   * 
   * @param serviceName Apache HTTPD service name
   * @return
   */
  public static ApacheService get(String serviceName) {
    final String osName = System.getProperty("os.name");  
    if (osName.toLowerCase().contains("windows")) {
      Objects.requireNonNull(serviceName);
      return new WindowsService(serviceName);
    } else {
      throw new UnsupportedOperationException("Unsupported for the current OS: " + osName);
    }
  }
  
  /**
   * 
   * @param context to get the serviceName from
   * @return
   */
  public static ApacheService get(ServletContext context) {
    final String serviceName = context.getInitParameter("org.jepria.httpd.apache.manager.web.apacheHttpdServiceName");
    
    if (serviceName == null) {
      throw new RuntimeException("Misconfiguration exception: "
          + " mandatory org.jepria.httpd.apache.manager.web.apacheHttpdServiceName context-param is not specified in web.xml");
    }
    
    return get(serviceName);
  }
}
