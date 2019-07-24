package org.jepria.httpd.apache.manager.web.service;

public class ApacheServiceFactory {
  /**
   * 
   * @param serviceName Apache service name
   * @return
   */
  public static ApacheService get(String serviceName) {
    final String osName = System.getProperty("os.name");  
    if (osName.toLowerCase().contains("windows")) {
      return new WindowsService(serviceName);
    } else {
      throw new UnsupportedOperationException("Unsupported for the current OS: " + osName);
    }
  }
}
