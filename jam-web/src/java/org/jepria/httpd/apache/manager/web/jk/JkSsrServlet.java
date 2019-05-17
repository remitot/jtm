package org.jepria.httpd.apache.manager.web.jk;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.httpd.apache.manager.web.JamPageHeader;
import org.jepria.httpd.apache.manager.web.JamPageHeader.CurrentMenuItem;
import org.jepria.httpd.apache.manager.web.jk.dto.BindingDto;
import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
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
    
    
    final String detailsId = req.getParameter("id");
    final boolean showDetails = detailsId != null && !"".equals(detailsId);
    
    
    final PageHeader pageHeader;
    if (showDetails) {
      pageHeader = new JamPageHeader(text, CurrentMenuItem.JK_DETAILS);
    } else {
      pageHeader = new JamPageHeader(text, CurrentMenuItem.JK);
    }
    pageBuilder.setHeader(pageHeader);
    
    if (checkAuth(req)) {

      pageHeader.setButtonLogout(req);
      
      if (showDetails) {
        // show details for JkMount by id from request param
        
        BindingDto binding = new JkApi().getBinding(env, detailsId);
        JkPageContent content = new JkPageContent(text, binding);
        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();");
        
      } else {
        // show table
        
        final List<JkMountDto> jkMounts = new JkApi().getJkMounts(env);
        
        JkPageContent content = new JkPageContent(text, jkMounts);
        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();");
      }
      
    } else {
      
      requireAuth(req, pageBuilder);
      
    }
    
    HtmlPageExtBuilder.Page page = pageBuilder.build();
    page.respond(resp);
  }
  
  private static String lookupTomcatManagerPath(Environment environment, String host, int port) {
    String tomcatManagerPath = environment.getProperty("org.jepria.httpd.apache.manager.web.TomcatManager." + host + "." + port + ".path");
    if (tomcatManagerPath == null) {
      tomcatManagerPath = environment.getProperty("org.jepria.httpd.apache.manager.web.TomcatManager.default.path");
      if (tomcatManagerPath == null) {
        throw new RuntimeException("Misconfiguration exception: "
            + "mandatory configuration property \"org.jepria.httpd.apache.manager.web.TomcatManager.default.path\" is not defined");
      }
    }
    return tomcatManagerPath;
  }
}
