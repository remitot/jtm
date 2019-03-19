package org.jepria.web.ssr;

import org.jepria.web.ssr.table.Collection;

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
  public PageHeader(CurrentMenuItem currentMenuItem) {
    super("div");
    classList.add("page-header");
    
    
    itemJdbc = new El("a");
    itemJdbc.classList.add("page-header__menu-item");
    itemJdbc.classList.add("page-header__menu-item_regular");
    if (currentMenuItem == CurrentMenuItem.JDBC) {
      itemJdbc.classList.add("page-header__menu-item_current");
    } else {
      itemJdbc.classList.add("page-header__menu-item_hoverable");
      itemJdbc.setAttribute("href", "jdbc");
    }
    itemJdbc.setInnerHTML("JDBC ресурсы"); // NON-NLS
    appendChild(itemJdbc);
    
    
    itemLog = new El("a");
    itemLog.classList.add("page-header__menu-item");
    itemLog.classList.add("page-header__menu-item_regular");
    if (currentMenuItem == CurrentMenuItem.LOG) {
      itemLog.classList.add("page-header__menu-item_current");
    } else {
      itemLog.classList.add("page-header__menu-item_hoverable");
      itemLog.setAttribute("href", "log");
    }
    itemLog.setInnerHTML("Логи"); // NON-NLS
    appendChild(itemLog);
    
    
    itemPort = new El("a");
    itemPort.classList.add("page-header__menu-item");
    itemPort.classList.add("page-header__menu-item_regular");
    if (currentMenuItem == CurrentMenuItem.PORT) {
      itemPort.classList.add("page-header__menu-item_current");
    } else {
      itemPort.classList.add("page-header__menu-item_hoverable");
      itemPort.setAttribute("href", "port");
    }
    itemPort.setInnerHTML("Порты"); // NON-NLS
    appendChild(itemPort);
    
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
      itemManagerApache = new El("a");
      itemManagerApache.classList.add("page-header__menu-item");
      itemManagerApache.classList.add("page-header__menu-item_apache-httpd");
      itemManagerApache.setAttribute("href", managerApacheHref);
      itemManagerApache.classList.add("page-header__menu-item_hoverable");
      
      itemManagerApache.setAttribute("target", "_blank");
      itemManagerApache.setInnerHTML("Apache HTTPD"); // NON-NLS
      
      childs.add(0, itemManagerApache);
    }
  }
  
  /**
   * @param logoutActionUrl html {@code action} value of the {@code form} to submit on logout button click. 
   * If {@code null}, the button is removed from the container
   */
  public void setButtonLogout(String logoutActionUrl) {
    // remove
    if (formLogout != null) {
      childs.remove(formLogout);
      formLogout = null;
    }
    
    // add
    if (logoutActionUrl != null) {
      formLogout = new El("form").setAttribute("action", logoutActionUrl).setAttribute("method", "post")
          .addClass("button-form");
      
      El buttonLogout = new El("button")
          .setAttribute("type", "submit")
          .addClass("page-header__button-logout")
          .addClass("big-black-button")
          .setInnerHTML("ВЫЙТИ"); // NON-NLS
      formLogout.appendChild(buttonLogout);
      
      appendChild(formLogout);
    }
  }
  
  @Override
  protected void addStyles(Collection styles) {
    super.addStyles(styles);
    styles.add("css/page-header.css");
    styles.add("css/jtm-common.css"); // for .big-black-button
  }
  
  @Override
  protected void addScripts(Collection scripts) {
    super.addScripts(scripts);
    scripts.add("js/jtm-common.js"); // for .big-black-button
  }
}
