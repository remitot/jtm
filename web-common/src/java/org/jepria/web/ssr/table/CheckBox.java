package org.jepria.web.ssr.table;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

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
  protected String getScript() throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = CheckBox.class.getClassLoader(); // fallback
    }
    final String resourceName 
        = CheckBox.class.getCanonicalName() // org.jepria.web.ssr.table.CheckBox
        .replaceAll("\\.", "/") + ".js"; // org/jepria/web/ssr/table/CheckBox.js
    
    try (InputStream in = classLoader.getResourceAsStream(resourceName);
        Scanner sc = new Scanner(in, "UTF-8")) {
      sc.useDelimiter("\\Z");
      if (sc.hasNext()) {
        return sc.next();
      }
    }
    return null;
  }
}
