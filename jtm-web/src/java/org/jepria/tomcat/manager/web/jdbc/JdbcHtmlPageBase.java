package org.jepria.tomcat.manager.web.jdbc;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.HtmlPage;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;

public class JdbcHtmlPageBase extends HtmlPage {

  public JdbcHtmlPageBase(Environment env) {
    final String managerApacheHref;
    if (env != null) {
      managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    } else {
      managerApacheHref = null;
    }

    
    head.appendChild(new El("title").setInnerHTML("Tomcat manager: датасорсы (JDBC)")) // NON-NLS
    .appendChild(new El("meta").setAttribute("http-equiv", "X-UA-Compatible").setAttribute("content", "IE=Edge"))
    .appendChild(new El("meta").setAttribute("http-equiv", "Content-Type").setAttribute("content", "text/html;charset=UTF-8"));

    body.setAttribute("onload", "jtm_onload();table_onload();checkbox_onload();controlButtons_onload();");
    final PageHeader pageHeader = new PageHeader(managerApacheHref, CurrentMenuItem.JDBC);

    body.appendChild(pageHeader);
  }
}
