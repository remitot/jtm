package org.jepria.web.ssr;

import org.jepria.web.ssr.table.Collection;

public class AuthFragment extends El {
  public AuthFragment() {
    super("div");
    addClass("auth-frame");
    
    final El loginStatusBar = new StatusBar(StatusBar.Type.INFO, "Доступ только админам"); // NON-NLS
    appendChild(loginStatusBar);
  }
  
  @Override
  protected void addStyles(Collection styles) {
    super.addStyles(styles);
    styles.add("css/auth-fragment.css");
  }
}
