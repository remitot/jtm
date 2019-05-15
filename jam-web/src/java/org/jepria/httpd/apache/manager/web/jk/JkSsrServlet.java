package org.jepria.httpd.apache.manager.web.jk;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.httpd.apache.manager.web.JamPageHeader;
import org.jepria.httpd.apache.manager.web.JamPageHeader.CurrentMenuItem;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.Texts;

public class JkSsrServlet extends SsrServletBase {

  private static final long serialVersionUID = -5587074686993550317L;
  
  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final Text text = Texts.get(req, "text/org_jepria_httpd_apache_manager_web_Text");
    
    final Environment env = EnvironmentFactory.get(req);
    
    final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(text);
    pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.jk.title"));
    
    final PageHeader pageHeader = new JamPageHeader(text, CurrentMenuItem.JK);
    pageBuilder.setHeader(pageHeader);
    
    if (checkAuth(req)) {

      El div = new El("div");
      div.setAttribute("style", "width:100px;height:100px;background-color:green;");
      
//      final List<PortDto> ports = new PortApi().list(env);
//      
//      PortPageContent content = new PortPageContent(text, ports);
      pageBuilder.setContent(div);
//      pageBuilder.setBodyAttributes("onload", "jtm_onload();table_onload();");
      
      pageHeader.setButtonLogout("jk"); // TODO this will erase any path- or request params of the current page
      
    } else {
      
      requireAuth(req, pageBuilder);
      
    }
    
    HtmlPageExtBuilder.Page page = pageBuilder.build();
    page.respond(resp);
  }
}
