package org.jepria.web.ssr;

import java.util.Objects;

import org.jepria.web.ssr.ForbiddenFragment;

public class HtmlPageForbidden extends HtmlPage {

  public HtmlPageForbidden(ForbiddenFragment logoutFragment) {

    Objects.requireNonNull(logoutFragment);
    
    getBodyChilds().add(logoutFragment);

    body.addScript("css/jtm-common.css");
    body.setAttribute("onload", "jtm_onload();");

    body.addClass("background_gray");
  }
  
}
