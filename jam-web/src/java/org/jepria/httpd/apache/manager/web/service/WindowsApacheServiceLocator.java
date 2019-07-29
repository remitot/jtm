package org.jepria.httpd.apache.manager.web.service;

public class WindowsApacheServiceLocator implements ApacheServiceLocator {

  @Override
  public ApacheService get(String serviceName) {
    return new WindowsService(serviceName);
  }

}
