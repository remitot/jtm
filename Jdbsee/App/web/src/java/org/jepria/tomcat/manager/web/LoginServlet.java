package org.jepria.tomcat.manager.web;

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
        
        // logout if logged in
        if (req.getUserPrincipal() != null) {
          req.logout();
        }
        
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
