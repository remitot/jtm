package org.jepria.web.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.web.HttpDataEncoding;

/**
 * The servlet must be mapped both to {@code /login} and {@code /logout} paths
 */
public class AuthServletBase extends HttpServlet {

  private static final long serialVersionUID = -4760089680493871337L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // basically for debug purposes
    doPost(req, resp);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    // servlet context path
    final String path;
    {
      String uri = req.getRequestURI();
      String ctx = req.getContextPath();
      path = uri.substring(uri.indexOf(ctx) + ctx.length());
    }
    
    if (path != null && (path.equals("/login") || path.startsWith("/login/"))) {
      
      boolean loginSuccess = login(req);
      
      final String redirect = req.getParameter("redirect");

      if (redirect != null) {
        resp.sendRedirect(redirect);

      } else {
        if (loginSuccess) {
          resp.setStatus(HttpServletResponse.SC_OK);
        } else {
          resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
      }
      
    } else if (path != null && (path.equals("/logout") || path.startsWith("/logout/"))) {
      
      logout(req);

      final String redirect = req.getParameter("redirect");

      if (redirect != null) {
        resp.sendRedirect(redirect);

      } else {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
      }
      
    } else {
      // unknown request
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported request path [" + path + "]");
      resp.flushBuffer();
      return;
    }
    
    resp.flushBuffer();
    return;
  }
  
  protected boolean login(HttpServletRequest req) throws IOException {
    
    final String username = HttpDataEncoding.getParameterUtf8(req, "username");
    final String password = HttpDataEncoding.getParameterUtf8(req, "password");
    
    final AuthState authState = AuthState.get(req); 
    
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
    AuthState.get(req, () -> authState);
    
    return loginSuccess;
  }
  
  protected void logout(HttpServletRequest req) throws ServletException {
    req.logout();
    req.getSession().invalidate();
    
    AuthState.get(req).auth = Auth.LOGOUT;
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
}
