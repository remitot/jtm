package org.jepria.tomcat.manager.web.port;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.JtmPageHeader;
import org.jepria.tomcat.manager.web.JtmPageHeader.CurrentMenuItem;
import org.jepria.tomcat.manager.web.port.dto.PortDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.Text;

public class PortSsrServlet extends SsrServletBase {
  private static final long serialVersionUID = -5897408312837631833L;
  
  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    Context context = Context.get(req, "text/org_jepria_tomcat_manager_web_Text");
    Text text = context.getText();
    
    
    final Environment env = EnvironmentFactory.get(req);
    
    final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(context);
    pageBuilder.setTitle(text.getString("org.jepria.tomcat.manager.web.port.title"));
    
    String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    
    final PageHeader pageHeader = new JtmPageHeader(context, managerApacheHref, CurrentMenuItem.PORT);
    pageBuilder.setHeader(pageHeader);
    
    
    if (checkAuth(req)) {
      pageHeader.setButtonLogout(req);
      
      final List<PortDto> ports = new PortApi().list(env);
      
      PortPageContent content = new PortPageContent(context, ports);
      pageBuilder.setContent(content);
      
    } else {
      
      requireAuth(req, pageBuilder);
      
    }
    
    HtmlPageExtBuilder.Page page = pageBuilder.build();
    page.respond(resp);
  }
}
