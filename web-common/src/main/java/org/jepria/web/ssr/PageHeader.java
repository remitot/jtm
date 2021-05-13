package org.jepria.web.ssr;

import org.jepria.web.auth.RedirectBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
   * Container for source info link (even if the link is not set)
   */
  private El sourceInfoContainer;
  
  /**
   * @param text
   */
  public PageHeader(Context context) {
    super("div", context);
    
    classList.add("page-header");

    itemsContainer = new El("div", context).addClass("page-header__container").addClass("page-header__container_pos_left");
    formLogoutContainer = new El("div", context).addClass("page-header__container").addClass("page-header__container_pos_right");
    sourceInfoContainer = new El("div", context).addClass("page-header__container");
    
    // important to maintain the appending order:
    appendChild(formLogoutContainer);
    appendChild(itemsContainer);
    appendChild(sourceInfoContainer);

    
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
    this.items = items;
    
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
      sb.append(context.getAppContextPath()).append('/');
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
        .addClass("page-header__line-element")
        .addClass("big-black-button")
        .setInnerHTML(text.getString("org.jepria.web.ssr.common.buttonLogout.text"));
    
    buttonLogout.addStyle("css/common.css"); // for .big-black-button
    buttonLogout.addScript(new Script("js/common.js", "common_onload")); // for .big-black-button
    
    formLogout.appendChild(buttonLogout);
    
    // add
    formLogoutContainer.appendChild(formLogout);
  }
  
  public static class MenuItem extends El {
    public MenuItem(Context context, boolean current, String href) {
      super("a", context);
      addClass("page-header__menu-item");
      addClass("page-header__line-element");
      addClass("page-header__menu-item_regular");
      if (current) {
        addClass("page-header__menu-item_current");
      } else {
        addClass("page-header__menu-item_hoverable");
        if (href != null) {
          setAttribute("href", href);
        }
      }
    }
  }

  /**
   * Sets source info link
   */
  public void setSources() {
    sourceInfoContainer.childs.clear();

    {
      El a = new El("a", context);
      a.addClass("page-header__line-element");
      a.addClass("page-header__menu-item_source-info");

      a.setAttribute("href", context.getAppContextPath() + "/sources");
      a.setAttribute("target", "_blank");

      a.setInnerHTML(context.getText().getString("org.jepria.web.ssr.common.sources.text"));
      a.setAttribute("title", context.getText().getString("org.jepria.web.ssr.common.sources.title"));
      sourceInfoContainer.appendChild(a);
    }
  }
}
