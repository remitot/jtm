package org.jepria.web.ssr;

import java.util.Objects;

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
  public StatusBar(Context context, Type type, String innerHTML) {
    super ("div", context);
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
    
    
    addStyle("css/status-bar.css");
  }
}
