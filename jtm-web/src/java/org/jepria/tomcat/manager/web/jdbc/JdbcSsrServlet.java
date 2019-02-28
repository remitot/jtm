package org.jepria.tomcat.manager.web.jdbc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.ssr.JdbcRenderer;

public class JdbcSsrServlet extends HttpServlet {

  private static final long serialVersionUID = -2556094883694667549L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    String path = req.getPathInfo();
    
    if ("/table".equals(path) || "/table/html".equals(path)) {
      
      try {
        final List<ConnectionDto> connections = new JdbcApi().list(req);
        
        final String responseBody = new JdbcRenderer().tableHtml(connections);
        
        // write response
        if (responseBody != null) {
          resp.setContentType("text/html; charset=UTF-8");
          resp.getWriter().print(responseBody);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
        return;
        
      } catch (Throwable e) {
        e.printStackTrace();

        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.flushBuffer();
        return;
      }

    } else if ("/table/js".equals(path)) {
        
        try {
          final String responseBody = new JdbcRenderer().tableJs();
          
          // write response
          if (responseBody != null) {
            resp.setContentType("application/javascript; charset=UTF-8");
            resp.getWriter().print(responseBody);
          }
          resp.setStatus(HttpServletResponse.SC_OK);
          resp.flushBuffer();
          return;
          
        } catch (Throwable e) {
          e.printStackTrace();

          resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          resp.flushBuffer();
          return;
        }

    } else {
      
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
  
}
