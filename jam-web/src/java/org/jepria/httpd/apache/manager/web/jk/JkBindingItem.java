package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.web.ssr.fields.Field;
import org.jepria.web.ssr.fields.ItemData;

/*package*/class JkBindingItem extends ItemData {
  private static final long serialVersionUID = 1L;

  private final String fieldLabel;
  private final String placeholder;
  
  public JkBindingItem(String fieldLabel, String placeholder) {
    this.fieldLabel = fieldLabel;
    this.placeholder = placeholder;
    
    put("field", new Field("field"));
  }
  
  public String fieldLabel() {
    return fieldLabel;
  }
  
  public String placeholder() {
    return placeholder;
  }
  
  public Field field() {
    return get("field");
  }
}
