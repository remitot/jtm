package org.jepria.tomcat.manager.web.port;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.HtmlPage;
import org.jepria.tomcat.manager.web.SsrServletBase;
import org.jepria.tomcat.manager.web.port.dto.PortDto;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;

public class PortSsrServlet extends SsrServletBase {
  private static final long serialVersionUID = -5897408312837631833L;
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (checkAuth(req)) {
      
      final Environment env = EnvironmentFactory.get(req);
      
      final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
      final PageHeader pageHeader = new PageHeader(managerApacheHref, "port/logout", CurrentMenuItem.PORT); // TODO this will erase any path- or request params of the current page

      final List<PortDto> ports = new PortApi().list(env);
      
      final HtmlPage htmlPage = new PortHtmlPage(ports);
      htmlPage.setPageHeader(pageHeader);
  
      htmlPage.respond(resp);
      
    } else {
      
      doLogin(req, resp, "port/login", PortHtmlPage.PAGE_TITLE, CurrentMenuItem.PORT);
      
    }
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    final String path = req.getPathInfo();
    
    if ("/login".equals(path)) {
      
      login(req);
      
      // port/login -> port
      // "abc" will lead to 'manager-ext/port/abc' 
      // "." will lead to 'manager-ext/port/'
      // "" will remain here 'manager-ext/port/login'
      resp.sendRedirect("../port"); // TODO
      return;
      
    } else if ("/logout".equals(path)) {
      
      logout(req);
      
      // port/logout -> port
      resp.sendRedirect(".."); // TODO
      return;
        
    }
    
  }

}
