package org.jepria.web.ssr;

/**
 * Builder for extended html pages, which have a header with menu items and logout button, and a status bar 
 */
public interface HtmlPageExtBuilder extends HtmlPageBaseBuilder {
  void setHeader(PageHeader header);
  void setStatusBar(StatusBar statusBar);
  
  public static HtmlPageExtBuilder newInstance(Context context) {
    return new HtmlPageExtBuilderImpl(context);
  }
}
