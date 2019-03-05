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
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.table.Table.TabIndex;

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
        // table html
        final List<ConnectionDto> connections = new JdbcApi().list(EnvironmentFactory.get(req));
        
        final JdbcTable table = new JdbcTable();
        table.load(connections);
        
        final String tableHtml = table.printHtml();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableHtml", tableHtml);
        
        // table script
        final String tableScript = table.printScripts();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableScript", tableScript);
        
        // table style
        final String tableStyle = table.printStyles();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableStyle", tableStyle);
        
        // table row-create template
        final TabIndex rowCreateTabIndex = new TabIndex() {
          private int i = 0;
          @Override
          public void setNext(El el) {
            el.classList.add("has-tabindex-rel");
            el.setAttribute("tabindex-rel", i++);
          }
        };
        final String tableRowCreateHtml = table.createRowCreate(rowCreateTabIndex).printHtml();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableRowCreateHtml", tableRowCreateHtml);
        
        // control buttons
        ControlButtons controlButtons = new ControlButtons();
        
        final String controlButtonsHtml = controlButtons.printHtml();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.controlButtonsHtml", controlButtonsHtml);
        
        final String controlButtonsScript = controlButtons.printScripts();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.controlButtonsScript", controlButtonsScript);
        
        
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
