package org.jepria.web.ssr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.jepria.web.ssr.table.Collection;

public class ControlButtons extends El {
  
  public final El buttonCreate;
  public final El buttonSave;
  public final El buttonReset;
  
  public ControlButtons() {
    super("div");
    classList.add("control-buttons");
    
    buttonCreate = new El("button")
        .addClass("control-button")
        .addClass("control-button_create")
        .addClass("big-black-button")
        .setInnerHTML("НОВАЯ ЗАПИСЬ"); // NON-NLS
    
    buttonSave = new El("button")
        .addClass("control-button")
        .addClass("control-button_save")
        .addClass("big-black-button")
        .setInnerHTML("СОХРАНИТЬ ВСЁ"); // NON-NLS
    
    buttonReset = new El("button")
        .addClass("control-button")
        .addClass("control-button_reset")
        .addClass("big-black-button")
        .setInnerHTML("СБРОСИТЬ ВСЁ"); // NON-NLS
    
    appendChild(buttonCreate);
    appendChild(buttonSave);
    appendChild(buttonReset);
  }
  
  @Override
  protected void addScripts(Collection scripts) throws IOException {
    super.addScripts(scripts);
    
    
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = ControlButtons.class.getClassLoader(); // fallback
    }

    
    try (InputStream in = classLoader.getResourceAsStream("org/jepria/web/ssr/control-buttons.js");
        Scanner sc = new Scanner(in, "UTF-8")) {
      sc.useDelimiter("\\Z");
      if (sc.hasNext()) {
        scripts.add(sc.next());
      }
    }
  }
  
}
