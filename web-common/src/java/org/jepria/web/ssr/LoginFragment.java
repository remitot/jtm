package org.jepria.web.ssr;

import org.jepria.web.ssr.table.Collection;

public class LoginFragment extends El {
  public LoginFragment() {
    super("div");
    setAttribute("id", "loginScreen");
    addClass("loginForm-hidden");
    
    final El loginFrame = new El("div").addClass("login-frame");
    appendChild(loginFrame);

    final El loginStatusBar = new StatusBar(StatusBar.Type.INFO, "Доступ только админам"); // NON-NLS
    loginFrame.appendChild(loginStatusBar);
    
    final El loginForm = new El("form").addClass("login-form")
        .setAttribute("action", "jdbc/login") // TODO this will lose any path- or query params from the current page
        .setAttribute("method", "post");
    
    final El rowUsername = new El("div").addClass("login-form_row");
    final El inputUsername = new El("input").setAttribute("type", "text").addClass("field-text").addClass("login-field_username")
        .setAttribute("name", "username").setAttribute("placeholder", "логин"); // NON-NLS
    rowUsername.appendChild(inputUsername);
    loginForm.appendChild(rowUsername);
    
    final El rowPassword = new El("div").addClass("login-form_row");
    final El inputPassword = new El("input").setAttribute("type", "password").addClass("field-text").addClass("login-field_password")
        .setAttribute("name", "password").setAttribute("placeholder", "пароль"); // NON-NLS
    rowPassword.appendChild(inputPassword);
    loginForm.appendChild(rowPassword);
    
    final El rowButtonLogin = new El("div").addClass("login-form_row");
    final El buttonLogin = new El("button").setAttribute("type", "submit").addClass("big-black-button")
        .setInnerHTML("ВОЙТИ"); // NON-NLS
    rowButtonLogin.appendChild(buttonLogin);
    loginForm.appendChild(rowButtonLogin);
    
    loginFrame.appendChild(loginForm);
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
