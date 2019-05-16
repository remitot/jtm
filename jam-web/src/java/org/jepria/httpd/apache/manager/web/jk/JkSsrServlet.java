package org.jepria.httpd.apache.manager.web.jk;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.httpd.apache.manager.web.JamPageHeader;
import org.jepria.httpd.apache.manager.web.JamPageHeader.CurrentMenuItem;
import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
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

      pageHeader.setButtonLogout(req);
      
      String detailsId = req.getParameter("id");
      
      if (detailsId == null || "".equals(detailsId)) {
        // show table
        
        final List<JkMountDto> jkMounts = new JkApi().getJkMounts(env);
        
        JkPageContent content = new JkPageContent(text, jkMounts);
        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();");
        
      } else {
        // show details for JkMount by id from request param
        
        String mountId = detailsId;
        if (mountId.startsWith("/")) {
          mountId = mountId.substring(1);
        }
        if (mountId.endsWith("/")) {
          mountId = mountId.substring(0, mountId.length() - 1);
        }
        
        El con = new El("label").setInnerHTML("details here for [" +mountId+ "]...");
        pageBuilder.setContent(Arrays.asList(con));
      }
      
    } else {
      
      requireAuth(req, pageBuilder);
      
    }
    
    HtmlPageExtBuilder.Page page = pageBuilder.build();
    page.respond(resp);
  }
}
