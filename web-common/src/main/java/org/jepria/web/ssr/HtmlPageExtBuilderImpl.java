package org.jepria.web.ssr;

import java.util.ArrayList;
import java.util.Arrays;

/*package*/class HtmlPageExtBuilderImpl extends HtmlPageBaseBuilderImpl implements HtmlPageExtBuilder {
  
  protected PageHeader header;
  protected StatusBar statusBar;
  
  protected boolean setLogout = false;
  protected String logoutRedirectPath;
  
  public HtmlPageExtBuilderImpl(Context context) {
    super(context);
  }
  
  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public void setHeader(PageHeader header) {
    this.header = header;
    buildBody();
  }

  @Override
  public void setStatusBar(StatusBar statusBar) {
    this.statusBar = statusBar;
    buildBody();
  }

  // private, getter is at most protected
  private Iterable<? extends Node> content;
  
  @Override
  public void setContent(Iterable<? extends Node> content) {
    this.content = content;
    buildBody();
  }
  
  @Override
  public void setContent(Node content) {
    this.content = new ArrayList<>(Arrays.asList(content));
    buildBody();
  }
  
  private void buildBody() {
    
    El body = getBody();
    
    body.childs.clear();
    
    if (header != null) {
      body.childs.add(header);
    }
    
    if (statusBar != null) {
      body.childs.add(statusBar);
    }
    
    if (content != null) {
      for (Node node: content) {
        body.childs.add(node);
      }
    }
  }
}