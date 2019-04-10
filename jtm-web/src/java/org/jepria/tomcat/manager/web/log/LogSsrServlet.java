package org.jepria.tomcat.manager.web.log;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.log.dto.LogDto;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.ForbiddenFragment;
import org.jepria.web.ssr.HtmlPage;
import org.jepria.web.ssr.HtmlPageForbidden;
import org.jepria.web.ssr.HtmlPageUnauthorized;
import org.jepria.web.ssr.LoginFragment;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;
import org.jepria.web.ssr.SsrServletBase;

public class LogSsrServlet extends SsrServletBase {
  
  private static final long serialVersionUID = 8783691536276281226L;
  
  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }

  private static final String LTZO_COOKIE_SESSION_ATTR_KEY = "org.jepria.tomcat.manager.web.log.SessionAttributes.localTimezoneOffsetCookie";
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    // null means the client timezone is undefined
    final TimeZone clientTimezone;
    
    
    // before processing the request itself, obtain the local timezone offset from the client over the cookies (then repeat the request and process) 
    if (req.getSession().getAttribute(LTZO_COOKIE_SESSION_ATTR_KEY) == null) {
      // first request
      req.getSession().setAttribute(LTZO_COOKIE_SESSION_ATTR_KEY, new Object());
      
      final HtmlPage page = new HtmlPage();
      // TODO populate the page with header or title (for the case of disabled JS, for example)
      El script = new El("script");
      script.setAttribute("type", "text/javascript");
      script.setInnerHTML("document.cookie=\"local-timezone-offset=\" + (-new Date().getTimezoneOffset()); window.location.reload();");
      page.getBodyChilds().add(script);
      page.respond(resp);
      return;
      
    } else {
      // after the local timezone offset cookie has been obtained 
      req.getSession().removeAttribute(LTZO_COOKIE_SESSION_ATTR_KEY);
      
      Optional<Cookie> cookieLtzo = Arrays.stream(req.getCookies()).filter(cookie -> cookie.getName().equals("local-timezone-offset")).findAny();
      final String cookieLtzoValue; 
      if (cookieLtzo.isPresent() && (cookieLtzoValue = cookieLtzo.get().getValue()) != null) {
        final int localTimezoneOffset = Integer.parseInt(cookieLtzoValue);
        
        // parse TimeZone
        String[] availableIds = TimeZone.getAvailableIDs(localTimezoneOffset * 60 * 1000);
        if (availableIds != null && availableIds.length > 0) {
          clientTimezone = TimeZone.getTimeZone(availableIds[0]);
        } else {
          throw new IllegalArgumentException("no TimeZone found for such offset: " + localTimezoneOffset);
        }
        
      } else {
        clientTimezone = null;
      }
    }
    

    
    final HtmlPage htmlPage;

    final PageHeader pageHeader = new PageHeader(CurrentMenuItem.LOG);
    
    final Environment env = EnvironmentFactory.get(req);
    final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    pageHeader.setManagerApache(managerApacheHref);
    
    if (checkAuth(req)) {
      List<LogDto> logs = new LogApi().list(env, null);

      htmlPage = new LogHtmlPage(logs, clientTimezone);
  
      pageHeader.setButtonLogout("log/logout"); // TODO this will erase any path- or request params of the current page
      
    } else {
      
      AuthInfo authInfo = requireAuth(req, "log/login", "log/logout"); // TODO this will erase any path- or request params of the current page
      
      // TODO refactor the following shit!
      if (authInfo.authFragment instanceof LoginFragment) {
        htmlPage = new HtmlPageUnauthorized((LoginFragment)authInfo.authFragment);
        htmlPage.setStatusBar(authInfo.statusBar);
      } else if (authInfo.authFragment instanceof ForbiddenFragment) {
        htmlPage = new HtmlPageForbidden((ForbiddenFragment)authInfo.authFragment);
        pageHeader.setButtonLogout("log/logout"); // TODO this will erase any path- or request params of the current page
        htmlPage.setStatusBar(authInfo.statusBar);
      } else {
        // TODO
        throw new IllegalStateException();
      }
      
      htmlPage.setTitle(LogHtmlPage.PAGE_TITLE);
    }
    
    htmlPage.setPageHeader(pageHeader);
    htmlPage.respond(resp);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final String path = req.getPathInfo();
    
    if ("/login".equals(path)) {
      
      login(req);
      
      resp.sendRedirect("../log"); // TODO
      return;
      
    } else if ("/logout".equals(path)) {
      
      logout(req);
      
      resp.sendRedirect("../log"); // TODO
      return;
        
    }
  }
}
