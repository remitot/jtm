package org.jepria.tomcat.manager.web.jdbc.ssr;

public class Label extends El {
  public Label(String text) {
    super("label");
    if (text != null) {
      setInnerHTML(text);
    }
  }
}
