package org.jepria.tomcat.manager.web.port;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.HtmlPage;
import org.jepria.tomcat.manager.web.HtmlPageForbidden;
import org.jepria.tomcat.manager.web.HtmlPageUnauthorized;
import org.jepria.tomcat.manager.web.SsrServletBase;
import org.jepria.tomcat.manager.web.port.dto.PortDto;
import org.jepria.web.ssr.ForbiddenFragment;
import org.jepria.web.ssr.LoginFragment;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;

public class PortSsrServlet extends SsrServletBase {
  private static final long serialVersionUID = -5897408312837631833L;
  
  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    final Environment env = EnvironmentFactory.get(req);
    
    final HtmlPage htmlPage;
    
    final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    
    if (checkAuth(req)) {

      final List<PortDto> ports = new PortApi().list(env);
      
      htmlPage = new PortHtmlPage(ports);
      
      final PageHeader pageHeader = new PageHeader(CurrentMenuItem.PORT); // TODO this will erase any path- or request params of the current page
      pageHeader.setManagerApache(managerApacheHref);
      pageHeader.setButtonLogout("port/logout"); // TODO this will erase any path- or request params of the current page
  
      htmlPage.setPageHeader(pageHeader);
      
    } else {
      
      final PageHeader pageHeader = new PageHeader(CurrentMenuItem.PORT);
      pageHeader.setManagerApache(managerApacheHref);
      
      AuthInfo authInfo = requireAuth(req, "port/login", "port/logout"); // TODO this will erase any path- or request params of the current page
      
      if (authInfo.authFragment instanceof LoginFragment) {
        htmlPage = new HtmlPageUnauthorized((LoginFragment)authInfo.authFragment);
        htmlPage.setStatusBar(authInfo.statusBar);
      } else if (authInfo.authFragment instanceof ForbiddenFragment) {
        htmlPage = new HtmlPageForbidden((ForbiddenFragment)authInfo.authFragment);
        pageHeader.setButtonLogout("port/logout"); // TODO this will erase any path- or request params of the current page
        htmlPage.setStatusBar(authInfo.statusBar);
      } else {
        // TODO
        throw new IllegalStateException();
      }
      
      htmlPage.setTitle(PortHtmlPage.PAGE_TITLE);
      htmlPage.setPageHeader(pageHeader);
      
    }

    htmlPage.respond(resp);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    final String path = req.getPathInfo();
    
    if ("/login".equals(path)) {
      
      login(req);
      
      // port/login -> port
      // Note: sendRedirect("abc") will redirect to 'manager-ext/port/abc' 
      // sendRedirect(".") will redirect to 'manager-ext/port/'
      // sendRedirect("") will stay here 'manager-ext/port/login'
      resp.sendRedirect("../port"); // TODO
      return;
      
    } else if ("/logout".equals(path)) {
      
      logout(req);
      
      // port/logout -> port
      resp.sendRedirect("../port"); // TODO
      return;
        
    }
    
  }

}
