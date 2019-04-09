package org.jepria.web.ssr.table;

import org.jepria.web.ssr.El;

public class CheckBox extends El {
  
  public final El input;
  
  public final El checkmark;
  
  // enabled by default
  public CheckBox(boolean active) {
    super("label");
    classList.add("checkbox");
    
    input = new El("input");
    input.setAttribute("type", "checkbox");
    input.setAttribute("name", "active");
    if (active) {
      input.setAttribute("checked", "checked");
    }
    appendChild(input);
    
    checkmark = new El("span");
    checkmark.classList.add("checkmark");
    appendChild(checkmark);
    
    
    addStyle("css/checkbox.css");
    addScript("js/checkbox.js");
  }
  
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    
    if (enabled) {
      classList.remove("checkbox-disabled");
      input.attributes.remove("disabled");
    } else {
      classList.add("checkbox-disabled");
      input.setAttribute("disabled", true);
    }
  }
  
  @Override
  public void setReadonly(boolean readonly) {
    super.setReadonly(readonly);
    
    setEnabled(false);
  }
}
