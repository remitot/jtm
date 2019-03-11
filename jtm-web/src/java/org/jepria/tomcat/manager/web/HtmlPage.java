package org.jepria.tomcat.manager.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jepria.web.ssr.El;

// TODO better to move to web-common, but it requeires servlet-api dependency...
public class HtmlPage extends El {
  
  protected final El head;
  protected final El body;
  
  public HtmlPage() {
    super("html");
    
    head = new El("head");
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
  
  public void response(HttpServletResponse response) throws IOException {
    response.setContentType("text/html; charset=UTF-8");
    render(response.getWriter());
    response.flushBuffer();
  }
}
