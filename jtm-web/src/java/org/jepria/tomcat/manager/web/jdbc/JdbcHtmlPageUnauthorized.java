package org.jepria.tomcat.manager.web.jdbc;

import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.web.ssr.LoginFragment;

public class JdbcHtmlPageUnauthorized extends JdbcHtmlPageBase {
  
  public JdbcHtmlPageUnauthorized(Environment env) {
    super(env);
  }
  
  public JdbcHtmlPageUnauthorized(HttpServletRequest request) {
    this(EnvironmentFactory.get(request));
    
    final LoginFragment loginFragment = new LoginFragment();
    getBodyChilds().add(loginFragment);
    
    // add onload scripts
    body.setAttribute("onload", "loginFragment_onload();pageHeader_onload();");
    
    body.addClass("login-background");
  }
}
