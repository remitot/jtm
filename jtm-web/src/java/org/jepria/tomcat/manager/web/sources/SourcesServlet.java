package org.jepria.tomcat.manager.web.sources;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
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

    final Context context = Context.get(req, "text/org_jepria_tomcat_manager_web_Text");

    HtmlPageBaseBuilder pageBuilder = HtmlPageBaseBuilder.newInstance(context);
    pageBuilder.setTitle(context.getText().getString("org.jepria.tomcat.manager.web.sources.title"));

    StringBuilder preContent = new StringBuilder();
    preContent.append("Context configuration:   ").append(env.getContextXml().toString()).append("\n");
    preContent.append("Server configuration:    ").append(env.getServerXml().toString()).append("\n");
    preContent.append("Logs directory:          ").append(env.getLogsDirectory().toString()).append("\n");

    El pre = new El("pre", context);
    pre.setInnerHTML(preContent.toString(), true);

    pageBuilder.getBody().appendChild(pre);

    pageBuilder.build().respond(resp);
  }

}
