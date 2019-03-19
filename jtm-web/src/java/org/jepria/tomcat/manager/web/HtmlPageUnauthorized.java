package org.jepria.tomcat.manager.web;

import java.util.Objects;

import org.jepria.web.ssr.LoginFragment;

public class HtmlPageUnauthorized extends HtmlPage {

  public HtmlPageUnauthorized(LoginFragment loginFragment) {

    Objects.requireNonNull(loginFragment);
    
    getBodyChilds().add(loginFragment);

    // add onload scripts
    body.setAttribute("onload", "jtm_onload();authFragmentLogin_onload();");

    body.addClass("background_gray");
  }
  
}
