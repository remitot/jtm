package org.jepria.web.ssr.fields;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;

public class CheckBox extends El {
  
  public final El input;
  
  public final El checkmark;
  
  public CheckBox(Context context, boolean active) {
    super("label", context);
    classList.add("checkbox");
    
    input = new El("input", context);
    input.setAttribute("type", "checkbox");
    if (active) {
      input.setAttribute("checked", "checked");
    }
    appendChild(input);
    
    checkmark = new El("span", context);
    checkmark.classList.add("checkmark");
    appendChild(checkmark);
    
    
    addStyle("css/checkbox.css");
    addScript(new Script("js/checkbox.js", "checkbox_onload"));
  }
  
  public void setEnabled(boolean enabled) {
    if (enabled) {
      classList.remove("checkbox_disabled");
      input.attributes.remove("disabled");
    } else {
      classList.add("checkbox_disabled");
      input.setAttribute("disabled", true);
    }
  }
  
  public void setTitleActive(String titleActive) {
    setAttribute("org.jepria.web.ssr.field.CheckBox.title.active", titleActive);
  }
  
  public void setTitleInactive(String titleInactive) {
    setAttribute("org.jepria.web.ssr.field.CheckBox.title.inactive", titleInactive);
  }
}
