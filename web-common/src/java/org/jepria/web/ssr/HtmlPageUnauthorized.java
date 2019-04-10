package org.jepria.web.ssr;

import java.util.Objects;

public class HtmlPageUnauthorized extends HtmlPage {

  public HtmlPageUnauthorized(Context context, LoginFragment loginFragment) {
    super(context);

    Objects.requireNonNull(loginFragment);
    
    getBodyChilds().add(loginFragment);

    body.addScript("css/jtm-common.css");
    body.setAttribute("onload", "jtm_onload();authFragmentLogin_onload();");

    body.addClass("background_gray");
  }
  
}
