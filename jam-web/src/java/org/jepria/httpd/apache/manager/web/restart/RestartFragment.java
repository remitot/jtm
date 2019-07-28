package org.jepria.httpd.apache.manager.web.restart;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlEscaper;
import org.jepria.web.ssr.Text;

public class RestartFragment extends El {
  
  public RestartFragment(Context context) {
    super("div", context);
    
    addStyle("css/restart/restart-fragment.css");
    
    addScript(new Script("js/common.js")); // for invoking windowReload() from restart-fragment.js
    addScript(new Script("js/restart/restart-fragment.js", "restart_fragment_onload"));
    
    addClass("restart-fragment");
    
    {
      El mask = new El("div", context);
      mask.addClass("restart-fragment-mask");
      appendChild(mask);
    }
    
    {
      El restartFrame = new El("div", context);
      restartFrame.addClass("restart-fragment-frame");
      {
        El img = new El("img", context);
        img.addClass("restart-fragment-frame__img");
        img.setAttribute("src", context.getContextPath() + "/img/restart/restart.gif");
        restartFrame.appendChild(img);
      }
      
      {
        El infoBar = new El("div", context);
        
        Text text = context.getText();
        
        String innerHTML = 
            HtmlEscaper.escape(text.getString("org.jepria.httpd.apache.manager.web.restart.fragment.info.restart"))
            + "<br/>"
            + HtmlEscaper.escape(text.getString("org.jepria.httpd.apache.manager.web.restart.fragment.info.no_leave"))
            + "<br/>"
            + HtmlEscaper.escape(text.getString("org.jepria.httpd.apache.manager.web.restart.fragment.info.if_leave"));
        
        infoBar.setInnerHTML(innerHTML, false);
        
        restartFrame.appendChild(infoBar);
      }
      
      appendChild(restartFrame);
    }
  }
}
