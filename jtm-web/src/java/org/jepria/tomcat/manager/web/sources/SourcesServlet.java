package org.jepria.tomcat.manager.web.sources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlPageBaseBuilder;

public class SourcesServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final Environment env = EnvironmentFactory.get(req);

    final Context context = Context.get(req, "text/org_jepria_tomcat_manager_web_Text");

    HtmlPageBaseBuilder pageBuilder = HtmlPageBaseBuilder.newInstance(context);
    pageBuilder.setTitle(context.getText().getString("org.jepria.tomcat.manager.web.sources.title"));

    StringBuilder preContent = new StringBuilder();
    
    String contextXmlSourceValue = null;
    {
      try { // no throwable must be thrown on this page
        contextXmlSourceValue = env.getContextXml().toString();
      } catch (Throwable e) {
        contextXmlSourceValue = stackTraceAsValue(e);
      }
    }
    
    String serverXmlSourceValue = null;
    {
      try { // no throwable must be thrown on this page
        serverXmlSourceValue = env.getServerXml().toString();
      } catch (Throwable e) {
        serverXmlSourceValue = stackTraceAsValue(e);
      }
    }
    
    String logsDirectorySourceValue = null;
    {
      try { // no throwable must be thrown on this page
        logsDirectorySourceValue = env.getLogsDirectory().toString();
      } catch (Throwable e) {
        logsDirectorySourceValue = stackTraceAsValue(e);
      }
    }
    
    preContent.append("Context configuration:   ").append(contextXmlSourceValue).append("\n");
    preContent.append("Server configuration:    ").append(serverXmlSourceValue).append("\n");
    preContent.append("Logs directory:          ").append(logsDirectorySourceValue).append("\n");

    El pre = new El("pre", context);
    pre.setInnerHTML(preContent.toString(), true);

    pageBuilder.getBody().appendChild(pre);

    pageBuilder.build().respond(resp);
  }
  
  /**
   * Pretty-prints stacktrace of the Throwable as a value of corresponding source
   * @param e
   * @return
   */
  protected String stackTraceAsValue(Throwable e) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    e.printStackTrace(new PrintStream(baos));
    String stackTrace = baos.toString();
    if (stackTrace != null && stackTrace.endsWith("\n")) {
      stackTrace = stackTrace.substring(0, stackTrace.length() - 1);
    }
    String ret = "<Failed to get value, stacktrace below>\n" + 
        "                         " + stackTrace.replaceAll("\n\t", "\n" + "                         " + "\t");
    return ret;
  }

}
