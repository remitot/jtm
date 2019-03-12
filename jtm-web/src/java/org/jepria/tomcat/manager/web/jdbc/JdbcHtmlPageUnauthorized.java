package org.jepria.tomcat.manager.web.jdbc;

import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.web.ssr.LoginFragment;
import org.jepria.web.ssr.Status;
import org.jepria.web.ssr.StatusBar;

public class JdbcHtmlPageUnauthorized extends JdbcHtmlPageBase {
  
  public JdbcHtmlPageUnauthorized(Environment env) {
    super(env);
  }
  
  public JdbcHtmlPageUnauthorized(HttpServletRequest request, boolean loginAlreadyFailed) {
    this(EnvironmentFactory.get(request));
    
    if (loginAlreadyFailed) {
      final String statusBarHTML = "<span class=\"span-bold\">Неверные данные, попробуйте ещё раз.</span>"; // NON-NLS
      StatusBar statusBar = new StatusBar(Status.Type.ERROR, statusBarHTML);
      body.appendChild(statusBar);
    }
    
    final LoginFragment loginFragment = new LoginFragment();
    body.appendChild(loginFragment);
    
    // add onload scripts
    body.setAttribute("onload", "loginFragment_onload();");
    
    body.addClass("login-background");
    
    request.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.servletLoginStatus.failure");
  }
}
