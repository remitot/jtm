package org.jepria.web.ssr;

import javax.servlet.http.HttpServletRequest;

/*package*/class ContextImpl implements Context {
  
  protected final Text text;
  
  protected final String contextPath;

  @Override
  public Text getText() {
    return text;
  }

  @Override
  public String getContextPath() {
    return contextPath;
  }

  public ContextImpl(Text text, String contextPath) {
    this.text = text;
    this.contextPath = contextPath == null ? "" : contextPath;
  }
  
  public ContextImpl(HttpServletRequest request, Text text) {
    this(text, request.getContextPath());
  }
  
  public ContextImpl(HttpServletRequest request, String bundleBaseName) {
    this(Texts.get(request, bundleBaseName), request.getContextPath());
  }
  
  public ContextImpl(HttpServletRequest request) {
    this(Texts.getCommon(request), request.getContextPath());
  }
}
