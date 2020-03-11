package org.jepria.web.ssr;

import javax.servlet.http.HttpServletRequest;

/*package*/class ContextImpl implements Context {
  
  protected final Text text;
  
  protected final String contextPath;
  
  protected final String requestURL;

  @Override
  public Text getText() {
    return text;
  }

  @Override
  public String getContextPath() {
    return contextPath;
  }
  
  @Override
  public String getRequestURL() {
    return requestURL;
  }
  
  public ContextImpl(Text text, String contextPath, String requestURL) {
    this.text = text;
    this.contextPath = contextPath == null ? "" : contextPath;
    this.requestURL = requestURL == null ? "" : requestURL;
  }
  
  public ContextImpl(HttpServletRequest request, Text text) {
    this(text, request.getContextPath(), request.getRequestURL().toString());
  }
  
  public ContextImpl(HttpServletRequest request, String bundleBaseName) {
    this(Texts.get(request, bundleBaseName), request.getContextPath(), request.getRequestURL().toString());
  }
  
  public ContextImpl(HttpServletRequest request) {
    this(Texts.getCommon(request), request.getContextPath(), request.getRequestURL().toString());
  }
}
