package org.jepria.web.ssr;

public class AuthFragment extends StatusBar {
  public AuthFragment(Context context) {
    super(context);
    
    setCloseable(false);
    
    addClass("status-bar_auth");
    addStyle("css/auth-fragment.css");
  }
}
