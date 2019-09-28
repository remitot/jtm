package org.jepria.httpd.apache.manager.web.restart;

import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.httpd.apache.manager.web.JamPageHeader;
import org.jepria.httpd.apache.manager.web.JamPageHeader.CurrentMenuItem;
import org.jepria.httpd.apache.manager.web.service.ApacheService;
import org.jepria.httpd.apache.manager.web.service.ApacheServiceLocator;
import org.jepria.httpd.apache.manager.web.service.ApacheServiceLocatorFactory;
import org.jepria.web.ssr.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

        pageBuilder.getBody().appendChild(new RestartFragment(context));

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
        pageHeader.setSources();

        if ("status-success".equals(req.getSession().getAttribute(RESTART_STATE_SESSION_ATTR_KEY))) {
          final StatusBar statusBar = new StatusBar(context);
          statusBar.setType(StatusBar.Type.SUCCESS);
          statusBar.setHeaderHTML(text.getString("org.jepria.httpd.apache.manager.web.restart.status.restart_success"));
          pageBuilder.setStatusBar(statusBar);
        } else if ("status-failure".equals(req.getSession().getAttribute(RESTART_STATE_SESSION_ATTR_KEY))) {
          final StatusBar statusBar = new StatusBar(context);
          statusBar.setType(StatusBar.Type.ERROR);
          statusBar.setHeaderHTML(text.getString("org.jepria.httpd.apache.manager.web.restart.status.restart_failure"));
          pageBuilder.setStatusBar(statusBar);
        }


        RestartPageContent content = new RestartPageContent(context);
        pageBuilder.setContent(content);

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

    final ApacheServiceLocator locator = ApacheServiceLocatorFactory.get();

    if (locator == null) {
      throw new RuntimeException("Restart failed: Apache service locator not found");
    }

    final String apacheServiceName = EnvironmentFactory.get(req).getApacheServiceName();

    if (apacheServiceName == null) {
      throw new RuntimeException("Misconfiguration exception: "
              + "mandatory configuration property \"org.jepria.httpd.apache.manager.web.apacheServiceName\" is not defined");
    }

    final ApacheService service = locator.get(apacheServiceName);

    if (service == null) {
      throw new RuntimeException("Restart failed: Apache service not found");
    }

    service.restart();
  }
}
