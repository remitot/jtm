package org.jepria.web.ssr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.jepria.web.ssr.PageHeader.CurrentMenuItem;

/*package*/class PageBuilderImpl implements JtmPageBuilder {
  
  private final Context context;
  
  private String title;
  private CurrentMenuItem currentMenuItem;
  private String managerApacheHref;
  private StatusBar statusBar;
  private String logoutActionUrl;
  
  private Iterable<El> content;
  private String bodyOnload;
  
  public PageBuilderImpl(Context context) {
    this.context = context;
  }
  
  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public void setCurrentMenuItem(CurrentMenuItem currentMenuItem) {
    this.currentMenuItem = currentMenuItem;
  }

  @Override
  public void setManagerApache(String managerApacheHref) {
    this.managerApacheHref = managerApacheHref;
  }

  @Override
  public void setContent(Iterable<El> content, String bodyOnload) {
    this.content = content;
    this.bodyOnload = bodyOnload;
  }
  
  @Override
  public void setContent(El content, String bodyOnload) {
    List<El> contentList = new ArrayList<>();
    contentList.add(content);
    setContent(contentList, bodyOnload);
  }
  
  @Override
  public void setButtonLogout(String logoutActionUrl) {
    this.logoutActionUrl = logoutActionUrl;
  }

  @Override
  public void setStatusBar(StatusBar statusBar) {
    this.statusBar = statusBar;
  }

  private boolean isBuilt = false;
  
  @Override
  public Page build() {
    if (isBuilt) {
      throw new IllegalStateException("The page has already been built");
    }
    
    final PageImpl page = createHtmlPage();
    
    if (title != null) {
      page.head.appendChild(new El("title", context).setInnerHTML(title));
    }
    
    page.head.appendChild(new El("meta", context).setAttribute("http-equiv", "X-UA-Compatible").setAttribute("content", "IE=Edge"))
        .appendChild(new El("meta", context).setAttribute("http-equiv", "Content-Type").setAttribute("content", "text/html;charset=UTF-8"));
    
    if (currentMenuItem != null || logoutActionUrl != null || managerApacheHref != null) {
      final PageHeader pageHeader = new PageHeader(context, currentMenuItem);
      
      if (managerApacheHref != null) {
        pageHeader.setManagerApache(managerApacheHref);
      }
      
      if (logoutActionUrl != null) {
        pageHeader.setButtonLogout(logoutActionUrl);
      }
      
      page.body.appendChild(pageHeader);
    }
    
    if (statusBar != null) {
      page.body.appendChild(statusBar);
    }
    
    if (content != null) {
      El bodyContent = new El("div", context);
      page.body.appendChild(bodyContent);
      
      for (El el: content) {
        bodyContent.appendChild(el);
      }
    }
    
    if (bodyOnload != null) {
      page.body.setAttribute("onload", bodyOnload);
    }
     
    // add all scripts and styles to the head
    for (String style: page.body.getStyles()) {
      page.head.appendChild(new El("link", context).setAttribute("rel", "stylesheet").setAttribute("href", style));
    }
    for (String script: page.body.getScripts()) {
      page.head.appendChild(new El("script", context).setAttribute("type", "text/javascript").setAttribute("src", script));
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
      root = new El("html", context);
      head = new El("head", context);
      body = new El("body", context);
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