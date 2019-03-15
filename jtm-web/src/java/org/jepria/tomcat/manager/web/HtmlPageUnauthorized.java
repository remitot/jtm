package org.jepria.tomcat.manager.web;

import org.jepria.web.ssr.LoginFragment;

public class HtmlPageUnauthorized extends HtmlPage {

  public HtmlPageUnauthorized(String loginActionUrl) {

    final LoginFragment loginFragment = new LoginFragment(loginActionUrl);
    getBodyChilds().add(loginFragment);

    // add onload scripts
    body.setAttribute("onload", "jtm_onload();loginFragment_onload();");

    body.addClass("login-background");
  }
  
}
