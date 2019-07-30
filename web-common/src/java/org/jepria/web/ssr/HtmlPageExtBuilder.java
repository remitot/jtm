package org.jepria.web.ssr;

import java.util.Arrays;

/**
 * Builder for extended html pages, which have a header with menu items and logout button, and a status bar 
 */
public interface HtmlPageExtBuilder extends HtmlPageBaseBuilder {
  
  void setHeader(PageHeader header);
  void setStatusBar(StatusBar statusBar);
  
  /**
   * Sets main content of the page's body regarding other page elements, e.g. header or status.
   * @param content 
   */
  void setContent(Iterable<? extends Node> content);
  
  /**
   * Sets main content of the page's body regarding other page elements, e.g. header or status.
   * @param content 
   */
  default void setContent(Node content) {
    setContent(Arrays.asList(content));
  };
  
  public static HtmlPageExtBuilder newInstance(Context context) {
    return new HtmlPageExtBuilderImpl(context);
  }
}
