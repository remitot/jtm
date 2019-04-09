package org.jepria.web.ssr;

public class AuthFragment extends El {
  public AuthFragment() {
    super("div");
    addClass("auth-frame");
    
    final El loginStatusBar = new StatusBar(StatusBar.Type.INFO, "Доступ только админам"); // NON-NLS
    appendChild(loginStatusBar);
    
    addStyle("css/auth-fragment.css");
  }
}
