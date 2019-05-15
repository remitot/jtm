package org.jepria.tomcat.manager.web;

import java.util.ArrayList;
import java.util.List;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.Text;

public class JtmPageHeader extends PageHeader {
  
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
   * If {@code null} then no such item is added
   * @param currentMenuItem the menu item to be displayed as currently active.
   * If {@code null}, no menu item will be displayed as currently active.
   */
  public JtmPageHeader(Text text, String managerApacheHref, CurrentMenuItem currentMenuItem) {
    super(text);
    
    
    // create and set items
    final List<El> items = new ArrayList<>();
    
    {
      if (managerApacheHref != null) {
        El itemManagerApache = new El("a");
        itemManagerApache.classList.add("page-header__menu-item");
        itemManagerApache.classList.add("page-header__menu-item_apache-httpd");
        itemManagerApache.setAttribute("href", managerApacheHref);
        itemManagerApache.classList.add("page-header__menu-item_hoverable");
        
        itemManagerApache.setAttribute("target", "_blank");
        itemManagerApache.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemApacheHTTPD"), true);
        
        items.add(itemManagerApache);
        
        addStyle("css/jtm-page-header.css");
      }
    }
    
    {
      El itemJdbc = new El("a");
      itemJdbc.classList.add("page-header__menu-item");
      itemJdbc.classList.add("page-header__menu-item_regular");
      if (currentMenuItem == CurrentMenuItem.JDBC) {
        itemJdbc.classList.add("page-header__menu-item_current");
      } else {
        itemJdbc.classList.add("page-header__menu-item_hoverable");
        itemJdbc.setAttribute("href", "jdbc");
      }
      itemJdbc.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemJdbc"), true);
      items.add(itemJdbc);
    }
    
    {
      El itemLog = new El("a");
      itemLog.classList.add("page-header__menu-item");
      itemLog.classList.add("page-header__menu-item_regular");
      if (currentMenuItem == CurrentMenuItem.LOG) {
        itemLog.classList.add("page-header__menu-item_current");
      } else {
        itemLog.classList.add("page-header__menu-item_hoverable");
        itemLog.setAttribute("href", "log");
      }
      itemLog.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemLog"), true);
      items.add(itemLog);
    }
    
    {
      El itemPort = new El("a");
      itemPort.classList.add("page-header__menu-item");
      itemPort.classList.add("page-header__menu-item_regular");
      if (currentMenuItem == CurrentMenuItem.PORT) {
        itemPort.classList.add("page-header__menu-item_current");
      } else {
        itemPort.classList.add("page-header__menu-item_hoverable");
        itemPort.setAttribute("href", "port");
      }
      itemPort.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemPort"), true);
      items.add(itemPort);
    }
    
    setItems(items);
  }
}