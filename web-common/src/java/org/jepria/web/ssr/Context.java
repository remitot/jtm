package org.jepria.web.ssr;

import javax.servlet.http.HttpServletRequest;

public interface Context {
  Text getText();
  /**
   * 
   * @return default value must be an empty string
   */
  String getContextPath();
  
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
