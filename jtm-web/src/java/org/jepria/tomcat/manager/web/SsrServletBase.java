package org.jepria.tomcat.manager.web;

import java.io.IOException;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.jepria.web.ssr.AuthFragment;
import org.jepria.web.ssr.LoginFragment;
import org.jepria.web.ssr.ForbiddenFragment;
import org.jepria.web.ssr.StatusBar;

public class SsrServletBase extends HttpServlet {

  private static final long serialVersionUID = 1760582345667928411L;
  
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null;
  }
  
  protected boolean login(HttpServletRequest req) throws IOException {
    final String username = req.getParameter("username");
    final String password = req.getParameter("password");
    
    final AuthState authState = getAuthState(req); 
    
    boolean loginSuccess;
    
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
      
      loginSuccess = true;
      
    } catch (ServletException e) {
      e.printStackTrace();
      
      authState.auth = Auth.LOGIN_FALIED;
      authState.username = username;
      
      loginSuccess = false;
    }
    
    // maintain the before-login auth state
    getAuthState(req, () -> authState);
    
    return loginSuccess;
  }
  
  protected void logout(HttpServletRequest req) throws ServletException {
    req.logout();
    req.getSession().invalidate();
    
    getAuthState(req).auth = Auth.LOGOUT;
  }

  public class AuthInfo {
    public AuthFragment authFragment;
    public StatusBar statusBar;
  }
  
  protected AuthInfo requireAuth(HttpServletRequest req, String loginActionUrl, String logoutActionUrl) {
    
    final AuthState authState = getAuthState(req);
    
    if (authState.auth == Auth.UNAUTHORIZED || authState.auth == Auth.LOGOUT) {
      authState.authPersistentData = null;
    }
    
    if (authState.auth == Auth.AUTHORIZED || authState.auth == null) {
      authState.auth = Auth.UNAUTHORIZED;
    }

    final AuthInfo authInfo;
    
    if (req.getUserPrincipal() == null) {
      final LoginFragment loginFragment = new LoginFragment(loginActionUrl);
      
      // restore preserved username
      if (authState.auth == Auth.LOGIN_FALIED && authState.username != null) {
        loginFragment.inputUsername.setAttribute("value", authState.username);
        loginFragment.inputPassword.addClass("requires-focus");
      } else {
        loginFragment.inputUsername.addClass("requires-focus");
      }
      
      authInfo = new AuthInfo();
      authInfo.authFragment = loginFragment;
      
    } else {
      authState.auth = Auth.FORBIDDEN;
      
      final ForbiddenFragment forbiddenFragment = new ForbiddenFragment(logoutActionUrl, req.getUserPrincipal().getName());
      
      authInfo = new AuthInfo();
      authInfo.authFragment = forbiddenFragment;
      
    }
    
    authInfo.statusBar = createStatusBar(authState);
    
    // reset a disposable state
    if (authState.auth == Auth.LOGIN_FALIED 
        || authState.auth == Auth.LOGOUT) {
      authState.auth = Auth.UNAUTHORIZED;
    }
    authState.username = null;

    return authInfo;
  }
  
  private static final String STATUS_BAR__MOD_DATA_SAVED__HTML_POSTFIX = ".&emsp;&emsp;&emsp;<span class=\"span-bold\">Сделанные изменения сохранены,</span> они будут восстановлены после авторизации.&ensp;<a href=\"\">Удалить их</a>"; // NON-NLS
  
  protected StatusBar createStatusBar(AuthState authState) {
    if (authState == null || authState.auth == null) {
      return null;
    }
    switch (authState.auth) {
    case LOGIN_FALIED: {
      String innerHtml = "<span class=\"span-bold\">Неверные данные,</span> попробуйте ещё раз"; // NON-NLS
      if (authState.authPersistentData != null) {
        innerHtml += STATUS_BAR__MOD_DATA_SAVED__HTML_POSTFIX;
      }
      return new StatusBar(StatusBar.Type.ERROR, innerHtml);
    }
    case LOGOUT: {
      return new StatusBar(StatusBar.Type.SUCCESS, "Разлогинились"); // NON-NLS
    }
    case UNAUTHORIZED: {
      String innerHtml = "Необходимо авторизоваться"; // NON-NLS
      if (authState.authPersistentData != null) {
        innerHtml += STATUS_BAR__MOD_DATA_SAVED__HTML_POSTFIX;
      }
      return new StatusBar(StatusBar.Type.INFO, innerHtml);
    }
    case FORBIDDEN: {
      return new StatusBar(StatusBar.Type.ERROR, "Доступ запрещён"); // NON-NLS
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
    public Object authPersistentData;
  }
  
  protected enum Auth {
    AUTHORIZED,
    /**
     * For the current module! Not application-wide
     */
    FORBIDDEN,
    UNAUTHORIZED,
    LOGIN_FALIED,
    LOGOUT,
  }
  
  protected AuthState getAuthState(HttpServletRequest request) {
    return getAuthState(request, () -> new AuthState());
  }
  
  /**
   * @param request
   * @param orElse an auth state instance to set and return if the current auth state is null 
   * @return
   */
  private AuthState getAuthState(HttpServletRequest request, Supplier<AuthState> orElse) {
    AuthState state = (AuthState)request.getSession().getAttribute("org.jepria.tomcat.manager.web.SessionAttributes.authState");
    if (state == null) {
      state = orElse == null ? null : orElse.get();
      request.getSession().setAttribute("org.jepria.tomcat.manager.web.SessionAttributes.authState", state);
    }
    return state;
  }
}
