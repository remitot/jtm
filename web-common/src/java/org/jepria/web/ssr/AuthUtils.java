package org.jepria.web.ssr;

import java.io.IOException;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public class AuthUtils {

  public static boolean login(HttpServletRequest req) throws IOException {
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
  
  public static void logout(HttpServletRequest req) throws ServletException {
    req.logout();
    req.getSession().invalidate();
    
    getAuthState(req).auth = Auth.LOGOUT;
  }
  
  public static enum Auth {
    AUTHORIZED,
    /**
     * For the current module! Not application-wide
     */
    FORBIDDEN,
    UNAUTHORIZED,
    LOGIN_FALIED,
    LOGOUT,
  }
  
  /*package*/static AuthState getAuthState(HttpServletRequest request) {
    return getAuthState(request, () -> new AuthState());
  }
  
  /**
   * @param request
   * @param orElse an auth state instance to set and return if the current auth state is null 
   * @return
   */
  /*package*/static AuthState getAuthState(HttpServletRequest request, Supplier<AuthState> orElse) {
    AuthState state = (AuthState)request.getSession().getAttribute("org.jepria.tomcat.manager.web.SessionAttributes.authState");
    if (state == null) {
      state = orElse == null ? null : orElse.get();
      request.getSession().setAttribute("org.jepria.tomcat.manager.web.SessionAttributes.authState", state);
    }
    return state;
  }
  
  /**
   * Class stored into a session
   */
  /*package*/static class AuthState {
    public Auth auth;
    public String username;
    public Object authPersistentData;
  }
}
