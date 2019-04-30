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
import org.jepria.web.ssr.HtmlPageBuilder;
import org.jepria.web.ssr.HtmlPageBuilder.Page;
import org.jepria.web.ssr.JtmPageBuilder;
import org.jepria.web.ssr.Node;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.Texts;

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
      

      // TODO populate the page with human-readable header or title (for the case of disabled JS, for example)
      HtmlPageBuilder pageBuilder = HtmlPageBuilder.newInstance();
      
      Node script = Node.fromHtml("<script type=\"text/javascript\">document.cookie=\"local-timezone-offset=\" + (-new Date().getTimezoneOffset()); window.location.reload();</script>");
      
      pageBuilder.setContent(script);
      
      Page page = pageBuilder.build();
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
    

    
    final Text text = Texts.get(req, "text/org_jepria_tomcat_manager_web_Text");
    
    final Environment env = EnvironmentFactory.get(req);
    
    final JtmPageBuilder pageBuilder = JtmPageBuilder.newInstance(text);
    pageBuilder.setTitle("Tomcat manager: логи"); // NON-NLS
    pageBuilder.setCurrentMenuItem(CurrentMenuItem.LOG);
    
    String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    pageBuilder.setManagerApache(managerApacheHref);
    
    
    if (checkAuth(req)) {
      
      List<LogDto> logs = new LogApi().list(env, null);

      LogPageContent content = new LogPageContent(text, logs, clientTimezone);
      pageBuilder.setContent(content);
      pageBuilder.setBodyAttributes("onload", "jtm_onload();table_onload();");
  
      pageBuilder.setButtonLogout("log"); // TODO this will erase any path- or request params of the current page
      
    } else {
      
      new AuthPageBuilder(req, "log").requireAuth(pageBuilder);
      
    }
    
    
    JtmPageBuilder.Page page = pageBuilder.build();
    page.respond(resp);
  }
}
