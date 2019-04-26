package org.jepria.web.ssr;

public class PageHeader extends El {
  
  private El itemManagerApache;
  private El itemJdbc;
  private El itemLog;
  private El itemPort;
  private El formLogout;
  
  /**
   * Menu items possibly displayed as currently selected 
   */
  public static enum CurrentMenuItem {
    JDBC,
    LOG,
    PORT,
  }
  
  /**
   * @param currentMenuItem the menu item to be displayed as currently active.
   * If {@code null}, no menu item will be displayed as currently active.
   */
  public PageHeader(Context context, CurrentMenuItem currentMenuItem) {
    super("div", context);
    classList.add("page-header");
    
    
    itemJdbc = new El("a", context);
    itemJdbc.classList.add("page-header__menu-item");
    itemJdbc.classList.add("page-header__menu-item_regular");
    if (currentMenuItem == CurrentMenuItem.JDBC) {
      itemJdbc.classList.add("page-header__menu-item_current");
    } else {
      itemJdbc.classList.add("page-header__menu-item_hoverable");
      itemJdbc.setAttribute("href", "jdbc");
    }
    itemJdbc.setInnerHTML(context.getText("org.jepria.web.ssr.PageHeader.itemJdbc"), true);
    appendChild(itemJdbc);
    
    
    itemLog = new El("a", context);
    itemLog.classList.add("page-header__menu-item");
    itemLog.classList.add("page-header__menu-item_regular");
    if (currentMenuItem == CurrentMenuItem.LOG) {
      itemLog.classList.add("page-header__menu-item_current");
    } else {
      itemLog.classList.add("page-header__menu-item_hoverable");
      itemLog.setAttribute("href", "log");
    }
    itemLog.setInnerHTML(context.getText("org.jepria.web.ssr.PageHeader.itemLog"), true);
    appendChild(itemLog);
    
    
    itemPort = new El("a", context);
    itemPort.classList.add("page-header__menu-item");
    itemPort.classList.add("page-header__menu-item_regular");
    if (currentMenuItem == CurrentMenuItem.PORT) {
      itemPort.classList.add("page-header__menu-item_current");
    } else {
      itemPort.classList.add("page-header__menu-item_hoverable");
      itemPort.setAttribute("href", "port");
    }
    itemPort.setInnerHTML(context.getText("org.jepria.web.ssr.PageHeader.itemPort"), true);
    appendChild(itemPort);
    
    
    addStyle("css/page-header.css");
    addStyle("css/jtm-common.css"); // for .big-black-button
    addScript("js/jtm-common.js"); // for .big-black-button
  }
  
  /**
   * @param managerApacheHref html {@code href} value for the 'Apache HTTPD' menu item. 
   * If {@code null} the item is removed from the container
   */
  public void setManagerApache(String managerApacheHref) {
    // remove
    if (itemManagerApache != null) {
      childs.remove(itemManagerApache);
      itemManagerApache = null;
    }
    
    // add
    if (managerApacheHref != null) {
      itemManagerApache = new El("a", context);
      itemManagerApache.classList.add("page-header__menu-item");
      itemManagerApache.classList.add("page-header__menu-item_apache-httpd");
      itemManagerApache.setAttribute("href", managerApacheHref);
      itemManagerApache.classList.add("page-header__menu-item_hoverable");
      
      itemManagerApache.setAttribute("target", "_blank");
      itemManagerApache.setInnerHTML(context.getText("org.jepria.web.ssr.PageHeader.itemApacheHTTPD"), true);
      
      childs.add(0, itemManagerApache);
    }
  }
  
  /**
   * @param logoutRedirectPath path to redirect after a successful logout (on logout form submit button click). 
   * If {@code null}, no redirect will be performed
   */
  public void setButtonLogout(String logoutRedirectPath) {
    // remove
    if (formLogout != null) {
      childs.remove(formLogout);
      formLogout = null;
    }
    
    
    // add
    final String action = "logout" + (logoutRedirectPath != null ? ("?redirect=" + logoutRedirectPath) : "");
    
    formLogout = new El("form", context).setAttribute("action", action).setAttribute("method", "post")
        .addClass("button-form");
    
    El buttonLogout = new El("button", context)
        .setAttribute("type", "submit")
        .addClass("page-header__button-logout")
        .addClass("big-black-button")
        .setInnerHTML(context.getText("org.jepria.web.ssr.common.buttonLogout.text"));
    formLogout.appendChild(buttonLogout);
    
    appendChild(formLogout);
  }
}
