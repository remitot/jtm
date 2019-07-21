package org.jepria.httpd.apache.manager.web.jk;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.SsrServletBase;

public class JkSsrServlet extends SsrServletBase {

  private static final long serialVersionUID = -5587074686993550317L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    final Context context = Context.get(req);
    
    resp.sendRedirect(context.getContextPath() + "/jk/mod_jk");
  }
}
