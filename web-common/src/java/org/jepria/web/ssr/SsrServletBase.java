package org.jepria.web.ssr;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.jepria.web.ssr.AuthUtils.Auth;
import org.jepria.web.ssr.AuthUtils.AuthState;

public class SsrServletBase extends HttpServlet {

  private static final long serialVersionUID = 1760582345667928411L;
  
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null;
  }
  
  private final String getStatusBarModDataSavedHtmlPostfix(Text text) {
    return ".&emsp;&emsp;&emsp;<span class=\"span-bold\">"
        + text.getString("org.jepria.web.ssr.SsrServletBase.status.mod_saved.saved") 
        + ",</span> " 
        + text.getString("org.jepria.web.ssr.SsrServletBase.status.mod_saved.restored") 
        + ".&ensp;<a href=\"\">" 
        + text.getString("org.jepria.web.ssr.SsrServletBase.status.mod_saved.delete")
        + "</a>";
  }
  
  protected StatusBar createStatusBar(Text text, AuthState authState) {
    if (authState == null || authState.auth == null) {
      return null;
    }
    switch (authState.auth) {
    case LOGIN_FALIED: {
      String innerHtml = "<span class=\"span-bold\">"
          + text.getString("org.jepria.web.ssr.SsrServletBase.status.login_failed.incorrect_data")
          + ",</span> "
          + text.getString("org.jepria.web.ssr.SsrServletBase.status.login_failed.try_again");
      if (authState.authPersistentData != null) {
        innerHtml += getStatusBarModDataSavedHtmlPostfix(text);
      }
      return new StatusBar(StatusBar.Type.ERROR, innerHtml);
    }
    case LOGOUT: {
      return new StatusBar(StatusBar.Type.SUCCESS, text.getString("org.jepria.web.ssr.SsrServletBase.status.logouted"));
    }
    case UNAUTHORIZED: {
      String innerHtml = text.getString("org.jepria.web.ssr.SsrServletBase.status.auth_required");
      if (authState.authPersistentData != null) {
        innerHtml += getStatusBarModDataSavedHtmlPostfix(text);
      }
      return new StatusBar(StatusBar.Type.INFO, innerHtml);
    }
    case FORBIDDEN: {
      return new StatusBar(StatusBar.Type.ERROR, text.getString("org.jepria.web.ssr.SsrServletBase.status.forbidden"));
    }
    case AUTHORIZED: {
      return null;
    }
    }
    throw new IllegalArgumentException(String.valueOf(authState.auth));
  }
  
  protected class AuthPageBuilder {
    
    private final HttpServletRequest req;
    private final String authRedirectPath;
    
    /**
     * 
     * @param req
     * @param authRedirectPath path to redirect after a successful login of logout.
     * If {@code null}, no redirect will be performed
     */
    public AuthPageBuilder(HttpServletRequest req, String authRedirectPath) {
      this.req = req;
      this.authRedirectPath = authRedirectPath;
    }

    public void requireAuth(JtmPageBuilder page) {
      final Text text = Text.fromRequest(req);
      
      final AuthState authState = AuthUtils.getAuthState(req);
      
      if (authState.auth == Auth.UNAUTHORIZED || authState.auth == Auth.LOGOUT) {
        authState.authPersistentData = null;
      }
      
      if (authState.auth == Auth.AUTHORIZED || authState.auth == null) {
        authState.auth = Auth.UNAUTHORIZED;
      }

      if (req.getUserPrincipal() == null) {
        final LoginFragment loginFragment = new LoginFragment(text, authRedirectPath);
        
        // restore preserved username
        if (authState.auth == Auth.LOGIN_FALIED && authState.username != null) {
          loginFragment.inputUsername.setAttribute("value", authState.username);
          loginFragment.inputPassword.addClass("requires-focus");
        } else {
          loginFragment.inputUsername.addClass("requires-focus");
        }
        
        final El content = new El("div");
        content.appendChild(loginFragment);
        
        page.setContent(content);
        page.setBodyAttributes("onload", "jtm_onload();authFragmentLogin_onload();", "class", "background_gray");
        
      } else {
        authState.auth = Auth.FORBIDDEN;
        
        final ForbiddenFragment forbiddenFragment = new ForbiddenFragment(text, authRedirectPath, req.getUserPrincipal().getName());
        
        final El content = new El("div");
        content.appendChild(forbiddenFragment);
        
        page.setContent(content);
        page.setBodyAttributes("onload", "jtm_onload();", "class", "background_gray");
        
        page.setButtonLogout(authRedirectPath);
      }
      
      page.setStatusBar(createStatusBar(text, authState));
      
      // reset a disposable state
      if (authState.auth == Auth.LOGIN_FALIED 
          || authState.auth == Auth.LOGOUT) {
        authState.auth = Auth.UNAUTHORIZED;
      }
      authState.username = null;
    }
    
  }
  
  protected Object getAuthPersistentData(HttpServletRequest req) {
    return AuthUtils.getAuthState(req).authPersistentData;
  }
  
  protected void setAuthPersistentData(HttpServletRequest req, Object data) {
    AuthUtils.getAuthState(req).authPersistentData = data;
  }
}
