package org.jepria.web.ssr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

import org.jepria.web.ssr.table.Collection;

public class StatusBar extends El {
  
  public static enum Type {
    NONE,
    SUCCESS,
    INFO,
    ERROR
  }
  
  /**
   * 
   * @param type not null
   * @param innerHTML may be null
   */
  public StatusBar(Type type, String innerHTML) {
    super ("div");
    classList.add("statusBar");
    
    Objects.requireNonNull(type);
    switch(type) {
    case NONE: {
      classList.add("statusBar_none");
      break;
    }
    case SUCCESS: {
      classList.add("statusBar_success");
      break;
    }
    case INFO: {
      classList.add("statusBar_info");
      break;
    }
    case ERROR: {
      classList.add("statusBar_error");
      break;
    }
    }
    
    setInnerHTML(innerHTML);
  }
  
  @Override
  protected void addStyles(Collection styles) throws IOException {
    super.addStyles(styles);
    
    
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = StatusBar.class.getClassLoader(); // fallback
    }

    
    try (InputStream in = classLoader.getResourceAsStream("org/jepria/web/ssr/status-bar.css");
        Scanner sc = new Scanner(in, "UTF-8")) {
      sc.useDelimiter("\\Z");
      if (sc.hasNext()) {
        styles.add(sc.next());
      }
    }
  }
}
