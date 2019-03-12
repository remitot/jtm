package org.jepria.web.ssr;

import java.util.Objects;

import org.jepria.web.ssr.table.Collection;

public class StatusBar extends El {
  
  public StatusBar(Status status) {
    this(status.type, status.statusHTML);
  }
  
  /**
   * 
   * @param type not null
   * @param innerHTML may be null
   */
  public StatusBar(Status.Type type, String innerHTML) {
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
  protected void addStyles(Collection styles) {
    super.addStyles(styles);
    styles.add("css/status-bar.css");
  }
}
