package org.jepria.tomcat.manager.web.port;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.port.dto.PortDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.JtmPageBuilder;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;
import org.jepria.web.ssr.SsrServletBase;

public class PortSsrServlet extends SsrServletBase {
  private static final long serialVersionUID = -5897408312837631833L;
  
  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final Context context = Context.fromRequest(req);
    
    final Environment env = EnvironmentFactory.get(req);
    
    final JtmPageBuilder pageBuilder = JtmPageBuilder.newInstance(context);
    pageBuilder.setTitle("Tomcat manager: порты"); // NON-NLS
    pageBuilder.setCurrentMenuItem(CurrentMenuItem.PORT);
    
    String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    pageBuilder.setManagerApache(managerApacheHref);
    
    
    if (checkAuth(req)) {

      final List<PortDto> ports = new PortApi().list(env);
      
      PortPageContent content = new PortPageContent(context, ports);
      pageBuilder.setContent(content);
      pageBuilder.setBodyAttributes("onload", "jtm_onload();table_onload();");
      
      
      pageBuilder.setButtonLogout("port/logout"); // TODO this will erase any path- or request params of the current page
      
    } else {
      
      new AuthPageBuilder(req, "port/login", "port/logout").requireAuth(pageBuilder);
      
    }
    
    JtmPageBuilder.Page page = pageBuilder.build();
    page.respond(resp);
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
