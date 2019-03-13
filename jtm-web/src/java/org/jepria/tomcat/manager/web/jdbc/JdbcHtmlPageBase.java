package org.jepria.tomcat.manager.web.jdbc;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.HtmlPage;
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

    setTitle("Tomcat manager: датасорсы (JDBC)"); // NON-NLS
    setPageHeader(new PageHeader(managerApacheHref, CurrentMenuItem.JDBC));
  }
}
