package org.jepria.tomcat.manager.web;

import java.io.IOException;

import org.jepria.web.ssr.El;

public class HtmlPage extends El {
  
  protected final El head;
  protected final El body;
  
  public HtmlPage() {
    super("html");
    
    head = new El("head")
        .appendChild(new El("meta").setAttribute("http-equiv", "X-UA-Compatible").setAttribute("content", "IE=Edge"))
        .appendChild(new El("meta").setAttribute("http-equiv", "Content-Type").setAttribute("content", "text/html;charset=UTF-8"));
    
    body = new El("body");

    appendChild(head);
    appendChild(body);
    
  }
  
  @Override
  public void render(Appendable sb) throws IOException {
    
    // add all scripts and styles to the head
    for (String style: body.getStyles()) {
      head.appendChild(new El("link").setAttribute("rel", "stylesheet").setAttribute("href", style));
    }
    for (String script: body.getScripts()) {
      head.appendChild(new El("script").setAttribute("type", "text/javascript").setAttribute("src", script));
    }
    
    sb.append("<!DOCTYPE html>");
    super.render(sb);
  }
}
