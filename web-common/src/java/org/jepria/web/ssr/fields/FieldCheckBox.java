package org.jepria.web.ssr.fields;

public class FieldCheckBox extends CheckBox {
  
  public FieldCheckBox(
      String name, 
      boolean value,
      Boolean valueOriginal,
      boolean invalid, 
      String invalidMessage) {
    
    super(value);
    
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
