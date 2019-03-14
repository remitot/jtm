package org.jepria.tomcat.manager.web;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;
import org.jepria.web.ssr.StatusBar;

public class SsrServletBase extends HttpServlet {

  private static final long serialVersionUID = 1760582345667928411L;
  
  protected boolean checkAuth(HttpServletRequest req, HttpServletResponse resp) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  protected boolean login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String username = req.getParameter("username");
    final String password = req.getParameter("password");
    
    try {
      
      // TODO Tomcat bug?
      // when logged into vsmlapprfid1:8081/manager-ext/jdbc, then opening vsmlapprfid1:8080/manager-ext/jdbc results 401 
      // (on tomcat's container security check level) -- WHY? (with SSO valve turned on!)
      // OK, but after that, if we do vsmlapprfid1:8080/manager-ext/api/login -- the userPrincipal IS null, but req.login() throws
      // 'javax.servlet.ServletException: This request has already been authenticated' -- WHY? Must be EITHER request authenticated OR userPrincipal==null!
      
      // So, as a workaround -- logout anyway...
      
//        // logout if logged in
//        if (req.getUserPrincipal() != null) {
//          req.logout();
//        }
      
      req.logout();
      
      req.login(username, password);

      req.getSession().removeAttribute("org.jepria.tomcat.manager.web.SessionAttributes.loginStatus");
      req.getSession().removeAttribute("org.jepria.tomcat.manager.web.SessionAttributes.loginStatus.removeOnNextGet");
      
      return true;
      
    } catch (ServletException e) {
      e.printStackTrace();
      
      setLoginStatus(req, LoginStatus.LOGIN_FAILURE);
      
      return false;
    }
  }
  
  protected void logout(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
    req.logout();
    req.getSession().invalidate();
    
    setLoginStatus(req, LoginStatus.LOGOUT_SUCCESS);
  }

  protected void doLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final Environment env = EnvironmentFactory.get(req);
    
    final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    final PageHeader pageHeader = new PageHeader(managerApacheHref, null, CurrentMenuItem.JDBC);
    
    if (req.getSession().getAttribute("org.jepria.tomcat.manager.web.SessionAttributes.loginStatus.removeOnNextGet") != null) {
      req.getSession().removeAttribute("org.jepria.tomcat.manager.web.SessionAttributes.loginStatus.removeOnNextGet");
      req.getSession().removeAttribute("org.jepria.tomcat.manager.web.SessionAttributes.loginStatus");
    }
    final LoginStatus loginStatus = (LoginStatus)req.getSession().getAttribute("org.jepria.tomcat.manager.web.SessionAttributes.loginStatus");
    
    HtmlPage htmlPage = new HtmlPageUnauthorized(pageHeader, "jdbc/login"); // TODO this will erase any path- or request params of the current page
    htmlPage.setStatusBar(createStatusBar(loginStatus));
    
    htmlPage.setTitle("Tomcat manager: датасорсы (JDBC)"); // NON-NLS
    htmlPage.respond(resp);
    
    req.getSession().setAttribute("org.jepria.tomcat.manager.web.SessionAttributes.loginStatus.removeOnNextGet", new Object());
  }
  
  protected enum LoginStatus {
    /**
     * Login attempt failed: incorrect credentials
     */
    LOGIN_FAILURE,
    /**
     * Mod attempt failed: {@link #checkAuth} failed
     */
    MOD_SESSION_EXPIRED,
    /**
     * Logout succeeded
     */
    LOGOUT_SUCCESS,
    
  }
  
  protected StatusBar createStatusBar(LoginStatus status) {
    if (status == null) {
      return null;
    }
    switch (status) {
    case LOGIN_FAILURE: {
      return new StatusBar(StatusBar.Type.ERROR, "<span class=\"span-bold\">Неверные данные, попробуйте ещё раз.</span>"); // NON-NLS
    }
    case MOD_SESSION_EXPIRED: {
      return new StatusBar(StatusBar.Type.INFO, "<span class=\"span-bold\">Необходимо авторизоваться.</span>&emsp;Сделанные изменения будут восстановлены.");
    }
    case LOGOUT_SUCCESS: {
      return new StatusBar(StatusBar.Type.SUCCESS, "Разлогинились.</span>");
    }
    }
    throw new IllegalArgumentException(String.valueOf(status));
  }
  
  protected void setLoginStatus(HttpServletRequest request, LoginStatus status) {
    Objects.requireNonNull(status, "LoginStatus must not be null");
    request.getSession().setAttribute("org.jepria.tomcat.manager.web.SessionAttributes.loginStatus", status);
    request.getSession().removeAttribute("org.jepria.tomcat.manager.web.SessionAttributes.loginStatus.removeOnNextGet");
  }
}
