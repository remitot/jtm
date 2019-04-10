package org.jepria.web.ssr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

public class HtmlPage {
  
  protected final Context context;
  
  protected final El root;
  protected final El head;
  protected final El body;
  
  public HtmlPage(Context context) {
    this.context = context;
    
    root = new El("html", context);
    head = new El("head", context);
    body = new El("body", context);
    root.appendChild(head);
    root.appendChild(body);
    
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
      head.appendChild(new El("title", context).setInnerHTML(title));
    }

    head.appendChild(new El("meta", context).setAttribute("http-equiv", "X-UA-Compatible").setAttribute("content", "IE=Edge"))
        .appendChild(new El("meta", context).setAttribute("http-equiv", "Content-Type").setAttribute("content", "text/html;charset=UTF-8"));
    
    
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
      head.appendChild(new El("link", context).setAttribute("rel", "stylesheet").setAttribute("href", style));
    }
    for (String script: body.getScripts()) {
      head.appendChild(new El("script", context).setAttribute("type", "text/javascript").setAttribute("src", script));
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
