package org.jepria.web.ssr;

import java.util.Objects;

import org.jepria.web.ssr.table.Collection;

public class LoginFragment extends El {
  
  public final El inputUsername;
  public final El inputPassword;
  public final El buttonLogin;
  
  /**
   * 
   * @param loginActionUrl html {@code action} value of the {@code form} to submit on login button click, non null
   */
  public LoginFragment(String loginActionUrl) {
    super("div");
    
    Objects.requireNonNull(loginActionUrl);
    
    addClass("login-frame");

    final El loginStatusBar = new StatusBar(StatusBar.Type.INFO, "Доступ только админам"); // NON-NLS
    appendChild(loginStatusBar);
    
    final El loginForm = new El("form").addClass("login-form")
        .setAttribute("action", loginActionUrl)
        .setAttribute("method", "post");
    
    final El rowUsername = new El("div").addClass("login-form__row");
    inputUsername = new El("input").setAttribute("type", "text").addClass("field-text").addClass("login-field_username")
        .setAttribute("name", "username").setAttribute("placeholder", "логин"); // NON-NLS
    rowUsername.appendChild(inputUsername);
    loginForm.appendChild(rowUsername);
    
    final El rowPassword = new El("div").addClass("login-form__row");
    inputPassword = new El("input").setAttribute("type", "password").addClass("field-text").addClass("login-field_password")
        .setAttribute("name", "password").setAttribute("placeholder", "пароль"); // NON-NLS
    rowPassword.appendChild(inputPassword);
    loginForm.appendChild(rowPassword);
    
    final El rowButtonLogin = new El("div").addClass("login-form__row");
    buttonLogin = new El("button").setAttribute("type", "submit").addClass("big-black-button")
        .setInnerHTML("ВОЙТИ"); // NON-NLS
    rowButtonLogin.appendChild(buttonLogin);
    loginForm.appendChild(rowButtonLogin);
    
    appendChild(loginForm);
  }
  
  @Override
  protected void addStyles(Collection styles) {
    super.addStyles(styles);
    styles.add("css/login-fragment.css");
  }
  
  @Override
  protected void addScripts(Collection scripts) {
    super.addScripts(scripts);
    scripts.add("js/login-fragment.js");
    scripts.add("js/jtm-common.js");
  }
}
