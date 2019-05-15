package org.jepria.web.ssr;

public class PageHeader extends El {
  
  protected final Text text;
  
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
  public PageHeader(Text text) {
    super("div");
    this.text = text;
    classList.add("page-header");

    itemsContainer = new El("div").addClass("page-header__container");
    formLogoutContainer = new El("div").addClass("page-header__container");
    
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
   * @param logoutRedirectPath path to redirect after a successful logout (on logout form submit button click). 
   * If {@code null}, no redirect will be performed
   */
  public void setButtonLogout(String logoutRedirectPath) {
    // remove existing
    formLogoutContainer.childs.clear();
    
    // create
    final String action = "logout" + (logoutRedirectPath != null ? ("?redirect=" + logoutRedirectPath) : "");
    
    formLogout = new El("form").setAttribute("action", action).setAttribute("method", "post")
        .addClass("button-form");
    
    El buttonLogout = new El("button")
        .setAttribute("type", "submit")
        .addClass("page-header__button-logout")
        .addClass("big-black-button")
        .setInnerHTML(text.getString("org.jepria.web.ssr.common.buttonLogout.text"));
    
    buttonLogout.addStyle("css/jtm-common.css"); // for .big-black-button
    buttonLogout.addScript("js/jtm-common.js"); // for .big-black-button
    
    formLogout.appendChild(buttonLogout);
    
    // add
    formLogoutContainer.appendChild(formLogout);
  }
}
