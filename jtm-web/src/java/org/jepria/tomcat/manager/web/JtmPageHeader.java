package org.jepria.tomcat.manager.web;

import java.util.ArrayList;
import java.util.List;

import org.jepria.web.ssr.Context;
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
  public JtmPageHeader(Context context, String managerApacheHref, CurrentMenuItem currentMenuItem) {
    super(context);
    
    Text text = context.getText();
    
    // create and set items
    final List<El> items = new ArrayList<>();
    
    {
      if (managerApacheHref != null) {
        El itemManagerApache = new El("a", context);
        itemManagerApache.classList.add("page-header__menu-item");
        itemManagerApache.classList.add("page-header__line-element");
        itemManagerApache.classList.add("page-header__menu-item_apache-httpd");
        itemManagerApache.setAttribute("href", managerApacheHref);
        itemManagerApache.classList.add("page-header__menu-item_hoverable");
        
        itemManagerApache.setAttribute("target", "_blank");
        itemManagerApache.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemApacheHTTPD"), true);
        
        items.add(itemManagerApache);
      }
    }
    
    {
      El itemJdbc = new MenuItem(context, currentMenuItem == CurrentMenuItem.JDBC, context.getContextPath() + "/jdbc");
      itemJdbc.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemJdbc"), true);
      items.add(itemJdbc);
    }
    
    {
      El itemLog = new MenuItem(context, currentMenuItem == CurrentMenuItem.LOG, context.getContextPath() + "/log");
      itemLog.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemLog"), true);
      items.add(itemLog);
    }
    
    {
      El itemPort = new MenuItem(context, currentMenuItem == CurrentMenuItem.PORT, context.getContextPath() + "/port");
      itemPort.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemPort"), true);
      items.add(itemPort);
    }
    

    // add class to the left item
    if (items.size() > 0) {
      items.get(0).classList.add("page-header__menu-item_left");
    }
    
    setItems(items);
  }
}
