package org.jepria.web.ssr;

import org.jepria.web.ssr.PageHeader.CurrentMenuItem;

public interface JtmPageBuilder extends HtmlPageBuilder {
  void setCurrentMenuItem(CurrentMenuItem currentMenuItem);
  void setManagerApache(String managerApacheHref);
  void setButtonLogout(String logoutActionUrl);
  void setStatusBar(StatusBar statusBar);
  
  public static JtmPageBuilder newInstance(Context context) {
    return new JtmPageBuilderImpl(context);
  }
}
