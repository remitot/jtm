package org.jepria.web.ssr.table;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.jepria.web.ssr.El;

public class CheckBox extends El {
  
  public final El input;
  
  public final El checkmark;
  
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
  }
  
  public void setEnabled(boolean enabled) {
    if (enabled) {
      classList.remove("checkbox-disabled");
      input.attributes.remove("disabled");
    } else {
      classList.add("checkbox-disabled");
      input.setAttribute("disabled", true);
    }
  }
  
  @Override
  protected void addScripts(Collection scripts) throws IOException {
    super.addScripts(scripts);
    
    
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = CheckBox.class.getClassLoader(); // fallback
    }
    try (InputStream in = classLoader.getResourceAsStream("org/jepria/web/ssr/table/checkbox.js");
        Scanner sc = new Scanner(in, "UTF-8")) {
      sc.useDelimiter("\\Z");
      if (sc.hasNext()) {
        scripts.add(sc.next());
      }
    }
  }
}
