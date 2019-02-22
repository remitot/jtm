package org.jepria.httpd.apache.manager.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginServlet extends HttpServlet {
  
  private static final long serialVersionUID = 7988979181448679156L;
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    String username = req.getParameter("username");
    String password = req.getParameter("password");
    
    if (username == null || "".equals(username) 
        || password == null || "".equals(password)) {
      
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      resp.flushBuffer();
      return;
    }
    
    if (username != null && password != null) {
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
        
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
        return;
        
      } catch (ServletException e) {
        e.printStackTrace();
        
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        resp.flushBuffer();
        return;
      }
    }
    
    
    if (req.getUserPrincipal() == null) {
      // unauthorized
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      resp.flushBuffer();
      return;
    }
  }
}
