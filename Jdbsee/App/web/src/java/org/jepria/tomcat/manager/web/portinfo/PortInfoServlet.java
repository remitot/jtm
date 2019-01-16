package org.jepria.tomcat.manager.web.portinfo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.core.portinfo.TomcatConfPortInfo;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;

public class PortInfoServlet extends HttpServlet {

  private static final long serialVersionUID = 2791033129244689227L;

  private void ajp13(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    getConnectorPort(req, resp, "AJP/1.3");
  }
  
  
  private void http11(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    getConnectorPort(req, resp, "HTTP/1.1");
  }
  
  private void getConnectorPort(HttpServletRequest req, HttpServletResponse resp, String portName) throws ServletException, IOException {
    try {
      
      Environment environment = EnvironmentFactory.get(req);
      
      TomcatConfPortInfo tomcatConf = new TomcatConfPortInfo(environment.getContextXmlInputStream(), 
          environment.getServerXmlInputStream());
      
      String port = tomcatConf.getConnectorPort(portName);
      
      if (port != null) {
        resp.getOutputStream().print(port);
      }

    } catch (Throwable e) {
      e.printStackTrace();

      resp.getOutputStream().println("Oops! Something went wrong.");//TODO
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }

    resp.setContentType("text/plain");
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.flushBuffer();
    return;
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    resp.setContentType("application/json; charset=UTF-8");
    
    String path = req.getPathInfo();
    
    if ("/ajp13".equals(path)) {
      ajp13(req, resp);
      return;
      
    } else if ("/http11".equals(path)) {
      http11(req, resp);
      return;
      
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
}
