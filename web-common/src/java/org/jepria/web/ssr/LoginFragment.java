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
  public LoginFragment(String loginActionUrl) {
    
    Objects.requireNonNull(loginActionUrl);
    
    addClass("login-frame");

    final El loginForm = new El("form").addClass("auth-form").addClass("auth-form_login")
        .setAttribute("action", loginActionUrl)
        .setAttribute("method", "post");
    
    final El rowUsername = new El("div").addClass("auth-form__row");
    inputUsername = new El("input").setAttribute("type", "text").addClass("field-text").addClass("login-field_username")
        .setAttribute("name", "username").setAttribute("placeholder", "логин"); // NON-NLS
    rowUsername.appendChild(inputUsername);
    loginForm.appendChild(rowUsername);
    
    final El rowPassword = new El("div").addClass("auth-form__row");
    inputPassword = new El("input").setAttribute("type", "password").addClass("field-text").addClass("login-field_password")
        .setAttribute("name", "password").setAttribute("placeholder", "пароль"); // NON-NLS
    rowPassword.appendChild(inputPassword);
    loginForm.appendChild(rowPassword);
    
    final El rowButtonLogin = new El("div").addClass("auth-form__row");
    buttonLogin = new El("button").setAttribute("type", "submit").addClass("big-black-button")
        .setInnerHTML("ВОЙТИ"); // NON-NLS
    rowButtonLogin.appendChild(buttonLogin);
    loginForm.appendChild(rowButtonLogin);
    
    appendChild(loginForm);
    
    
    addScript("js/auth-fragment_login.js");
    addScript("js/jtm-common.js");
  }
}
