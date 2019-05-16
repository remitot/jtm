package org.jepria.httpd.apache.manager.web;

import java.util.ArrayList;
import java.util.List;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlEscaper;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.Text;

public class JamPageHeader extends PageHeader {
  
  /**
   * Menu items possibly displayed as currently selected 
   */
  public static enum CurrentMenuItem {
    JK,
    JK_DETAILS,
  }
  
  /**
   * @param currentMenuItem the menu item to be displayed as currently active.
   * If {@code null}, no menu item will be displayed as currently active.
   */
  public JamPageHeader(Text text, CurrentMenuItem currentMenuItem) {
    super(text);
    
    
    // create and set items
    final List<El> items = new ArrayList<>();
    
    if (currentMenuItem == CurrentMenuItem.JK) {
      
      {
        El itemJk = new El("a");
        itemJk.classList.add("page-header__menu-item");
        
        itemJk.classList.add("page-header__menu-item_regular");
        itemJk.classList.add("page-header__menu-item_current");
        
        itemJk.setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJk"), true);
        items.add(itemJk);
      }
      
    } else if (currentMenuItem == CurrentMenuItem.JK_DETAILS) {
      
      {
        El itemJk = new El("a");
        itemJk.classList.add("page-header__menu-item");
        itemJk.classList.add("page-header__menu-item_regular");
        
        itemJk.classList.add("page-header__menu-item_hoverable");
        itemJk.setAttribute("href", "jk");
        
        String innerHtmlEscaped = HtmlEscaper.escape(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJk"));
        itemJk.setInnerHTML(innerHtmlEscaped + "&nbsp/&nbsp", false);
        items.add(itemJk);
      }
      
      {
        El itemJk = new El("a");
        itemJk.classList.add("page-header__menu-item");

        itemJk.classList.add("page-header__menu-item_current");
        
        itemJk.setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJkDetails"), true);
        items.add(itemJk);
      }
      
    }
    
    setItems(items);
  }
}
