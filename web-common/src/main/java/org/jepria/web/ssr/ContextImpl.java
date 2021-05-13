package org.jepria.web.ssr;

import javax.servlet.http.HttpServletRequest;

/*package*/class ContextImpl implements Context {
  
  protected final Text text;
  
  protected final String appContextPath;
  protected final String servletContextPath;
  
  protected final String requestURL;

  @Override
  public Text getText() {
    return text;
  }

  @Override
  public String getAppContextPath() {
    return appContextPath;
  }

  @Override
  public String getServletContextPath() {
    return servletContextPath;
  }

  @Override
  public String getRequestURL() {
    return requestURL;
  }
  
  public ContextImpl(Text text, String appContextPath, String servletContextPath, String requestURL) {
    this.text = text;
    this.appContextPath = appContextPath == null ? "" : appContextPath;
    this.servletContextPath = servletContextPath == null ? "" : servletContextPath;
    this.requestURL = requestURL == null ? "" : requestURL;
  }
  
  public ContextImpl(HttpServletRequest request, Text text) {
    this(text, request.getContextPath(), getServletContextPath(request), request.getRequestURL().toString());
  }
  
  protected static String getServletContextPath(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    String pathInfo = request.getPathInfo() != null ? request.getPathInfo() : "";
    // requestURI = /contextPath + /servletContextPath + /pathInfo
    return requestURI.substring(request.getContextPath().length(), requestURI.length() - pathInfo.length());
  }
  
  public ContextImpl(HttpServletRequest request, String bundleBaseName) {
    this(request, Texts.get(request, bundleBaseName));
  }
  
  public ContextImpl(HttpServletRequest request) {
    this(request, Texts.getCommon(request));
  }
}
