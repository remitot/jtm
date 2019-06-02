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
    JK,
    JK_DETAILS,
    JK_NEW_BINDING,
  }
  
  private El itemJkDetails = null;
  
  /**
   * @param currentMenuItem the menu item to be displayed as currently active.
   * If {@code null}, no menu item will be displayed as currently active.
   */
  public JamPageHeader(Context context, CurrentMenuItem currentMenuItem) {
    super(context);
    
    Text text = context.getText();
    
    // create and set items
    final List<El> items = new ArrayList<>();
    
    if (currentMenuItem == CurrentMenuItem.JK) {
      
      {
        El itemJk = new El("a", context);
        itemJk.classList.add("page-header__menu-item");
        
        itemJk.classList.add("page-header__menu-item_regular");
        itemJk.classList.add("page-header__menu-item_current");
        
        itemJk.setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJk"), true);
        items.add(itemJk);
      }
      
    } else if (currentMenuItem == CurrentMenuItem.JK_DETAILS || currentMenuItem == CurrentMenuItem.JK_NEW_BINDING) {
      
      {
        El itemJk = new El("a", context);
        itemJk.classList.add("page-header__menu-item");
        itemJk.classList.add("page-header__menu-item_regular");
        
        itemJk.classList.add("page-header__menu-item_hoverable");
        itemJk.setAttribute("href", context.getContextPath() + "/jk");
        
        itemJk.setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJk"), true);
        items.add(itemJk);
        
        
        El itemSlash = new El("span", context);
        itemSlash.classList.add("page-header__menu-item");
        itemSlash.setInnerHTML("&nbsp/&nbsp");
        items.add(itemSlash);
      }
      
      {
        El itemJk = new El("a", context);
        itemJk.classList.add("page-header__menu-item");

        itemJk.classList.add("page-header__menu-item_current");
      
        if (currentMenuItem == CurrentMenuItem.JK_DETAILS) {
          itemJkDetails = itemJk;
          
          // initial
          setCurrentDetailsMenuItemText(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJkDetails.default"));
        } else {
          itemJk.setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJkNewBinding"), true);
        }
        items.add(itemJk);
      }
      
    }
    
    setItems(items);
  }
  
  /**
   * Sets a text for the current details menu item 
   * (applicable for the headers created with {@link CurrentMenuItem#JK_DETAILS} only, for any other header does nothing). 
   * @param text
   */
  // TODO bad architecture: the method is only JK_DETAILS-specific, senseless for any other CurrentMenuItem type
  public void setCurrentDetailsMenuItemText(String text) {
    if (itemJkDetails != null) {
      itemJkDetails.setInnerHTML(text, true);
    }
  }
}
