package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;

public class BindingDetailsControlButtons extends ControlButtons {
  
  public BindingDetailsControlButtons(Context context) {
    super(context);
  }
  
  @Override
  public void addButtonSave(String saveActionUrl) {
    
    final Text text = context.getText();
    
    final El form = new El("form", context).setAttribute("action", saveActionUrl).setAttribute("method", "post")
        .addClass("button-form")
        .addClass("control-button-form_save");
    
    El button = new El("button", context)
        .setAttribute("type", "submit")
        .setAttribute("disabled") // disabled by default
        .addClass("control-button")
        .addClass("control-button_save")
        .addClass("big-black-button")
        .setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.jk.ControlButtons.buttonSave.text"), true);
        
    form.appendChild(button);
    
    appendChild(form);
  }
  
  public void addButtonDelete(String deleteActionUrl) {
    
    final Text text = context.getText();
    
    final El form = new El("form", context).setAttribute("action", deleteActionUrl).setAttribute("method", "post")
        .addClass("button-form");
    
    El button = new El("button", context)
        .setAttribute("type", "submit")
        .addClass("control-button")
        .addClass("big-black-button")
        .setInnerHTML(text.getString("org.jepria.httpd.apache.manager.web.jk.ControlButtons.buttonDelete.text"), true);
        
    form.appendChild(button);
    
    appendChild(form);
  }
}
