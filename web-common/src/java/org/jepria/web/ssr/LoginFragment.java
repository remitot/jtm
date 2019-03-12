package org.jepria.web.ssr;

import org.jepria.web.ssr.table.Collection;

public class LoginFragment extends El {
  public LoginFragment() {
    super("div");
    setAttribute("id", "loginScreen");
    addClass("loginForm-hidden");
    
    final El loginFrame = new El("div").setAttribute("id", "loginFrame");
    appendChild(loginFrame);

    final El loginStatusBar = new StatusBar(StatusBar.Type.INFO, "Доступ только админам"); // NON-NLS
    loginFrame.appendChild(loginStatusBar);
    
    final El loginForm = new El("form").setAttribute("id", "login-form");
    
    final El rowUsername = new El("div").addClass("row");
    final El inputUsername = new El("input").setAttribute("type", "text").addClass("field-text").setAttribute("id", "fieldUsername")
        .setAttribute("placeholder", "логин"); // NON-NLS
    rowUsername.appendChild(inputUsername);
    loginForm.appendChild(rowUsername);
    
    final El rowPassword = new El("div").addClass("row");
    final El inputPassword = new El("input").setAttribute("type", "password").addClass("field-text").setAttribute("id", "fieldPassword")
        .setAttribute("placeholder", "пароль"); // NON-NLS
    rowPassword.appendChild(inputPassword);
    loginForm.appendChild(rowPassword);
    
    final El rowButtonLogin = new El("div").addClass("row");
    final El buttonLogin = new El("button").setAttribute("type", "submit").addClass("big-black-button").setAttribute("id", "buttonLogin")
        .setAttribute("onclick", "onButtonLoginClick(); return false;").setInnerHTML("ВОЙТИ"); // NON-NLS
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
  }
}
