package org.jepria.tomcat.manager.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

  /**
   * Processes session auth logic and returns a pre-rendered {@link HtmlPage} with login GUI 
   */
  protected HtmlPage requireAuth(HttpServletRequest req, HttpServletResponse resp,
      String loginActionUrl) throws IOException {
    
    final AuthState authState = getAuthState(req);
    
    if (authState.auth == Auth.UNAUTHORIZED || authState.auth == Auth.UNAUTHORIZED__LOGOUT) {
      authState.modDataSaved = false;
    }
    
    if (authState.auth == Auth.AUTHORIZED || authState.auth == null) {
      authState.auth = Auth.UNAUTHORIZED;
    }

    final HtmlPageUnauthorized htmlPage = new HtmlPageUnauthorized(loginActionUrl);
    
    // restore preserved username
    if (authState.auth == Auth.UNAUTHORIZED__LOGIN_FALIED && authState.username != null) {
      htmlPage.loginFragment.inputUsername.setAttribute("value", authState.username);
      htmlPage.loginFragment.inputPassword.addClass("requires-focus");
    } else {
      htmlPage.loginFragment.inputUsername.addClass("requires-focus");
    }

    htmlPage.setStatusBar(createStatusBar(authState));
    
    // reset a disposable state
    if (authState.auth == Auth.UNAUTHORIZED__LOGIN_FALIED 
        || authState.auth == Auth.UNAUTHORIZED__LOGOUT) {
      authState.auth = Auth.UNAUTHORIZED;
    }
    authState.username = null;

    return htmlPage;
  }
  
  private static final String STATUS_BAR__MOD_DATA_SAVED__HTML_POSTFIX = ".&emsp;&emsp;&emsp;<span class=\"span-bold\">Сделанные изменения сохранены,</span> они будут восстановлены после авторизации.&ensp;<a href=\"\">Удалить их</a>"; // NON-NLS
  
  protected StatusBar createStatusBar(AuthState authState) {
    if (authState == null || authState.auth == null) {
      return null;
    }
    switch (authState.auth) {
    case UNAUTHORIZED__LOGIN_FALIED: {
      String innerHtml = "<span class=\"span-bold\">Неверные данные,</span> попробуйте ещё раз"; // NON-NLS
      if (authState.modDataSaved) {
        innerHtml += STATUS_BAR__MOD_DATA_SAVED__HTML_POSTFIX;
      }
      return new StatusBar(StatusBar.Type.ERROR, innerHtml);
    }
    case UNAUTHORIZED__LOGOUT: {
      return new StatusBar(StatusBar.Type.SUCCESS, "Разлогинились"); // NON-NLS
    }
    case UNAUTHORIZED: {
      String innerHtml = "Необходимо авторизоваться"; // NON-NLS
      if (authState.modDataSaved) {
        innerHtml += STATUS_BAR__MOD_DATA_SAVED__HTML_POSTFIX;
      }
      return new StatusBar(StatusBar.Type.INFO, innerHtml);
    }
    case AUTHORIZED: {
      return null;
    }
    }
    throw new IllegalArgumentException(String.valueOf(authState.auth));
  }
  
  /**
   * Class stored into a session
   */
  protected class AuthState {
    public Auth auth;
    public String username;
    
    private boolean modDataSaved = false;
    public void saveModData() {
      modDataSaved = true;
    }
  }
  
  protected enum Auth {
    AUTHORIZED,
    UNAUTHORIZED,
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
