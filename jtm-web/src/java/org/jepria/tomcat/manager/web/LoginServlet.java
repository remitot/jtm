package org.jepria.tomcat.manager.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.web.ssr.AuthUtils;

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

    boolean success = AuthUtils.login(req);

    final String redirect = req.getParameter("redirect");

    if (redirect != null) {
      resp.sendRedirect(redirect);

    } else {
      if (success) {
        resp.setStatus(HttpServletResponse.SC_OK);
      } else {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      }
      resp.flushBuffer();
    }
  }
}
