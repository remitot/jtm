package org.jepria.httpd.apache.manager.web;

import java.util.ArrayList;
import java.util.List;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.Text;

public class JamPageHeader extends PageHeader {
  
  /**
   * Menu items possibly displayed as currently selected 
   */
  public static enum CurrentMenuItem {
    JK,
  }
  
  /**
   * @param currentMenuItem the menu item to be displayed as currently active.
   * If {@code null}, no menu item will be displayed as currently active.
   */
  public JamPageHeader(Text text, CurrentMenuItem currentMenuItem) {
    super(text);
    
    
    // create and set items
    final List<El> items = new ArrayList<>();
    
    {
      El itemJk = new El("a");
      itemJk.classList.add("page-header__menu-item");
      itemJk.classList.add("page-header__menu-item_regular");
      if (currentMenuItem == CurrentMenuItem.JK) {
        itemJk.classList.add("page-header__menu-item_current");
      } else {
        itemJk.classList.add("page-header__menu-item_hoverable");
        itemJk.setAttribute("href", "jdbc");
      }
      itemJk.setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJk"), true);
      items.add(itemJk);
    }
    
    setItems(items);
  }
}
