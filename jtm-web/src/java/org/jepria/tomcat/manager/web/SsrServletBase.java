package org.jepria.tomcat.manager.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;
import org.jepria.web.ssr.StatusBar;

public class SsrServletBase extends HttpServlet {

  private static final long serialVersionUID = 1760582345667928411L;
  
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  protected boolean login(HttpServletRequest req) throws IOException {
    final String username = req.getParameter("username");
    final String password = req.getParameter("password");
    
    final AuthState authState = getAuthState(req); 
    
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

      authState.auth = Auth.AUTHORIZED;
      
      return true;
      
    } catch (ServletException e) {
      e.printStackTrace();
      
      authState.auth = Auth.UNAUTHORIZED__LOGIN_FALIED;
      authState.username = username;
      
      return false;
    }
  }
  
  protected void logout(HttpServletRequest req) throws ServletException {
    req.logout();
    req.getSession().invalidate();
    
    getAuthState(req).auth = Auth.UNAUTHORIZED__LOGOUT;
  }

  protected void doLogin(HttpServletRequest req, HttpServletResponse resp, String loginActionUrl) throws IOException {
    
    final AuthState authState = getAuthState(req);
    if (authState.auth == Auth.AUTHORIZED || authState.auth == null) {
      authState.auth = Auth.UNAUTHORIZED;
    }

    
    final Environment env = EnvironmentFactory.get(req);
    
    final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    final PageHeader pageHeader = new PageHeader(managerApacheHref, null, CurrentMenuItem.JDBC);

    final HtmlPageUnauthorized htmlPage = new HtmlPageUnauthorized(loginActionUrl);
    
    // restore preserved username
    if (authState.auth == Auth.UNAUTHORIZED__LOGIN_FALIED && authState.username != null) {
      htmlPage.loginFragment.inputUsername.setAttribute("value", authState.username);
      htmlPage.loginFragment.inputPassword.addClass("requires-focus");
    } else {
      htmlPage.loginFragment.inputUsername.addClass("requires-focus");
    }
    
    htmlPage.setPageHeader(pageHeader);
    htmlPage.setStatusBar(createStatusBar(authState.auth));
    
    htmlPage.setTitle("Tomcat manager: датасорсы (JDBC)"); // NON-NLS
    htmlPage.respond(resp);

    
    // reset a disposable state
    if (authState.auth == Auth.UNAUTHORIZED__LOGIN_FALIED 
        || authState.auth == Auth.UNAUTHORIZED__LOGOUT
        || authState.auth == Auth.UNAUTHORIZED__MOD) {
      authState.auth = Auth.UNAUTHORIZED;
    }
    authState.username = null;
    
  }
  
  protected StatusBar createStatusBar(Auth auth) {
    if (auth == null) {
      return null;
    }
    switch (auth) {
    case UNAUTHORIZED__LOGIN_FALIED: {
      return new StatusBar(StatusBar.Type.ERROR, "<span class=\"span-bold\">Неверные данные,</span> попробуйте ещё раз"); // NON-NLS
    }
    case UNAUTHORIZED__MOD: {
      return new StatusBar(StatusBar.Type.INFO, "Необходимо авторизоваться.&emsp;<span class=\"span-bold\">Сделанные изменения будут восстановлены</span>");
    }
    case UNAUTHORIZED__LOGOUT: {
      return new StatusBar(StatusBar.Type.SUCCESS, "Разлогинились");
    }
    case UNAUTHORIZED: {
      return new StatusBar(StatusBar.Type.INFO, "Необходимо авторизоваться");
    }
    case AUTHORIZED: {
      return null;
    }
    }
    throw new IllegalArgumentException(String.valueOf(auth));
  }
  
  /**
   * Class stored into a session
   */
  protected class AuthState {
    public Auth auth;
    public String username;
  }
  
  protected enum Auth {
    AUTHORIZED,
    UNAUTHORIZED,
    UNAUTHORIZED__MOD,
    UNAUTHORIZED__LOGIN_FALIED,
    UNAUTHORIZED__LOGOUT,
  }
  
  protected AuthState getAuthState(HttpServletRequest request) {
    AuthState state = (AuthState)request.getSession().getAttribute("org.jepria.tomcat.manager.web.SessionAttributes.authState");
    if (state == null) {
      state = new AuthState();
      request.getSession().setAttribute("org.jepria.tomcat.manager.web.SessionAttributes.authState", state);
    }
    return state;
  }
}
