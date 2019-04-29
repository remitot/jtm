package org.jepria.web.ssr;

import org.jepria.web.ssr.PageHeader.CurrentMenuItem;

public interface JtmPageBuilder extends HtmlPageBuilder {
  void setCurrentMenuItem(CurrentMenuItem currentMenuItem);
  void setManagerApache(String managerApacheHref);
  /**
   * @param logoutRedirectPath path to redirect after a successful logout.
   * If {@code null}, no redirect will be performed
   */
  void setButtonLogout(String logoutRedirectPath);
  void setStatusBar(StatusBar statusBar);
  
  public static JtmPageBuilder newInstance(Text text) {
    return new JtmPageBuilderImpl(text);
  }
}
