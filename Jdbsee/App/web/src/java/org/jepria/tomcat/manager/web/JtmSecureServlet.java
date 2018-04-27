package org.jepria.tomcat.manager.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JtmSecureServlet extends HttpServlet {
  
  private static final long serialVersionUID = -2990837522249446367L;

  private static boolean authenticate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    String username = req.getParameter("username");
    String password = req.getParameter("password");
    
    if (username != null && password != null) {
      try {
        
        // logout if logged in
        if (req.getUserPrincipal() != null) {
          req.logout();
        }
        
        req.login(username, password);
        
      } catch (ServletException e) {
        e.printStackTrace();
        
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.flushBuffer();
        return false;
      }
    }
    
    
    if (req.getUserPrincipal() == null) {
      // unauthorized
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      resp.flushBuffer();
      return false;
    }

    
    String securityRoleName = req.getServletContext().getInitParameter("jtm.security-role"); 
    if (!req.isUserInRole(securityRoleName)) {
      // forbidden
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
      resp.flushBuffer();
      return false;
    }
    
    return true;
  }
  
  @Override
  protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (authenticate(req, resp)) {
      doDeleteAuth(req, resp);
    }
  }
  
  protected void doDeleteAuth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doDelete(req, resp);
  }
  
  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (authenticate(req, resp)) {
      doGetAuth(req, resp);
    }
  }
  
  protected void doGetAuth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doGet(req, resp);
  }
  
  @Override
  protected final void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (authenticate(req, resp)) {
      doHeadAuth(req, resp);
    }
  }
  
  protected void doHeadAuth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doHead(req, resp);
  }
  
  @Override
  protected final void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (authenticate(req, resp)) {
      doOptionsAuth(req, resp);
    }
  }
  
  protected void doOptionsAuth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doOptions(req, resp);
  }
  
  @Override
  protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (authenticate(req, resp)) {
      doPostAuth(req, resp);
    }
  }
  
  protected void doPostAuth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doPost(req, resp);
  }
  
  @Override
  protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (authenticate(req, resp)) {
      doPutAuth(req, resp);
    }
  }
  
  protected void doPutAuth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doPut(req, resp);
  }
  
  @Override
  protected final void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (authenticate(req, resp)) {
      doTraceAuth(req, resp);
    }
  }
  
  protected void doTraceAuth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doTrace(req, resp);
  }
}
