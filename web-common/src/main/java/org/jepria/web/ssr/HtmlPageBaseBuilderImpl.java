package org.jepria.web.ssr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/*package*/class HtmlPageBaseBuilderImpl implements HtmlPageBaseBuilder {
  
  protected final El root;
  protected final El head;
  protected final El body;
  
  protected String title;
  
  protected Iterable<? extends Node> content;
  
  protected Map<String, String> bodyAttributes;
  
  protected final Context context;
  
  public HtmlPageBaseBuilderImpl(Context context) {
    this.context = context;
    
    {
      root = new El("html", context);
      head = new El("head", context);
      body = new El("body", context);
    }
    {
      root.appendChild(head);
      root.appendChild(body);
    }
  }
  
  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public El getBody() {
    // TODO restrict some modifications on the returned element here, e.g. setAttribute("onload", ...)
    return body;
  }
  
  private boolean isBuilt = false;
  
  @Override
  public Page build() {
    if (isBuilt) {
      throw new IllegalStateException("The page has already been built");
    }
    
    final PageImpl page = createHtmlPage();
    
    if (title != null) {
      head.appendChild(new El("title", context).setInnerHTML(title));
    }
    
    head.appendChild(new El("meta", context).setAttribute("http-equiv", "X-UA-Compatible").setAttribute("content", "IE=Edge"))
        .appendChild(new El("meta", context).setAttribute("http-equiv", "Content-Type").setAttribute("content", "text/html;charset=UTF-8"));
    
    if (content != null) {
      El bodyContent = new El("div", context);
      body.appendChild(bodyContent);
      
      for (Node node: content) {
        bodyContent.appendChild(node);
      }
    }
    
    if (bodyAttributes != null) {
      for (Map.Entry<String, String> attribute: bodyAttributes.entrySet()) {
        body.setAttribute(attribute.getKey(), attribute.getValue());
      }
    }
     
    // add all scripts and styles to the head
    for (String style: body.getStyles()) {
      String href = context.getAppContextPath() + "/" + style;
      head.appendChild(new El("link", context).setAttribute("rel", "stylesheet").setAttribute("href", href));
    }
    for (String script: body.getScripts()) {
      String src = context.getAppContextPath() + "/" + script;
      head.appendChild(new El("script", context).setAttribute("type", "text/javascript").setAttribute("src", src));
    }
    // add all onload functions to the body onload attribute
    StringBuilder onload = new StringBuilder();
    for (String onloadFunction: body.getOnloadFunctions()) {
      onload.append(onloadFunction);
      if (!onloadFunction.trim().endsWith("();")) {
        onload.append("();");
      }
    }
    body.setAttribute("onload", onload.toString());

    isBuilt = true;
    
    return page;
  }
  
  protected PageImpl createHtmlPage() {
    return new PageImpl();
  }
  
  protected class PageImpl implements Page {
    
    @Override
    public void respond(HttpServletResponse response) throws IOException {
      response.setContentType("text/html; charset=UTF-8");
      
      try (PrintWriter writer = response.getWriter()) {
        // PrintWriter encoding is UTF-8, as set above
        print(writer);
      }
      
      response.flushBuffer();
    }
    
    protected String getDoctypeTag() {
      return "<!DOCTYPE html>";
    }

    @Override
    public void print(PrintWriter out) throws IOException {
      out.print(getDoctypeTag());
      root.render(out);
    }
  }
  
}