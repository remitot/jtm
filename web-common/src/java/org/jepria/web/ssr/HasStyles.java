package org.jepria.web.ssr;

public interface HasStyles {
  /**
   * Adds a .css style specific to this element.
   * A style is added by its relative path (same as {@code href} attribute value of a {@code <link rel="stylesheet">} tag) 
   */
  void addStyle(String styleLinkHref);
}
