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
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.Text;

public class JkSsrServlet extends SsrServletBase {

  private static final long serialVersionUID = -5587074686993550317L;
  
  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    Context context = Context.get(req, "text/org_jepria_httpd_apache_manager_web_Text");
    Text text = context.getText();
    
    final Environment env = EnvironmentFactory.get(req);
    
    final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(context);
    pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.jk.title"));
    

    final String mountId;
    final boolean details;
    final boolean list;
    final boolean newBinding;
    
    
    final String path = req.getPathInfo();
    
    if (path == null || "/".equals(path) || "".equals(path)) {
      mountId = null;
      details = newBinding = false;
      list = true;
    } else {
      if ("/new-binding".equals(path)) {
        mountId = null;
        details = list = false;
        newBinding = true;
      } else {
        mountId = path.substring("/".length());
        list = newBinding = false;
        details = true;
      }
    }
  
    
    
    final PageHeader pageHeader;
    if (details) {
      pageHeader = new JamPageHeader(context, CurrentMenuItem.JK_DETAILS);
    } else if (newBinding) {
      pageHeader = new JamPageHeader(context, CurrentMenuItem.JK_NEW_BINDING);
    } else {
      pageHeader = new JamPageHeader(context, CurrentMenuItem.JK);
    }
    pageBuilder.setHeader(pageHeader);
    
    if (checkAuth(req)) {

      pageHeader.setButtonLogout(req);
      
      if (details) {
        // show details for JkMount by id
        
        BindingDto binding = new JkApi().getBinding(env, mountId);
        BindingDetailsPageContent content = new BindingDetailsPageContent(context, mountId, binding);
        
        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();checkbox_onload();controlButtons_onload();jk_onload();");
        
      } else if (newBinding) {
        // show details for a newly created binding
        
        BindingDetailsPageContent content = new BindingDetailsPageContent(context);
        
        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();checkbox_onload();controlButtons_onload();");
        
      } else if (list) {
        // show table
        
        final List<JkMountDto> jkMounts = new JkApi().getJkMounts(env);
        
        JkMountTablePageContent content = new JkMountTablePageContent(context, jkMounts);
        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();");
      }
      
    } else {
      
      requireAuth(req, pageBuilder);
      
    }
    
    HtmlPageExtBuilder.Page page = pageBuilder.build();
    page.respond(resp);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    boolean unknownRequest = false;
    
    final String path = req.getPathInfo();
    
    if (path == null) {
      unknownRequest = true;
      
    } else if ("/new-binding".equals(path)) {
      
      // TODO create new binding
      
    } else {
      final String[] split = path.split("/");
      if (split.length == 2) {
        
        if ("mod".equals(split[1])) {
      
          final String mountId = split[0];
          // TODO modify binding by mountId
          
        } else if ("del".equals(split[1])) {
          
          final String mountId = split[0];
          // TODO delete binding by mountId
          
        } else {
          
          unknownRequest = true;
        }
        
      } else {
        unknownRequest = true;
      }
    }
    
    if (unknownRequest) {
      // unknown request
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not understand the request");
      return;
    }
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
