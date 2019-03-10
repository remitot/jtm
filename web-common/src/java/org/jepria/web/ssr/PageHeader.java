package org.jepria.web.ssr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

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
  public PageHeader(String managerApacheHref, CurrentMenuItem currentMenuItem) {
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
    
    
    final El buttonLogout = new El("div");
    buttonLogout.classList.add("page-header__button-logout");
    buttonLogout.classList.add("big-black-button");
    buttonLogout.setInnerHTML("ВЫЙТИ"); // NON-NLS
    appendChild(buttonLogout);
  }
  
  @Override
  protected void addStyles(Collection styles) throws IOException {
    super.addStyles(styles);
    
    
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = PageHeader.class.getClassLoader(); // fallback
    }

    
    try (InputStream in = classLoader.getResourceAsStream("org/jepria/web/ssr/page-header.css");
        Scanner sc = new Scanner(in, "UTF-8")) {
      sc.useDelimiter("\\Z");
      if (sc.hasNext()) {
        styles.add(sc.next());
      }
    }
  }
}
