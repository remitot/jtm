package org.jepria.web.ssr.fields;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;

public class FieldTextInput extends El {
  
  public FieldTextInput(
      Context context,
      String name, 
      String value,
      String valueOriginal,
      boolean invalid, 
      String invalidMessage) {
    
    super("input", context);
    
    addClass("field-text");
    
    setAttribute("type", "text");
    if (name != null) {
      setAttribute("name", name);
    }
    setAttribute("value", value);
    setAttribute("value-original", valueOriginal);
    if (invalid) {
      addClass("invalid");
      if (invalidMessage != null) {
        setAttribute("title", invalidMessage);
      }
    }
    
    addStyle("css/field-text.css");
  }
}
