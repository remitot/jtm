package org.jepria.web.ssr;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.jepria.web.auth.AuthServletBase.Auth;
import org.jepria.web.auth.AuthState;

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
  
  protected String getAuthRedirectPathDefault(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String context = request.getContextPath();
    
    if (uri.startsWith(context)) {
      
      StringBuilder ret = new StringBuilder();
      ret.append(uri.substring(context.length()));
      if (ret.charAt(0) == '/') {
        ret.deleteCharAt(0);
      }
      
      String qs = request.getQueryString();
      if (qs != null) {
        ret.append('?').append(qs);
      }
      
      return ret.toString();
      
    } else {
      throw new IllegalStateException("HttpServletRequest.getRequestURI() must strint with HttpServletRequest.getContextPath()");
    }
  }
  
  /**
   * Show an auth fragment ({@link LoginFragment} in case of non-authenticated user 
   * or {@link ForbiddenFragment} in case of the user having not enough rights) on the page,
   * then redirect to the current request path after a successful login or logout
   * @param req current request that needs authentication
   * @param page original page that was about to be responded if the request had been authenticated
   */
  protected void requireAuth(HttpServletRequest req, JtmPageBuilder page) {
    requireAuth(req, page, null);
  }
  
  /**
   * Show an auth fragment ({@link LoginFragment} in case of non-authenticated user 
   * or {@link ForbiddenFragment} in case of the user having not enough rights) on the page.
   * @param req current request that needs authentication
   * @param page original page that was about to be responded if the request had been authenticated
   * @param authRedirectPath path to redirect after a successful login or logout
   * If {@code null}, the redirect path is the current request path
   */
  protected void requireAuth(HttpServletRequest req, JtmPageBuilder page, String authRedirectPath) {
    
    // redirect to a current request path in case of null
    authRedirectPath = authRedirectPath != null ? authRedirectPath : getAuthRedirectPathDefault(req);
    
    final Text text = Texts.getCommon(req);
    
    final AuthState authState = AuthState.get(req);
    
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
    
  protected Object getAuthPersistentData(HttpServletRequest req) {
    return AuthState.get(req).authPersistentData;
  }
  
  protected void setAuthPersistentData(HttpServletRequest req, Object data) {
    AuthState.get(req).authPersistentData = data;
  }
}
