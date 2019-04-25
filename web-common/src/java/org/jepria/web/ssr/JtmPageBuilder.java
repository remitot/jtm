package org.jepria.web.ssr;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.jepria.web.ssr.PageHeader.CurrentMenuItem;

public interface JtmPageBuilder {
  void setTitle(String title);
  void setCurrentMenuItem(CurrentMenuItem currentMenuItem);
  void setManagerApache(String managerApacheHref);
  /**
   * Has the same effect as if {@code content} elements were subsequently appended to the HTML &lt;body&gt; tag
   * @param content list of {@code body} child elements 
   * @param bodyOnload value of the {@code body} {@code onload} attribute, depending on the {@code content}
   */
  void setContent(Iterable<El> content, String bodyOnload);
  /**
   * Has the same effect as if {@code content} element was appended to the HTML &lt;body&gt; tag
   * @param content a {@code body} child element 
   * @param bodyOnload value of the {@code body} {@code onload} attribute, depending on the {@code content}
   */
  void setContent(El content, String bodyOnload);
  void setButtonLogout(String logoutActionUrl);
  void setStatusBar(StatusBar statusBar);
  Page build();
  
  public static JtmPageBuilder newInstance(Context context) {
    return new PageBuilderImpl(context);
  }
  
  public interface Page {
    void print(PrintWriter out) throws IOException;
    void respond(HttpServletResponse response) throws IOException;
  }
  
}
