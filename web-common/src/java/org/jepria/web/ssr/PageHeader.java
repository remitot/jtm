package org.jepria.web.ssr;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.jepria.web.auth.RedirectBuilder;

public class PageHeader extends El {
  
  /**
   * Container for regular menu items (even if there are no items)
   */
  // private
  private final El itemsContainer;
  
  /**
   * Container for logout form (even if there is no logout button) 
   */
  // private
  private final El formLogoutContainer;
  
  // private
  private Iterable<? extends Node> items;
  
  // private field with protected getter
  private El formLogout;
  
  /**
   * @param text
   */
  public PageHeader(Context context) {
    super("div", context);
    
    classList.add("page-header");

    itemsContainer = new El("div", context).addClass("page-header__container");
    formLogoutContainer = new El("div", context).addClass("page-header__container");
    
    appendChild(itemsContainer);
    appendChild(formLogoutContainer);
    
    addStyle("css/page-header.css");
  }
  
  // protected
  protected El getFormLogout() {
    return formLogout;
  }
  
  public Iterable<? extends Node> getItems() {
    return items;
  }
  
  public void setItems(Iterable<? extends Node> items) {
    // remove existing
    itemsContainer.childs.clear();
    
    // add
    
    if (items != null) {
      for (Node item: items) {
        itemsContainer.appendChild(item);
      }
    }
  }
  
  /**
   * Sets the logout button to the page header, with redirecting to the current URI+QueryString after a successful logout
   * @param request current request to get URI+QueryString from. 
   * If {@code null}, no redirect will be performed
   */
  public void setButtonLogout(HttpServletRequest request) {
    if (request == null) {
      setButtonLogout((String)null);
    } else {
      setButtonLogout(RedirectBuilder.self(request));
    }
  }
  
  /**
   * Sets the logout button to the page header, redirecting to the specified path after a successful logout 
   * @param logoutRedirectPath path to redirect after a successful logout (on logout form submit button click). 
   * If {@code null}, no redirect will be performed. The value will be URL-encoded
   */
  public void setButtonLogout(String logoutRedirectPath) {
    // remove existing
    formLogoutContainer.childs.clear();
    
    // create
    final String action;
    {
      StringBuilder sb = new StringBuilder();
      sb.append("logout");
      if (logoutRedirectPath != null) {
        sb.append("?redirect=");
        try {
          sb.append(URLEncoder.encode(logoutRedirectPath, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          // impossible
          throw new RuntimeException(e);
        }
      }
      action = sb.toString();
    }
    
    formLogout = new El("form", context).setAttribute("action", action).setAttribute("method", "post")
        .addClass("button-form");
    
    final Text text = context.getText();
    
    El buttonLogout = new El("button", context)
        .setAttribute("type", "submit")
        .addClass("page-header__button-logout")
        .addClass("big-black-button")
        .setInnerHTML(text.getString("org.jepria.web.ssr.common.buttonLogout.text"));
    
    buttonLogout.addStyle("css/common.css"); // for .big-black-button
    buttonLogout.addScript("js/common.js"); // for .big-black-button
    
    formLogout.appendChild(buttonLogout);
    
    // add
    formLogoutContainer.appendChild(formLogout);
  }
}
