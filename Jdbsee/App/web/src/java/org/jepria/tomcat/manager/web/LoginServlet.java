package org.jepria.tomcat.manager.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The servlet does nothing (the superservlet does authentication), but returns SUCCESS or FAILURE 
 */
public class LoginServlet extends JtmSecureServlet {
  
  private static final long serialVersionUID = 7988979181448679156L;

  @Override
  protected void onLoginFailed(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.getOutputStream().print("FAILURE");
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.flushBuffer();
  }
  
  @Override
  protected void onAccessDenied(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.getOutputStream().print("FAILURE");
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.flushBuffer();
  }
  
  @Override
  protected void doPostAuth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.getOutputStream().print("SUCCESS");
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.flushBuffer();
  }
  
}
