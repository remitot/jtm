package org.jepria.httpd.apache.manager.web.sources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlPageBaseBuilder;

public class SourcesServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final Environment env = EnvironmentFactory.get(req);

    final Context context = Context.get(req, "text/org_jepria_httpd_apache_manager_web_Text");

    HtmlPageBaseBuilder pageBuilder = HtmlPageBaseBuilder.newInstance(context);
    pageBuilder.setTitle(context.getText().getString("org.jepria.httpd.apache.manager.web.sources.title"));

    StringBuilder preContent = new StringBuilder();
    
    String modJkSourceValue = null;
    {
      try { // no throwable must be thrown on this page
        modJkSourceValue = env.getMod_jk_confFile().toString();
      } catch (Throwable e) {
        modJkSourceValue = stackTraceAsValue(e);
      }
    }
    
    String workersPropertiesSourceValue = null;
    {
      try { // no throwable must be thrown on this page
        workersPropertiesSourceValue = env.getWorkers_propertiesFile().toString();
      } catch (Throwable e) {
        workersPropertiesSourceValue = stackTraceAsValue(e);
      }
    }
    
    String apacheServiceNameSourceValue = null;
    {
      try { // no throwable must be thrown on this page
        apacheServiceNameSourceValue = env.getApacheServiceName();
      } catch (Throwable e) {
        apacheServiceNameSourceValue = stackTraceAsValue(e);
      }
    }
    
    preContent.append("mod_jk configuration:    ").append(modJkSourceValue).append("\n\n");
    preContent.append("workers properties:      ").append(workersPropertiesSourceValue).append("\n\n");
    preContent.append("Apache service name:     ").append(apacheServiceNameSourceValue).append("\n\n");

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
