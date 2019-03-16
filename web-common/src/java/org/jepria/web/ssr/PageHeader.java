package org.jepria.web.ssr;

import org.jepria.web.ssr.table.Collection;

public class PageHeader extends El {
  
  public final El itemManagerApache;
  public final El itemJdbc;
  public final El itemLog;
  public final El itemPort;
  public final El buttonLogout;
  
  /**
   * Menu items possibly displayed as currently selected 
   */
  public static enum CurrentMenuItem {
    JDBC,
    LOG,
    PORT,
  }
  
  /**
   * @param managerApacheHref html {@code href} value for the 'Apache HTTPD' menu item. 
   * If {@code null}, the menu item will not be added to the {@link PageHeader} 
   * @param logoutActionUrl html {@code action} value of the {@code form} to submit on logout button click. 
   * If {@code null}, the button will not be added to the {@link PageHeader}
   * @param currentMenuItem the menu item to be displayed as currently active.
   * If {@code null}, no menu item will be displayed as currently active.
   */
  public PageHeader(String managerApacheHref, String logoutActionUrl, CurrentMenuItem currentMenuItem) {
    super("div");
    classList.add("page-header");
    
    
    if (managerApacheHref != null) {
      itemManagerApache = new El("a");
      itemManagerApache.classList.add("page-header__menu-item");
      itemManagerApache.setAttribute("href", managerApacheHref);
      itemManagerApache.classList.add("page-header__menu-item_hoverable");
      
      itemManagerApache.setAttribute("target", "_blank");
      itemManagerApache.setInnerHTML("Apache HTTPD"); // NON-NLS
      appendChild(itemManagerApache);
      
      appendChild(Node.fromHtml("&emsp;&emsp;&emsp;"));
      
    } else {
      // TODO remain empty space?
      itemManagerApache = null;
    }
    
    
    itemJdbc = new El("a");
    itemJdbc.classList.add("page-header__menu-item");
    if (currentMenuItem == CurrentMenuItem.JDBC) {
      itemJdbc.classList.add("page-header__menu-item_current");
    } else {
      itemJdbc.classList.add("page-header__menu-item_hoverable");
      itemJdbc.setAttribute("href", "jdbc");
    }
    itemJdbc.setInnerHTML("JDBC ресурсы"); // NON-NLS
    appendChild(itemJdbc);
    
    appendChild(Node.fromHtml("&emsp;&ensp;"));
    
    
    itemLog = new El("a");
    itemLog.classList.add("page-header__menu-item");
    if (currentMenuItem == CurrentMenuItem.LOG) {
      itemLog.classList.add("page-header__menu-item_current");
    } else {
      itemLog.classList.add("page-header__menu-item_hoverable");
      itemLog.setAttribute("href", "log");
    }
    itemLog.setInnerHTML("Логи"); // NON-NLS
    appendChild(itemLog);
    
    appendChild(Node.fromHtml("&emsp;&ensp;"));
    
    
    itemPort = new El("a");
    itemPort.classList.add("page-header__menu-item");
    if (currentMenuItem == CurrentMenuItem.PORT) {
      itemPort.classList.add("page-header__menu-item_current");
    } else {
      itemPort.classList.add("page-header__menu-item_hoverable");
      itemPort.setAttribute("href", "port");
    }
    itemPort.setInnerHTML("Порты"); // NON-NLS
    appendChild(itemPort);
    
    
    if (logoutActionUrl != null) {
      final El formReset = new El("form").setAttribute("action", logoutActionUrl).setAttribute("method", "post")
          .addClass("button-form");
      
      buttonLogout = new El("button")
          .setAttribute("type", "submit")
          .addClass("page-header__button-logout")
          .addClass("big-black-button")
          .setInnerHTML("ВЫЙТИ"); // NON-NLS
      formReset.appendChild(buttonLogout);
      
      appendChild(formReset);
    } else {
      buttonLogout = null;
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
