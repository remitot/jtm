package org.jepria.web.ssr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.jepria.web.ssr.table.Collection;

public class ControlButtons extends El {
  
  public final El buttonSave;
  
  public ControlButtons() {
    super("div");
    classList.add("control-buttons");
    
    buttonSave = new El("button")
        .setAttribute("onclick", "onSaveButtonClick()")
        .addClass("control-button")
        .addClass("control-button_save")
        .addClass("big-black-button")
        .setAttribute("disabled") // initial
        .setInnerHTML("СОХРАНИТЬ ВСЁ"); // NON-NLS
    
    appendChild(buttonSave);
  }
  
  @Override
  protected void addScripts(Collection scripts) throws IOException {
    super.addScripts(scripts);
    
    
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = ControlButtons.class.getClassLoader(); // fallback
    }

    
    try (InputStream in = classLoader.getResourceAsStream("org/jepria/web/ssr/ControlButtons.js");
        Scanner sc = new Scanner(in, "UTF-8")) {
      sc.useDelimiter("\\Z");
      if (sc.hasNext()) {
        scripts.add(sc.next());
      }
    }
  }
  
}
