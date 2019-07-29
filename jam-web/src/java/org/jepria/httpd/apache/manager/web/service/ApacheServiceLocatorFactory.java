package org.jepria.httpd.apache.manager.web.service;

public class ApacheServiceLocatorFactory {
  
  public static ApacheServiceLocator get() {
    final String osName = System.getProperty("os.name");  
    if (osName.toLowerCase().contains("windows")) {
      return new WindowsApacheServiceLocator();
    } else {
      throw new UnsupportedOperationException("Unsupported Apache service locator for the OS [" + osName + "]");
    }
  }
}
