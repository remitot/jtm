package org.jepria.web.ssr;

import java.util.Objects;

public class HtmlPageForbidden extends HtmlPage {

  public HtmlPageForbidden(Context context, ForbiddenFragment logoutFragment) {
    super(context);
    
    Objects.requireNonNull(logoutFragment);
    
    getBodyChilds().add(logoutFragment);

    body.addScript("css/jtm-common.css");
    body.setAttribute("onload", "jtm_onload();");

    body.addClass("background_gray");
  }
  
}
