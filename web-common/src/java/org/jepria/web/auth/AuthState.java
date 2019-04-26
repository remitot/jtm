package org.jepria.web.auth;

import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.jepria.web.auth.AuthServletBase.Auth;

/**
 * Authentication state stored into a session
 */
public class AuthState {
  public Auth auth;
  public String username;
  public Object authPersistentData;
  
  
  public static AuthState get(HttpServletRequest request) {
    return get(request, () -> new AuthState());
  }
  
  /**
   * @param request
   * @param orElse an auth state instance to set and return if the current auth state is null 
   * @return
   */
  public static AuthState get(HttpServletRequest request, Supplier<AuthState> orElse) {
    AuthState state = (AuthState)request.getSession().getAttribute("org.jepria.tomcat.manager.web.SessionAttributes.authState");
    if (state == null) {
      state = orElse == null ? null : orElse.get();
      request.getSession().setAttribute("org.jepria.tomcat.manager.web.SessionAttributes.authState", state);
    }
    return state;
  }
}
