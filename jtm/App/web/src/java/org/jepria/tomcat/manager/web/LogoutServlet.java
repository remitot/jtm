package org.jepria.tomcat.manager.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoutServlet extends HttpServlet {

  private static final long serialVersionUID = 5808020075908523066L;
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.logout();
    req.getSession().invalidate();
    
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.flushBuffer();
    return;
  }

}
