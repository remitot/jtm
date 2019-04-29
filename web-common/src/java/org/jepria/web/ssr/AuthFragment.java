package org.jepria.web.ssr;

public class AuthFragment extends El {
  public AuthFragment(Text text) {
    super("div");
    addClass("auth-frame");
    
    final El loginStatusBar = new StatusBar(StatusBar.Type.INFO, text.getString("org.jepria.web.ssr.AuthFragment.status.access_admin"));
    appendChild(loginStatusBar);
    
    addStyle("css/auth-fragment.css");
  }
}
