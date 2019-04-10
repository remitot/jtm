package org.jepria.web.ssr;

public class ControlButtons extends El {
  
  public final El buttonCreate;
  public final El buttonSave;
  public final El buttonReset;
  
  public ControlButtons(Context context, String saveActionUrl, String resetActionUrl) {
    super("div", context);
    classList.add("control-buttons");
    
    buttonCreate = new El("button", context)
        .addClass("control-button")
        .addClass("control-button_create")
        .addClass("big-black-button")
        .setInnerHTML(context.getText("org.jepria.web.ssr.ControlButtons.buttonCreate.text"), true);
    
    
    final El formSave = new El("form", context).setAttribute("action", saveActionUrl).setAttribute("method", "post")
        .addClass("button-form")
        .addClass("control-button-form_save");
    
    buttonSave = new El("button", context)
        .setAttribute("type", "submit")
        .setAttribute("disabled") // disabled by default
        .addClass("control-button")
        .addClass("control-button_save")
        .addClass("big-black-button")
        .setInnerHTML(context.getText("org.jepria.web.ssr.ControlButtons.buttonSave.text"), true);
    formSave.appendChild(buttonSave);
    
    
    final El formReset = new El("form", context).setAttribute("action", resetActionUrl).setAttribute("method", "post")
        .addClass("button-form");
    
    buttonReset = new El("button", context)
        .setAttribute("type", "submit")
        .setAttribute("disabled") // disabled by default
        .addClass("control-button")
        .addClass("control-button_reset")
        .addClass("big-black-button")
        .setInnerHTML(context.getText("org.jepria.web.ssr.ControlButtons.buttonReset.text"), true);
    formReset.appendChild(buttonReset);
    
    
    appendChild(buttonCreate);
    appendChild(formSave);
    appendChild(formReset);
    
    
    addStyle("css/control-buttons.css");
    addStyle("css/jtm-common.css"); // for .big-black-button
    addScript("js/control-buttons.js");
    addScript("js/jtm-common.js"); // for .big-black-button
  }
}
