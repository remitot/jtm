package org.jepria.httpd.apache.manager.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.web.service.ApacheServiceFactory;

public class RestartServlet extends HttpServlet {

  private static final long serialVersionUID = -5406772704670572455L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doPost(req, resp);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    final String apacheServiceName = EnvironmentFactory.get(req).getProperty("org.jepria.httpd.apache.manager.web.apacheServiceName");
    
    // restart the Apache service
    ApacheServiceFactory.get(apacheServiceName).restart();
    
  }
}
