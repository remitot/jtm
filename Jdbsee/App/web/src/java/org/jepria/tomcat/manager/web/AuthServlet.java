package org.jepria.tomcat.manager.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthServlet extends HttpServlet {
  
  private static final long serialVersionUID = 7988979181448679156L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    resp.setContentType("text/plain; charset=UTF-8");
    
    String username = req.getParameter("username");
    String password = req.getParameter("password");
    
    try {
      req.login(username, password);
      
      // login success
      resp.getOutputStream().print("SUCCESS");
      
    } catch (Throwable e) {
      e.printStackTrace();
      
      // login failure
      resp.getOutputStream().print("FAILURE");
    }
    
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.flushBuffer();
    return;
  }
  
}
