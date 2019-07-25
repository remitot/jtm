package org.jepria.httpd.apache.manager.web.restart;

import java.util.ArrayList;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;

public class RestartPageContent extends ArrayList<El> {

  private static final long serialVersionUID = 2085233077868783633L;

  public RestartPageContent(Context context) {

    { // control buttons
      final ControlButtons controlButtons = new ControlButtons(context);
      controlButtons.addScript("js/restart/restart.js");

      final String restartActionUrl = context.getContextPath() + "/restart";
      {
        final Text text = context.getText();

        final El formRestart = new El("form", context).setAttribute("action", restartActionUrl).setAttribute("method", "post")
            .addClass("button-form")
            .addClass("control-button-form_restart");

        El buttonRestart = new El("button", context)
            .setAttribute("type", "submit")
            .addClass("control-button")
            .addClass("control-button_restart")
            .addClass("big-black-button")
            .setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.restart.ControlButtons.buttonRestart.text"), true);

        buttonRestart.setAttribute("title", text.getString("org.jepria.httpd.apache.manager.web.restart.ControlButtons.buttonRestart.title"));

        formRestart.appendChild(buttonRestart);

        controlButtons.appendChild(formRestart);
      }

      add(controlButtons);
    }
    
    
    { // restart statistics     // TODO this is a stub
      El div = new El("div", context);
      div.setInnerHTML("Здесь можно вывесить табличку со статистикой перезагрузок: дата, время, автор перезагрузки, фактическая длительность и т.д.");
      div.setAttribute("style", "color: #808080; padding: 15px");
      add(div);
    }


  }

}
