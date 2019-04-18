package org.jepria.web.ssr;

import java.io.IOException;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

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

  private final String getStatusBarModDataSavedHtmlPostfix(Context context) {
    return ".&emsp;&emsp;&emsp;<span class=\"span-bold\">"
        + context.getText("org.jepria.web.ssr.SsrServletBase.status.mod_saved.saved") 
        + ",</span> " 
        + context.getText("org.jepria.web.ssr.SsrServletBase.status.mod_saved.restored") 
        + ".&ensp;<a href=\"\">" 
        + context.getText("org.jepria.web.ssr.SsrServletBase.status.mod_saved.delete")
        + "</a>";
  }
  
  protected StatusBar createStatusBar(Context context, AuthState authState) {
    if (authState == null || authState.auth == null) {
      return null;
    }
    switch (authState.auth) {
    case LOGIN_FALIED: {
      String innerHtml = "<span class=\"span-bold\">"
          + context.getText("org.jepria.web.ssr.SsrServletBase.status.login_failed.incorrect_data")
          + ",</span> "
          + context.getText("org.jepria.web.ssr.SsrServletBase.status.login_failed.try_again");
      if (authState.authPersistentData != null) {
        innerHtml += getStatusBarModDataSavedHtmlPostfix(context);
      }
      return new StatusBar(context, StatusBar.Type.ERROR, innerHtml);
    }
    case LOGOUT: {
      return new StatusBar(context, StatusBar.Type.SUCCESS, context.getText("org.jepria.web.ssr.SsrServletBase.status.logouted"));
    }
    case UNAUTHORIZED: {
      String innerHtml = context.getText("org.jepria.web.ssr.SsrServletBase.status.auth_required");
      if (authState.authPersistentData != null) {
        innerHtml += getStatusBarModDataSavedHtmlPostfix(context);
      }
      return new StatusBar(context, StatusBar.Type.INFO, innerHtml);
    }
    case FORBIDDEN: {
      return new StatusBar(context, StatusBar.Type.ERROR, context.getText("org.jepria.web.ssr.SsrServletBase.status.forbidden"));
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
  
  protected class AuthPageBuilder {
    
    private final HttpServletRequest req;
    private final String loginActionUrl;
    private final String logoutActionUrl;
    
    public AuthPageBuilder(HttpServletRequest req, String loginActionUrl, String logoutActionUrl) {
      this.req = req;
      this.loginActionUrl = loginActionUrl;
      this.logoutActionUrl = logoutActionUrl;
    }

    public void requireAuth(PageBuilder page) {
      final Context context = Context.fromRequest(req);
      
      final AuthState authState = getAuthState(req);
      
      if (authState.auth == Auth.UNAUTHORIZED || authState.auth == Auth.LOGOUT) {
        authState.authPersistentData = null;
      }
      
      if (authState.auth == Auth.AUTHORIZED || authState.auth == null) {
        authState.auth = Auth.UNAUTHORIZED;
      }

      if (req.getUserPrincipal() == null) {
        final LoginFragment loginFragment = new LoginFragment(context, loginActionUrl);
        
        // restore preserved username
        if (authState.auth == Auth.LOGIN_FALIED && authState.username != null) {
          loginFragment.inputUsername.setAttribute("value", authState.username);
          loginFragment.inputPassword.addClass("requires-focus");
        } else {
          loginFragment.inputUsername.addClass("requires-focus");
        }
        
        page.getBody().appendChild(loginFragment);
        
        page.getBody().addScript("css/jtm-common.css");
        page.getBody().setAttribute("onload", "jtm_onload();authFragmentLogin_onload();");

        page.getBody().addClass("background_gray");
        
      } else {
        authState.auth = Auth.FORBIDDEN;
        
        final ForbiddenFragment forbiddenFragment = new ForbiddenFragment(context, logoutActionUrl, req.getUserPrincipal().getName());
        
        page.getBody().appendChild(forbiddenFragment);
        
        page.getBody().addScript("css/jtm-common.css");
        page.getBody().setAttribute("onload", "jtm_onload();");

        page.getBody().addClass("background_gray");
        
        page.setButtonLogout(logoutActionUrl);
      }
      
      page.setStatusBar(createStatusBar(context, authState));
      
      // reset a disposable state
      if (authState.auth == Auth.LOGIN_FALIED 
          || authState.auth == Auth.LOGOUT) {
        authState.auth = Auth.UNAUTHORIZED;
      }
      authState.username = null;
    }
    
  }
}
