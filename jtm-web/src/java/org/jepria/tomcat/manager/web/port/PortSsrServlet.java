package org.jepria.tomcat.manager.web.port;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.port.dto.PortDto;
import org.jepria.web.ssr.JtmPageBuilder;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.Texts;

public class PortSsrServlet extends SsrServletBase {
  private static final long serialVersionUID = -5897408312837631833L;
  
  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final Text text = Texts.get(req, "text/org_jepria_tomcat_manager_web_Text");
    
    final Environment env = EnvironmentFactory.get(req);
    
    final JtmPageBuilder pageBuilder = JtmPageBuilder.newInstance(text);
    pageBuilder.setTitle(text.getString("org.jepria.tomcat.manager.web.port.title"));
    pageBuilder.setCurrentMenuItem(CurrentMenuItem.PORT);
    
    String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    pageBuilder.setManagerApache(managerApacheHref);
    
    
    if (checkAuth(req)) {

      final List<PortDto> ports = new PortApi().list(env);
      
      PortPageContent content = new PortPageContent(text, ports);
      pageBuilder.setContent(content);
      pageBuilder.setBodyAttributes("onload", "jtm_onload();table_onload();");
      
      pageBuilder.setButtonLogout("port"); // TODO this will erase any path- or request params of the current page
      
    } else {
      
      new AuthPageBuilder(req, "port").requireAuth(pageBuilder);
      
    }
    
    JtmPageBuilder.Page page = pageBuilder.build();
    page.respond(resp);
  }
}
