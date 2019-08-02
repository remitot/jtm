package org.jepria.web.ssr;

public class AuthFragment extends StatusBar {
  public AuthFragment(Context context) {
    super(context);
    
    setCloseable(false);
    
    addClass("status-bar_auth");
    addStyle("css/auth-fragment.css");
  }
  
  // TODO unused text? "org.jepria.web.ssr.AuthFragment.status.access_admin"
}
