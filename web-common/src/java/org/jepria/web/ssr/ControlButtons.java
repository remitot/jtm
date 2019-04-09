package org.jepria.web.ssr;

public class ControlButtons extends El {
  
  public final El buttonCreate;
  public final El buttonSave;
  public final El buttonReset;
  
  public ControlButtons(String saveActionUrl, String resetActionUrl) {
    super("div");
    classList.add("control-buttons");
    
    buttonCreate = new El("button")
        .addClass("control-button")
        .addClass("control-button_create")
        .addClass("big-black-button")
        .setInnerHTML("НОВАЯ ЗАПИСЬ"); // NON-NLS
    
    
    final El formSave = new El("form").setAttribute("action", saveActionUrl).setAttribute("method", "post")
        .addClass("button-form")
        .addClass("control-button-form_save");
    
    buttonSave = new El("button")
        .setAttribute("type", "submit")
        .setAttribute("disabled") // disabled by default
        .addClass("control-button")
        .addClass("control-button_save")
        .addClass("big-black-button")
        .setInnerHTML("СОХРАНИТЬ ВСЁ"); // NON-NLS
    formSave.appendChild(buttonSave);
    
    
    final El formReset = new El("form").setAttribute("action", resetActionUrl).setAttribute("method", "post")
        .addClass("button-form");
    
    buttonReset = new El("button")
        .setAttribute("type", "submit")
        .setAttribute("disabled") // disabled by default
        .addClass("control-button")
        .addClass("control-button_reset")
        .addClass("big-black-button")
        .setInnerHTML("СБРОСИТЬ ВСЁ"); // NON-NLS
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
