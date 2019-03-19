package org.jepria.tomcat.manager.web;

import java.util.Objects;

import org.jepria.web.ssr.ForbiddenFragment;

public class HtmlPageForbidden extends HtmlPage {

  public HtmlPageForbidden(ForbiddenFragment logoutFragment) {

    Objects.requireNonNull(logoutFragment);
    
    getBodyChilds().add(logoutFragment);

    // add onload scripts
    body.setAttribute("onload", "jtm_onload();");

    body.addClass("background_gray");
  }
  
}
