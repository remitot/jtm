package org.jepria.web.ssr.fields;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;

public class FieldTextLabel extends El {
  
  public FieldTextLabel(Context context) {
    super("label", context);
    addClass("field-text");
    addStyle("css/field-text.css");
  }
  
  public FieldTextLabel(Context context, String html) {
    this(context);
    setInnerHTML(html, false);
  }
}
