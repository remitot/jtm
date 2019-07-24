package org.jepria.httpd.apache.manager.web.restart;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.httpd.apache.manager.web.JamPageHeader;
import org.jepria.httpd.apache.manager.web.JamPageHeader.CurrentMenuItem;
import org.jepria.httpd.apache.manager.web.service.ApacheServiceFactory;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.HtmlPageBaseBuilder;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.StatusBar;
import org.jepria.web.ssr.Text;

public class RestartSsrServlet extends SsrServletBase {

  private static final long serialVersionUID = -5406772704670572455L;

  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  private static final String RESTART_STATE_SESSION_ATTR_KEY = 
      RestartSsrServlet.class.getCanonicalName() + ".SessionAttributes.restartState";
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    Context context = Context.get(req, "text/org_jepria_httpd_apache_manager_web_Text");
    Text text = context.getText();
    
    if ("init".equals(req.getSession().getAttribute(RESTART_STATE_SESSION_ATTR_KEY))) {
      req.getSession().removeAttribute(RESTART_STATE_SESSION_ATTR_KEY);
      
      if (checkAuth(req)) {

        final HtmlPageBaseBuilder pageBuilder = HtmlPageBaseBuilder.newInstance(context);
        pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.restart.title"));
        
        pageBuilder.setContent(new RestartFragment(context));
        pageBuilder.setBodyAttributes("onload", "common_onload();restart_fragment_onload();");

        HtmlPageExtBuilder.Page page = pageBuilder.build();
        page.respond(resp);

        req.getSession().setAttribute(RESTART_STATE_SESSION_ATTR_KEY, "wait-status");
        
      } else {

        resp.sendRedirect(context.getContextPath() + "/restart");
      }

    } else {

      final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(context);
      pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.restart.title"));

      final PageHeader pageHeader = new JamPageHeader(context, CurrentMenuItem.RESTART);
      pageBuilder.setHeader(pageHeader);

      if (checkAuth(req)) {
        pageHeader.setButtonLogout(req);

        
        StatusBar statusBar = null;
        if ("status-success".equals(req.getSession().getAttribute(RESTART_STATE_SESSION_ATTR_KEY))) {
          statusBar = createStatusBar(context, true);
        } else if ("status-failure".equals(req.getSession().getAttribute(RESTART_STATE_SESSION_ATTR_KEY))) {
          statusBar = createStatusBar(context, false);
        }
        pageBuilder.setStatusBar(statusBar);
        

        RestartPageContent content = new RestartPageContent(context);
        pageBuilder.setContent(content);
        
        pageBuilder.setBodyAttributes("onload", "common_onload();restart_onload();");

      } else {

        requireAuth(req, pageBuilder);

      }

      HtmlPageExtBuilder.Page page = pageBuilder.build();
      page.respond(resp);
      
      req.getSession().removeAttribute(RESTART_STATE_SESSION_ATTR_KEY);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String path = req.getPathInfo();

    Context context = Context.get(req, "text/org_jepria_httpd_apache_manager_web_Text");

    if ("/exec".equals(path)) {

      if (checkAuth(req)) {

        try {
          restart(req);
          
          // set status success only if waiting for the status
          if ("wait-status".equals(req.getSession().getAttribute(RESTART_STATE_SESSION_ATTR_KEY))) {
            req.getSession().setAttribute(RESTART_STATE_SESSION_ATTR_KEY, "status-success");
          }
          
        } catch (Throwable e) {

          // set status failure only if waiting for the status
          if ("wait-status".equals(req.getSession().getAttribute(RESTART_STATE_SESSION_ATTR_KEY))) {
            req.getSession().setAttribute(RESTART_STATE_SESSION_ATTR_KEY, "status-failure");
          }
          
          throw e;
        }
        
        

      } else {

        resp.sendRedirect(context.getContextPath() + "/restart");
      }

    } else {

      if (checkAuth(req)) {
        req.getSession().setAttribute(RESTART_STATE_SESSION_ATTR_KEY, "init");
      }
      resp.sendRedirect(context.getContextPath() + "/restart");
    }

  }

  protected void restart(HttpServletRequest req) {

    final String apacheServiceName = EnvironmentFactory.get(req).getProperty("org.jepria.httpd.apache.manager.web.apacheServiceName");

    if (apacheServiceName == null) {
      throw new RuntimeException("Misconfiguration exception: "
          + "mandatory configuration property \"org.jepria.httpd.apache.manager.web.apacheServiceName\" is not defined");
    }

    // restart the Apache service
    ApacheServiceFactory.get(apacheServiceName).restart();
  }
  
  protected StatusBar createStatusBar(Context context, boolean success) {

    Text text = context.getText();
    
    if (success) {
      return new StatusBar(context, StatusBar.Type.SUCCESS, text.getString("org.jepria.httpd.apache.manager.web.restart.status.restart_success"));
    } else {
      return new StatusBar(context, StatusBar.Type.ERROR, text.getString("org.jepria.httpd.apache.manager.web.restart.status.restart_failure"));
    }
  }

}
