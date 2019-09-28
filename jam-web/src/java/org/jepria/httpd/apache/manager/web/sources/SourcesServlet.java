package org.jepria.httpd.apache.manager.web.sources;

import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlPageBaseBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SourcesServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final Environment env = EnvironmentFactory.get(req);

    final Context context = Context.get(req, "text/org_jepria_httpd_apache_manager_web_Text");

    HtmlPageBaseBuilder pageBuilder = HtmlPageBaseBuilder.newInstance(context);
    pageBuilder.setTitle(context.getText().getString("org.jepria.httpd.apache.manager.web.sources.title"));

    StringBuilder preContent = new StringBuilder();
    preContent.append("mod_jk configuration:    ").append(env.getMod_jk_confFile().toString()).append("\n");
    preContent.append("workers properties:      ").append(env.getWorkers_propertiesFile().toString()).append("\n");
    preContent.append("Apache service name:     ").append(env.getApacheServiceName()).append("\n");

    El pre = new El("pre", context);
    pre.setInnerHTML(preContent.toString(), true);

    pageBuilder.getBody().appendChild(pre);

    pageBuilder.build().respond(resp);
  }

}
