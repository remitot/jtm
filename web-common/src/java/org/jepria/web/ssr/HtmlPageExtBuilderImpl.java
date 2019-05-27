package org.jepria.web.ssr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
  }

  @Override
  public void setStatusBar(StatusBar statusBar) {
    this.statusBar = statusBar;
  }

  // private, getter is at most protected
  private Iterable<? extends Node> extContent;
  
  @Override
  public void setContent(Iterable<? extends Node> content) {
    extContent = content;
  }
  
  @Override
  public void setContent(Node content) {
    extContent = new ArrayList<>(Arrays.asList(content));
  }
  
  @Override
  public Page build() {
    
    List<Node> content = new ArrayList<>();
    
    if (header != null) {
      content.add(header);
    }
    
    if (statusBar != null) {
      content.add(statusBar);
    }
    
    if (extContent != null) {
      for (Node node: extContent) {
        content.add(node);
      }
    }
    
    super.setContent(content);
    
    return super.build();
  }
}