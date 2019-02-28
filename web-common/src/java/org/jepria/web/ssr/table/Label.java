package org.jepria.web.ssr.table;

public class Label extends El {
  
  public Label(String text) {
    super("label");
    setInnerHTML(text);
  }
}
