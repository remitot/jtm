package org.jepria.web.ssr;

import java.util.Objects;

import org.jepria.web.ssr.LoginFragment;

public class HtmlPageUnauthorized extends HtmlPage {

  public HtmlPageUnauthorized(LoginFragment loginFragment) {

    Objects.requireNonNull(loginFragment);
    
    getBodyChilds().add(loginFragment);

    body.addScript("css/jtm-common.css");
    body.setAttribute("onload", "jtm_onload();authFragmentLogin_onload();");

    body.addClass("background_gray");
  }
  
}
