package org.jepria.web.ssr;

import java.util.Objects;

public class LoginFragment extends AuthFragment {
  
  public final El inputUsername;
  public final El inputPassword;
  public final El buttonLogin;
  
  /**
   * 
   * @param loginActionUrl html {@code action} value of the {@code form} to submit on login button click, non null
   */
  public LoginFragment(Context context, String loginActionUrl) {
    super(context);
    
    Objects.requireNonNull(loginActionUrl);
    
    addClass("login-frame");

    final El loginForm = new El("form", context).addClass("auth-form").addClass("auth-form_login")
        .setAttribute("action", loginActionUrl)
        .setAttribute("method", "post");
    
    final El rowUsername = new El("div", context).addClass("auth-form__row");
    inputUsername = new El("input", context).setAttribute("type", "text").addClass("field-text").addClass("login-field_username")
        .setAttribute("name", "username").setAttribute("placeholder", context.getText("org.jepria.web.ssr.LoginFragment.fieldUsername.placeholder"));
    rowUsername.appendChild(inputUsername);
    loginForm.appendChild(rowUsername);
    
    final El rowPassword = new El("div", context).addClass("auth-form__row");
    inputPassword = new El("input", context).setAttribute("type", "password").addClass("field-text").addClass("login-field_password")
        .setAttribute("name", "password").setAttribute("placeholder", context.getText("org.jepria.web.ssr.LoginFragment.fieldPassword.placeholder"));
    rowPassword.appendChild(inputPassword);
    loginForm.appendChild(rowPassword);
    
    final El rowButtonLogin = new El("div", context).addClass("auth-form__row");
    buttonLogin = new El("button", context).setAttribute("type", "submit").addClass("big-black-button")
        .setInnerHTML(context.getText("org.jepria.web.ssr.common.buttonLogin.text"));
    rowButtonLogin.appendChild(buttonLogin);
    loginForm.appendChild(rowButtonLogin);
    
    appendChild(loginForm);
    
    
    addScript("js/auth-fragment_login.js");
    addScript("js/jtm-common.js");
  }
}
