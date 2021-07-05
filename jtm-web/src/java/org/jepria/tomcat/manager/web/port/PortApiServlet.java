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

  protected void portHttp(HttpServletRequest req, HttpServletResponse response) throws IOException {
    
    // the content type is defined for the entire method
    response.setContentType("text/plain; charset=UTF-8");
    
    try {
      
      final Environment environment = EnvironmentFactory.get(req);
      
      final PortDto port = new PortApi().portHttp(environment);

      if (port == null) {
        // the server is requred to have its HTTP port defined
        throw new IllegalStateException();

      } else {

        response.getWriter().print(port.getNumber());

        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
        return;
      }

    } catch (Throwable e) {
      final String errorId = String.valueOf(System.currentTimeMillis());

      synchronized (System.err) {
        System.err.println("Error ID [" + errorId + "]:");
        e.printStackTrace();
      }

      // response body must either be empty or match the declared content type
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error ID [" + errorId + "]");
      response.flushBuffer();
      return;
    }
  }
  
  protected void portAjp(HttpServletRequest req, HttpServletResponse response) throws IOException {
    
    // the content type is defined for the entire method
    response.setContentType("text/plain; charset=UTF-8");
    
    try {
      
      final Environment environment = EnvironmentFactory.get(req);
      
      final PortDto port = new PortApi().portAjp(environment);

      if (port == null) {
        // the server is required to have its AJP port defined
        throw new IllegalStateException();

      } else {

        response.getWriter().print(port.getNumber());

        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
        return;
      }

    } catch (Throwable e) {
      final String errorId = String.valueOf(System.currentTimeMillis());

      synchronized (System.err) {
        System.err.println("Error ID [" + errorId + "]:");
        e.printStackTrace();
      }

      // response body must either be empty or match the declared content type
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error ID [" + errorId + "]");
      response.flushBuffer();
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
      // unknown request
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported request path [" + path + "]");
      resp.flushBuffer();
      return;
    }
  }
}
