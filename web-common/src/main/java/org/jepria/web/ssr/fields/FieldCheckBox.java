package org.jepria.web.ssr.fields;

import org.jepria.web.ssr.Context;

public class FieldCheckBox extends CheckBox {
  
  public FieldCheckBox(
      Context context,
      String name, 
      boolean value,
      Boolean valueOriginal,
      boolean invalid, 
      String invalidMessage) {
    
    super(context, value);
    
    input.setAttribute("name", name);
    
    setAttribute("value-original", valueOriginal);
    if (invalid) {
      addClass("invalid");
      if (invalidMessage != null) {
        setAttribute("title", invalidMessage);
      }
    }
  }
}
