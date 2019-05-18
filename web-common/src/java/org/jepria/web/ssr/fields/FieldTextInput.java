package org.jepria.web.ssr.fields;

import org.jepria.web.ssr.El;

public class FieldTextInput extends El {
  
  public FieldTextInput(
      String name, 
      String value,
      String valueOriginal,
      String placeholder,
      boolean invalid, 
      String invalidMessage) {
    
    super("input");
    
    addClass("field-text");
    
    setAttribute("type", "text");
    if (name != null) {
      setAttribute("name", name);
    }
    setAttribute("value", value);
    setAttribute("value-original", valueOriginal);
    if (placeholder != null) {
      setAttribute("placeholder", placeholder);
    }
    if (invalid) {
      addClass("invalid");
      if (invalidMessage != null) {
        setAttribute("title", invalidMessage);
      }
    }
    
    addStyle("css/field-text.css");
  }
  
  @Override
  public void setReadonly(boolean readonly) {
    super.setReadonly(readonly);
    
    if (readonly) {
      classList.add("field-text_readonly");
    } else {
      classList.remove("field-text_readonly");
    }
  }
}
