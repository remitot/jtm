package org.jepria.tomcat.manager.web.jdbc;

import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.web.ssr.Node;

public class JdbcHtmlPageUnauthorized extends JdbcHtmlPageBase {
  
  public JdbcHtmlPageUnauthorized(Environment env) {
    super(env);
  }
  
  public JdbcHtmlPageUnauthorized(HttpServletRequest request) {
    this(EnvironmentFactory.get(request));
    
    body.appendChild(Node.fromHtml("unauthorized"));
  }
}
