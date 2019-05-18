package org.jepria.web.ssr.fields;

import org.jepria.web.ssr.El;

public class FieldTextLabel extends El {
  
  public FieldTextLabel() {
    super("label");
    addClass("field-text");
    addStyle("css/field-text.css");
  }
  
  public FieldTextLabel(String html) {
    this();
    setInnerHTML(html, false);
  }
}
