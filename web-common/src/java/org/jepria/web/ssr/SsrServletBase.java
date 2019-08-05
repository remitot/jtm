package org.jepria.web.ssr;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.jepria.web.auth.AuthServletBase.Auth;
import org.jepria.web.auth.AuthState;
import org.jepria.web.auth.RedirectBuilder;
import org.jepria.web.ssr.StatusBar.Type;

public class SsrServletBase extends HttpServlet {

  private static final long serialVersionUID = 1760582345667928411L;

  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null;
  }

  private final String getStatusBarModDataSavedHtmlPostfix(Text text) {
    return "<br/><br/>"
        + text.getString("org.jepria.web.ssr.SsrServletBase.status.mod_saved.saved") 
        + ",<br/>" 
        + text.getString("org.jepria.web.ssr.SsrServletBase.status.mod_saved.restored") 
        + ".<br/><a href=\"\">" 
        + text.getString("org.jepria.web.ssr.SsrServletBase.status.mod_saved.delete")
        + "</a>";
  }

  /**
   * Show an auth fragment ({@link LoginFragment} in case of non-authenticated user 
   * or {@link ForbiddenFragment} in case of the user having not enough rights) on the page,
   * then redirect to the current request path after a successful login or logout
   * @param req current request that needs authentication
   * @param page original page that was about to be responded if the request had been authenticated
   */
  protected void requireAuth(HttpServletRequest req, HtmlPageExtBuilder page) {
    requireAuth(req, page, null);
  }

  /**
   * Show an auth fragment ({@link LoginFragment} in case of non-authenticated user 
   * or {@link ForbiddenFragment} in case of the user having not enough rights) on the page.
   * @param req current request that needs authentication
   * @param page original page that was about to be responded if the request had been authenticated
   * @param authRedirectPath path to redirect after a successful login or logout
   * If {@code null}, will redirect to the same requested resource
   */
  protected void requireAuth(HttpServletRequest req, HtmlPageExtBuilder page, String authRedirectPath) {

    final Context context = Context.get(req);

    // redirect to a current request path in case of null
    authRedirectPath = authRedirectPath != null ? authRedirectPath : RedirectBuilder.self(req);

    final AuthState authState = AuthState.get(req);

    if (authState.auth == Auth.UNAUTHORIZED || authState.auth == Auth.LOGOUT) {
      authState.authPersistentData = null;
    }

    if (authState.auth == Auth.AUTHORIZED || authState.auth == null) {
      authState.auth = Auth.UNAUTHORIZED;
    }

    final AuthFragment authFragment;
    
    if (req.getUserPrincipal() == null) {
      final LoginFragment loginFragment = new LoginFragment(context, authRedirectPath);

      // restore preserved username
      if (authState.auth == Auth.LOGIN_FALIED && authState.username != null) {
        loginFragment.inputUsername.setAttribute("value", authState.username);
        loginFragment.inputPassword.addClass("requires-focus");
      } else {
        loginFragment.inputUsername.addClass("requires-focus");
      }

      authFragment = loginFragment;

    } else {
      authState.auth = Auth.FORBIDDEN;

      authFragment = new ForbiddenFragment(context, authRedirectPath, req.getUserPrincipal().getName());
    }

    
    { // set status bar type and header
      if (authState != null) {
        final Auth auth = authState.auth;
        final Text text = context.getText();
  
        if (auth == Auth.LOGIN_FALIED) {
          
          String html = 
              "<span class=\"span-bold\">"
              + text.getString("org.jepria.web.ssr.SsrServletBase.status.login_failed.incorrect_data")
              + "</span><br/>"
              + "(" + text.getString("org.jepria.web.ssr.AuthFragment.status.access_admin") + ")";
          
          if (authState.authPersistentData != null) {
            html += getStatusBarModDataSavedHtmlPostfix(text);
          }
          
          authFragment.setType(Type.ERROR);
          authFragment.getHeader().setInnerHTML(html);
          
        } else if (auth == Auth.LOGOUT) {
          
          authFragment.setType(Type.SUCCESS);
          String html = text.getString("org.jepria.web.ssr.SsrServletBase.status.logouted");
          authFragment.getHeader().setInnerHTML(html);
          
        } else if (auth == Auth.UNAUTHORIZED) {
        
          String html =
              "<span class=\"span-bold\">"
              + text.getString("org.jepria.web.ssr.SsrServletBase.status.auth_required")
              + "</span><br/>"
              + "(" + text.getString("org.jepria.web.ssr.AuthFragment.status.access_admin") + ")";
          
          if (authState.authPersistentData != null) {
            html += getStatusBarModDataSavedHtmlPostfix(text);
          }
          
          authFragment.setType(Type.INFO);
          authFragment.getHeader().setInnerHTML(html);
        
        } else if (auth == Auth.FORBIDDEN) {
          
          String html = text.getString("org.jepria.web.ssr.SsrServletBase.status.forbidden");
          
          authFragment.setType(Type.ERROR);
          authFragment.getHeader().setInnerHTML(html);
        }
      }
    }
    
    
    page.setContent(authFragment);
    page.getBody().setAttribute("class", "background_gray");
    

    // reset a disposable state
    if (authState.auth == Auth.LOGIN_FALIED 
        || authState.auth == Auth.LOGOUT) {
      authState.auth = Auth.UNAUTHORIZED;
    }
    authState.username = null;
  }

  //////// Auth-persistent data ////////
  
  protected Object getAuthPersistentData(HttpServletRequest req) {
    return AuthState.get(req).authPersistentData;
  }

  protected void setAuthPersistentData(HttpServletRequest req, Object data) {
    AuthState.get(req).authPersistentData = data;
  }

  //////// Applicational state ////////

  protected String getAppStateSessionAttrKey() {
    return getClass().getCanonicalName() + ".SessionAttributes.appState";
  }
  
  /**
   * Class stored into a session
   */
  protected class AppState {
    public Object modRequest = null;
    public Object modStatus = null;
  }

  protected AppState getAppState(HttpServletRequest request) {
    AppState state = (AppState)request.getSession().getAttribute(getAppStateSessionAttrKey());
    if (state == null) {
      state = new AppState();
      request.getSession().setAttribute(getAppStateSessionAttrKey(), state);
    }
    return state;
  }

  protected void clearAppState(HttpServletRequest request) {
    request.getSession().removeAttribute(getAppStateSessionAttrKey());
  }

  ///////////////////////////
}
