package org.jepria.web.ssr;

import javax.servlet.http.HttpServletRequest;

public interface Context {
  Text getText();
  /**
   * Application context path relative to the domain, e.g. {@code /manager-ext}
   * @return default value must be an empty string
   */
  String getAppContextPath();

  /**
   * Servlet context path relative to the application context path, e.g. {@code /jdbc}
   * @return
   */
  String getServletContextPath();

  /**
   * Full URL, does not include query string 
   * @return
   */
  String getRequestURL();
  
  public static Context get(HttpServletRequest request, Text text) {
    return new ContextImpl(request, text);
  }
  
  public static Context get(HttpServletRequest request, String bundleBaseName) {
    return new ContextImpl(request, bundleBaseName);
  }
  
  public static Context get(HttpServletRequest request) {
    return new ContextImpl(request);
  }
}
