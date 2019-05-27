package org.jepria.web.ssr;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LoginFragment extends AuthFragment {
  
  public final El inputUsername;
  public final El inputPassword;
  public final El buttonLogin;
  
  /**
   * 
   * @param loginRedirectPath path to redirect after a successful login (on login form submit button click).
   * If {@code null}, no redirect will be performed
   */
  public LoginFragment(Context context, String loginRedirectPath) {
    super(context);
    
    final String action;
    {
      StringBuilder sb = new StringBuilder();
      sb.append(context.getContextPath()).append('/');
      sb.append("login");
      if (loginRedirectPath != null) {
        sb.append("?redirect=");
        try {
          sb.append(URLEncoder.encode(loginRedirectPath, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          // impossible
          throw new RuntimeException(e);
        }
      }
      action = sb.toString();
    }
    
    addClass("login-frame");

    final Text text = context.getText();
    
    final El loginForm = new El("form", context).addClass("auth-form").addClass("auth-form_login")
        .setAttribute("action", action)
        .setAttribute("method", "post");
    
    final El rowUsername = new El("div", context).addClass("auth-form__row");
    inputUsername = new El("input", context).setAttribute("type", "text").addClass("field-text").addClass("login-field_username")
        .setAttribute("name", "username").setAttribute("placeholder", text.getString("org.jepria.web.ssr.LoginFragment.fieldUsername.placeholder"));
    inputUsername.addStyle("css/field-text.css");
    rowUsername.appendChild(inputUsername);
    loginForm.appendChild(rowUsername);
    
    final El rowPassword = new El("div", context).addClass("auth-form__row");
    inputPassword = new El("input", context).setAttribute("type", "password").addClass("field-text").addClass("login-field_password")
        .setAttribute("name", "password").setAttribute("placeholder", text.getString("org.jepria.web.ssr.LoginFragment.fieldPassword.placeholder"));
    inputPassword.addStyle("css/field-text.css");
    rowPassword.appendChild(inputPassword);
    loginForm.appendChild(rowPassword);
    
    final El rowButtonLogin = new El("div", context).addClass("auth-form__row");
    buttonLogin = new El("button", context).setAttribute("type", "submit").addClass("big-black-button")
        .setInnerHTML(text.getString("org.jepria.web.ssr.common.buttonLogin.text"));
    rowButtonLogin.appendChild(buttonLogin);
    loginForm.appendChild(rowButtonLogin);
    
    appendChild(loginForm);
    
    
    addScript("js/auth-fragment_login.js");
    addScript("js/common.js");
    addStyle("css/common.css");
  }
}
