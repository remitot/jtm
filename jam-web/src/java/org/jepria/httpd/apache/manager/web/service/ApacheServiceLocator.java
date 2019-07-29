package org.jepria.httpd.apache.manager.web.service;

public interface ApacheServiceLocator {
  ApacheService get(String serviceName);
}
