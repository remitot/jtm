package org.jepria.web.ssr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jepria.web.ssr.PageHeader.CurrentMenuItem;

/*package*/class JtmPageBuilderImpl extends HtmlPageBuilderImpl implements JtmPageBuilder {
  
  protected CurrentMenuItem currentMenuItem;
  protected String managerApacheHref;
  protected StatusBar statusBar;
  protected String logoutActionUrl;
  
  public JtmPageBuilderImpl(Context context) {
    super(context);
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
  public void setButtonLogout(String logoutActionUrl) {
    this.logoutActionUrl = logoutActionUrl;
  }

  @Override
  public void setStatusBar(StatusBar statusBar) {
    this.statusBar = statusBar;
  }

  protected Iterable<El> jtmContent;
  
  @Override
  public void setContent(Iterable<El> content) {
    jtmContent = content;
  }
  
  @Override
  public void setContent(El content) {
    jtmContent = new ArrayList<>(Arrays.asList(content));
  }
  
  @Override
  public Page build() {
    
    List<El> content = new ArrayList<>();
    
    if (currentMenuItem != null || logoutActionUrl != null || managerApacheHref != null) {
      final PageHeader pageHeader = new PageHeader(context, currentMenuItem);
      
      if (managerApacheHref != null) {
        pageHeader.setManagerApache(managerApacheHref);
      }
      
      if (logoutActionUrl != null) {
        pageHeader.setButtonLogout(logoutActionUrl);
      }
      
      content.add(pageHeader);
    }
    
    if (statusBar != null) {
      content.add(statusBar);
    }
    
    if (jtmContent != null) {
      Iterator<El> jtmContentIt = jtmContent.iterator();
      while (jtmContentIt.hasNext()) {
        content.add(jtmContentIt.next());
      }
    }
    
    super.setContent(content);
    
    return super.build();
  }
}