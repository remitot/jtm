package org.jepria.web.ssr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/*package*/class HtmlPageBaseBuilderImpl implements HtmlPageBaseBuilder {
  
  protected String title;
  
  protected Iterable<? extends Node> content;
  
  protected Map<String, String> bodyAttributes;
  
  public HtmlPageBaseBuilderImpl() {}
  
  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public void setContent(Iterable<? extends Node> content) {
    this.content = content;
  }
  
  @Override
  public void setContent(Node content) {
    List<Node> contentList = new ArrayList<>();
    contentList.add(content);
    setContent(contentList);
  }
  

  @Override
  public void setBodyAttributes(Map<String, String> attributes) {
    this.bodyAttributes = attributes;
  }
  
  @Override
  public void setBodyAttributes(String... attributes) {
    if (attributes != null) {
      if (attributes.length % 2 != 0) {
        throw new IllegalArgumentException("Expected attributes array of an even length");
      }
      
      Map<String, String> map = new HashMap<>();
      for (int i = 0; i < attributes.length; i += 2) {
        map.put(attributes[i], attributes[i + 1]);
      }
      
      setBodyAttributes(map);
    }
  }
  
  private boolean isBuilt = false;
  
  @Override
  public Page build() {
    if (isBuilt) {
      throw new IllegalStateException("The page has already been built");
    }
    
    final PageImpl page = createHtmlPage();
    
    if (title != null) {
      page.head.appendChild(new El("title").setInnerHTML(title));
    }
    
    page.head.appendChild(new El("meta").setAttribute("http-equiv", "X-UA-Compatible").setAttribute("content", "IE=Edge"))
        .appendChild(new El("meta").setAttribute("http-equiv", "Content-Type").setAttribute("content", "text/html;charset=UTF-8"));
    
    if (content != null) {
      El bodyContent = new El("div");
      page.body.appendChild(bodyContent);
      
      for (Node node: content) {
        bodyContent.appendChild(node);
      }
    }
    
    if (bodyAttributes != null) {
      for (Map.Entry<String, String> attribute: bodyAttributes.entrySet()) {
        page.body.setAttribute(attribute.getKey(), attribute.getValue());
      }
    }
     
    // add all scripts and styles to the head
    for (String style: page.body.getStyles()) {
      page.head.appendChild(new El("link").setAttribute("rel", "stylesheet").setAttribute("href", style));
    }
    for (String script: page.body.getScripts()) {
      page.head.appendChild(new El("script").setAttribute("type", "text/javascript").setAttribute("src", script));
    }

    isBuilt = true;
    
    return page;
  }
  
  protected PageImpl createHtmlPage() {
    return new PageImpl();
  }
  
  protected class PageImpl implements Page {
    protected final El root;
    protected final El head;
    protected final El body;
    
    public PageImpl() {
      root = new El("html");
      head = new El("head");
      body = new El("body");
      root.appendChild(head);
      root.appendChild(body);
    }
    
    @Override
    public void respond(HttpServletResponse response) throws IOException {
      response.setContentType("text/html; charset=UTF-8");
      print(response.getWriter());
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