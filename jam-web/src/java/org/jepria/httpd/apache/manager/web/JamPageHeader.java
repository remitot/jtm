package org.jepria.httpd.apache.manager.web;

import java.util.ArrayList;
import java.util.List;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.Text;

public class JamPageHeader extends PageHeader {
  
  /**
   * Menu items possibly displayed as currently selected 
   */
  public static enum CurrentMenuItem {
    JK_MODJK,
    JK_WORKERS,
    RESTART,
  }
  
  /**
   * @param currentMenuItem the menu item to be displayed as currently active.
   * If {@code null}, no menu item will be displayed as currently active.
   */
  public JamPageHeader(Context context, CurrentMenuItem currentMenuItem) {
    super(context);
    
    Text text = context.getText();
    
    // create and set items
    final List<El> items = new ArrayList<>();
    
    {
      El itemJkModjk = new MenuItem(context, currentMenuItem == CurrentMenuItem.JK_MODJK, context.getContextPath() + "/jk/modjk");
      itemJkModjk.setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJkModjk"), true);
      items.add(itemJkModjk);
    }
    
    {
      El itemJkWorkers = new MenuItem(context, currentMenuItem == CurrentMenuItem.JK_WORKERS, context.getContextPath() + "/jk/workers");
      itemJkWorkers.setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJkWorkers"), true);
      items.add(itemJkWorkers);
    }
    
    {
      El itemRestart = new MenuItem(context, currentMenuItem == CurrentMenuItem.RESTART, context.getContextPath() + "/restart");
      itemRestart.setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemRestart"), true);
      items.add(itemRestart);
    }
    
    
    // add class to the left item
    if (items.size() > 0) {
      items.get(0).classList.add("page-header__menu-item_left");
    }
    
    setItems(items);
  }
}
