package org.jepria.web.ssr;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.jepria.web.ssr.PageHeader.CurrentMenuItem;

public interface PageBuilder {
  void setTitle(String title);
  void setCurrentMenuItem(CurrentMenuItem currentMenuItem);
  void setManagerApache(String managerApacheHref);
  El getBody();
  void setButtonLogout(String logoutActionUrl);
  void setStatusBar(StatusBar statusBar);
  Page build();
  
  public static PageBuilder newInstance(Context context) {
    return new PageBuilderImpl(context);
  }
  
  public interface Page {
    void print(PrintWriter out) throws IOException;
    void respond(HttpServletResponse response) throws IOException;
  }
  
}
