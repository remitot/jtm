package org.jepria.tomcat.manager.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Node;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.StatusBar;
import org.jepria.web.ssr.table.Collection;

public class HtmlPage {
  
  protected final El root;
  protected final El head;
  protected final El body;
  
  public HtmlPage() {
    root = new El("html");
    head = new El("head");
    body = new Body();
    root.appendChild(head);
    root.appendChild(body);
    
  }
  
  private class Body extends El {
    public Body() {
      super("body");
    }
    
    @Override
    protected void addScripts(Collection scripts) {
      super.addScripts(scripts);
      scripts.add("css/jtm-common.css");
    }
  }
  
  private String title;
  private PageHeader pageHeader;
  private StatusBar statusBar;
  private List<Node> bodyChilds = new ArrayList<>();
  
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public PageHeader getPageHeader() {
    return pageHeader;
  }

  public void setPageHeader(PageHeader pageHeader) {
    this.pageHeader = pageHeader;
  }

  public StatusBar getStatusBar() {
    return statusBar;
  }

  public void setStatusBar(StatusBar statusBar) {
    this.statusBar = statusBar;
  }

  public List<Node> getBodyChilds() {
    return bodyChilds;
  }

  private void rebuild() {
    
    // clear
    head.childs.clear();
    body.childs.clear();
    
    
    // add title
    if (title != null) {
      head.appendChild(new El("title").setInnerHTML(title)); // NON-NLS
    }

    head.appendChild(new El("meta").setAttribute("http-equiv", "X-UA-Compatible").setAttribute("content", "IE=Edge"))
        .appendChild(new El("meta").setAttribute("http-equiv", "Content-Type").setAttribute("content", "text/html;charset=UTF-8"));
    
    
    // build body
    if (pageHeader != null) {
      body.appendChild(pageHeader);
    }
    
    if (statusBar != null) {
      body.appendChild(statusBar);
    }
    
    if (bodyChilds != null) {
      for (Node bodyNode: bodyChilds) {
        body.appendChild(bodyNode);
      }
    }
     
    
    // add all scripts and styles to the head
    for (String style: body.getStyles()) {
      head.appendChild(new El("link").setAttribute("rel", "stylesheet").setAttribute("href", style));
    }
    for (String script: body.getScripts()) {
      head.appendChild(new El("script").setAttribute("type", "text/javascript").setAttribute("src", script));
    }
  }

  public void respond(HttpServletResponse response) throws IOException {
    rebuild();
    
    response.setContentType("text/html; charset=UTF-8");
    response.getWriter().print(getDoctypeTag());
    root.render(response.getWriter());
    response.flushBuffer();

  }
  
  protected String getDoctypeTag() {
    return "<!DOCTYPE html>";
  }
}
