package org.jepria.tomcat.manager.web.jdbc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.ssr.JdbcTable;
import org.jepria.web.ssr.table.Table;

/**
 * Servlet producing server-side-rendered pages
 */
public class JdbcSsrServlet extends HttpServlet {

  private static final long serialVersionUID = -2556094883694667549L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    final String path = req.getPathInfo();
    
    if (path == null || "".equals(path) || "/".equals(path)) {
      
      try {
        // get table html
        final List<ConnectionDto> connections = new JdbcApi().list(EnvironmentFactory.get(req));
        
        final Table<ConnectionDto> table = new JdbcTable();
        table.load(connections);
        
        final String tableHtml = table.printHtml();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableHtml", tableHtml);
        
        
        // get table script
        final String tableScript = table.printScript();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableScript", tableScript);
        
        // forward to the target page
        req.getRequestDispatcher("/gui/jdbc-ssr/jdbc-ssr-target.jsp").forward(req, resp);
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
