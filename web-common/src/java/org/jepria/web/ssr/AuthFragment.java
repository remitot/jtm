package org.jepria.web.ssr;

public class AuthFragment extends El {
  public AuthFragment(Context context) {
    super("div", context);
    addClass("auth-frame");
    
    Text text = context.getText();
    
    final El loginStatusBar = new StatusBar(context, StatusBar.Type.INFO, text.getString("org.jepria.web.ssr.AuthFragment.status.access_admin"));
    appendChild(loginStatusBar);
    
    addStyle("css/auth-fragment.css");
  }
}
