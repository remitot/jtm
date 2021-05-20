package org.jepria.tomcat.manager.web;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JtmPageHeader extends PageHeader {
  
  /**
   * Menu items possibly displayed as currently selected 
   */
  public enum CurrentMenuItem {
    JDBC,
    LOG,
    PORT,
    ORACLE
  }
  
  /**
   * @param managerApacheHref html {@code href} value for the 'Apache HTTPD' menu item. 
   * If {@code null} then no such item is added
   * @param currentMenuItem the menu item to be displayed as currently active.
   * If {@code null}, no menu item will be displayed as currently active.
   */
  public JtmPageHeader(Context context, String managerApacheHref, CurrentMenuItem currentMenuItem) {
    super(context);
  
    this.addStyle("css/jtm-page-header.css");
    
    Text text = context.getText();
    
    // create and set items
    final List<El> items = new ArrayList<>();
    
    {
      if (managerApacheHref != null) {
        El itemManagerApache = new MenuItem(context, false, managerApacheHref);
        itemManagerApache.addClass("page-header__menu-item_apache-httpd");

        itemManagerApache.setAttribute("target", "_blank");
        itemManagerApache.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemApacheHTTPD"), true);
        
        items.add(itemManagerApache);
      }
    }
    
    {
      El itemJdbc = new MenuItem(context, currentMenuItem == CurrentMenuItem.JDBC, context.getAppContextPath() + "/jdbc");
      itemJdbc.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemJdbc"), true);
      items.add(itemJdbc);
    }
    
    {
      El itemLog = new MenuItem(context, currentMenuItem == CurrentMenuItem.LOG, context.getAppContextPath() + "/log");
      itemLog.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemLog"), true);
      items.add(itemLog);
    }
    
    {
      El itemPort = new MenuItem(context, currentMenuItem == CurrentMenuItem.PORT, context.getAppContextPath() + "/port");
      itemPort.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemPort"), true);
      items.add(itemPort);
    }

//    {
//      El itemOracle = new MenuItem(context, currentMenuItem == CurrentMenuItem.ORACLE, context.getAppContextPath() + "/oracle");
//      itemOracle.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemOracle"), true);
//      items.add(itemOracle);
//    }
  
    {
      // add warning if the application is accessed without port
      String url = context.getRequestURL();
      Matcher m = Pattern.compile("([a-z][a-z0-9+\\-.]*://[^/]+).*").matcher(url);// regex to determine whether or not the port number is specified in URL
      if (m.matches()) {
        String fullDomain = m.group(1);
        if (!fullDomain.matches(".+:\\d+")) {
          
          String fullDomainWithPortExample = fullDomain + ":8080";
          
          El itemNoPortAccessWarning = new El("img", context);
          itemNoPortAccessWarning.setAttribute("src", context.getAppContextPath() + "/img/header__no-port-access-warning.png");
          itemNoPortAccessWarning.setAttribute("title", String.format(text.getString("org.jepria.tomcat.manager.web.PageHeader.itemNoPortAccessWarning"), fullDomainWithPortExample));
          itemNoPortAccessWarning.addClass("page-header__menu-item_no-port-access-warning");
          items.add(itemNoPortAccessWarning);
        }
      } else {
        // TODO do nothing or fail if the URL is incorrect?
      }
    }
  
    // add class to the left item
    if (items.size() > 0) {
      items.get(0).classList.add("page-header__menu-item_left");
    }
    
    setItems(items);
  }
}
