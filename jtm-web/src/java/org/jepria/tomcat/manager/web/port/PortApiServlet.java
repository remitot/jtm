package org.jepria.tomcat.manager.web.port;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.port.dto.PortDto;

public class PortApiServlet extends HttpServlet {

  private static final long serialVersionUID = 2791033129244689227L;

  private static void portHttp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("text/plain; charset=UTF-8");
    
    try {
      
      final Environment environment = EnvironmentFactory.get(req);
      
      final PortDto port = new PortApi().portHttp(environment);

      if (port == null) {
        // the server is requred to have its HTTP port defined
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.flushBuffer();
        return;
        
      } else {
        
        resp.getWriter().print(port.get("number"));
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
        return;
      }

    } catch (Throwable e) {
      e.printStackTrace();

      // response body must either be empty or match the declared content type
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }
  }
  
  private static void portAjp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("text/plain; charset=UTF-8");
    
    try {
      
      final Environment environment = EnvironmentFactory.get(req);
      
      final PortDto port = new PortApi().portAjp(environment);

      if (port == null) {
        // the server is requred to have its AJP port defined
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.flushBuffer();
        return;
        
      } else {
        
        resp.getWriter().print(port.get("number"));
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
        return;
      }

    } catch (Throwable e) {
      e.printStackTrace();

      // response body must either be empty or match the declared content type
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    String path = req.getPathInfo();
    
    if ("/http".equals(path)) {
      portHttp(req, resp);
      return;
      
    } else if ("/ajp".equals(path)) {
      portAjp(req, resp);
      return;
      
    } else {
      
      // TODO set content type for the error case?
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
}
