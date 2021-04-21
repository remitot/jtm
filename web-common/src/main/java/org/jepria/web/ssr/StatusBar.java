package org.jepria.web.ssr;

import java.io.IOException;

public class StatusBar extends El {
  
  public static enum Type {
    SUCCESS,
    INFO,
    ERROR
  }
  
  protected final El header;
  /**
   * Content part of a header against the close button
   */
  protected final El headerContent;
  protected final El closeButton;
  
  protected Type type = null;
  protected boolean closeable = true;
  
  public StatusBar(Context context) {
    super ("div", context);
    
    classList.add("status-bar");
    classList.add("block-shadow");
    
    { // header
      header = new El("div", context);
      header.addClass("status-bar__header");
      appendChild(header);
      
      headerContent = new El("div", context);
      headerContent.addClass("status-bar__header-content");
      header.appendChild(headerContent);
    }
    
    { // create close button
      closeButton = new El("input", context);
      closeButton.setAttribute("type", "image");
      closeButton.addClass("status-bar__header-close");
      closeButton.setAttribute("src", context.getContextPath() + "/img/close.png");
    }
    
    addScript(new Script("js/status-bar.js", "statusBar_onload"));
    addStyle("css/common.css");
    addStyle("css/status-bar.css");
  }
  
  public void setType(Type type) {
    this.type = type;
  }
  
  @Override
  public void render(Appendable sb) throws IOException {
    beforeRender();
    super.render(sb);
  }
  
  protected void beforeRender() {
    { // add type class
      if (type == Type.SUCCESS) {
        classList.add("status-bar_success");
      } else if (type == Type.INFO) {
        classList.add("status-bar_info");
      } else if (type == Type.ERROR) {
        classList.add("status-bar_error");
      }
    }
    
    { // append close button
      if (closeable) {
        header.appendChild(closeButton);
      }
    }
  }
  
  public El getHeader() {
    return headerContent;
  }
  
  public void setHeaderHTML(String html) {
    headerContent.setInnerHTML(html);
  }
  
  public void setCloseable(boolean closeable) {
    this.closeable = closeable;
  }
}
