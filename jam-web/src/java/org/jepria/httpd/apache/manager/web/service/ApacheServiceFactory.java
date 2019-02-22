package org.jepria.httpd.apache.manager.web.service;

import java.util.Objects;

public class ApacheServiceFactory {
  /**
   * 
   * @param serviceName Apache service name
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
}
