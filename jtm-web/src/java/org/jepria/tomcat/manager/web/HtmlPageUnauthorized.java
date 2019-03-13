package org.jepria.tomcat.manager.web;

import org.jepria.web.ssr.LoginFragment;
import org.jepria.web.ssr.PageHeader;

public class HtmlPageUnauthorized extends HtmlPage {
  
  // pageHeader needed in a constructor to properly add pageHeader_onload() to body.onload
  //TODO if body.onload will be constructed dynamically, remove this arg and use page.setPageHeader from outside
  public HtmlPageUnauthorized(PageHeader pageHeader, String loginActionUrl) {
    
    if (pageHeader != null) {
      setPageHeader(pageHeader);
    }
    
    final LoginFragment loginFragment = new LoginFragment(loginActionUrl);
    getBodyChilds().add(loginFragment);
    
    // add onload scripts
    body.setAttribute("onload", "jtm_onload();loginFragment_onload();pageHeader_onload();");
    
    body.addClass("login-background");
  }
}
