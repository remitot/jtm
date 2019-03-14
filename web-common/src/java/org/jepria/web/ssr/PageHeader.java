package org.jepria.web.ssr;

import org.jepria.web.ssr.table.Collection;

public class PageHeader extends El {
  
  public static enum CurrentMenuItem {
    JDBC,
    LOG,
    PORT,
  }
  
  /**
   * 
   * @param managerApacheHref if {@code null}, the Apache-Manager menu item will be disabled
   * @param currentMenuItem
   */
  public PageHeader(String managerApacheHref, String logoutActionUrl, CurrentMenuItem currentMenuItem) {
    super("div");
    classList.add("page-header");
    
    El menuItem;
    
    
    menuItem = new El("a");
    menuItem.classList.add("page-header__menu-item");
    if (managerApacheHref != null) {
      menuItem.setAttribute("href", managerApacheHref);
      menuItem.classList.add("page-header__menu-item_hoverable");
    } else {
      // TODO log? set title? or quiet?
    }
    menuItem.setAttribute("target", "_blank");
    menuItem.setInnerHTML("Apache HTTPD"); // NON-NLS
    appendChild(menuItem);
    
    appendChild(Node.fromHtml("&emsp;&emsp;&emsp;"));
    
    
    menuItem = new El("a");
    menuItem.classList.add("page-header__menu-item");
    if (currentMenuItem == CurrentMenuItem.JDBC) {
      menuItem.classList.add("page-header__menu-item_current");
    } else {
      menuItem.classList.add("page-header__menu-item_hoverable");
      menuItem.setAttribute("href", "jdbc");
    }
    menuItem.setInnerHTML("JDBC ресурсы"); // NON-NLS
    appendChild(menuItem);
    
    appendChild(Node.fromHtml("&emsp;"));
    
    
    menuItem = new El("a");
    menuItem.classList.add("page-header__menu-item");
    if (currentMenuItem == CurrentMenuItem.LOG) {
      menuItem.classList.add("page-header__menu-item_current");
    } else {
      menuItem.classList.add("page-header__menu-item_hoverable");
      menuItem.setAttribute("href", "log");
    }
    menuItem.setInnerHTML("Логи"); // NON-NLS
    appendChild(menuItem);
    
    appendChild(Node.fromHtml("&emsp;"));
    
    
    menuItem = new El("a");
    menuItem.classList.add("page-header__menu-item");
    if (currentMenuItem == CurrentMenuItem.PORT) {
      menuItem.classList.add("page-header__menu-item_current");
    } else {
      menuItem.classList.add("page-header__menu-item_hoverable");
      menuItem.setAttribute("href", "port");
    }
    menuItem.setInnerHTML("Порты"); // NON-NLS
    appendChild(menuItem);
    
    if (logoutActionUrl != null) {
      final El formReset = new El("form").setAttribute("action", logoutActionUrl).setAttribute("method", "post")
          .addClass("button-form");
      
      final El buttonLogout = new El("button")
          .setAttribute("type", "submit")
          .addClass("page-header__button-logout")
          .addClass("big-black-button")
          .setInnerHTML("ВЫЙТИ"); // NON-NLS
      formReset.appendChild(buttonLogout);
      
      appendChild(formReset);
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
