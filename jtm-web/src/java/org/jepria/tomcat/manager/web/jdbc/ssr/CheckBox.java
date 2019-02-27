package org.jepria.tomcat.manager.web.jdbc.ssr;

public class CheckBox extends El {
  
  protected final El input;
  
  protected final El checkmark;
  
  public CheckBox(boolean active) {
    super("label");
    classList.add("checkbox");
    
    input = new El("input");
    input.setAttribute("type", "checkbox");
    input.setAttribute("name", "active");
    input.setAttribute("checked", active);
    input.setAttribute("value-original", active);
    appendChild(input);
    
    checkmark = new El("span");
    checkmark.classList.add("checkmark");
    appendChild(checkmark);
  }
  
  public void setEnabled(boolean enabled) {
    if (enabled) {
      classList.remove("checkbox-disabled");
      input.setAttribute("disabled", true);
    } else {
      classList.add("checkbox-disabled");
      input.attributes.remove("disabled");
    }
  }
}
