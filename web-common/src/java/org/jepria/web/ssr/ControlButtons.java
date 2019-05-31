package org.jepria.web.ssr;

public class ControlButtons extends El {
  
  public ControlButtons(Context context) {
    super("div", context);
    classList.add("control-buttons");
    
    addStyle("css/control-buttons.css");
    addStyle("css/common.css"); // for .big-black-button
    addScript("js/common.js"); // for .big-black-button
  }
  
  public void addButtonCreate() {
    
    final Text text = context.getText();
    
    El buttonCreate = new El("button", context)
        .addClass("control-button")
        .addClass("control-button_create")
        .addClass("big-black-button")
        .setInnerHTML(text.getString("org.jepria.web.ssr.ControlButtons.buttonCreate.text"), true);
    
    appendChild(buttonCreate);
  }
  
  public void addButtonSave(String saveActionUrl) {
    
    final Text text = context.getText();
    
    // TODO if saveActionUrl == null then assign current url
    final El formSave = new El("form", context).setAttribute("action", saveActionUrl).setAttribute("method", "post")
        .addClass("button-form")
        .addClass("control-button-form_save");
    
    El buttonSave = new El("button", context)
        .setAttribute("type", "submit")
        .setAttribute("disabled") // disabled by default
        .addClass("control-button")
        .addClass("control-button_save")
        .addClass("big-black-button")
        .setInnerHTML(text.getString("org.jepria.web.ssr.ControlButtons.buttonSave.text"), true);
        
    buttonSave.setAttribute("org.jepria.web.ssr.ControlButtons.buttonSave.title.save", text.getString("org.jepria.web.ssr.ControlButtons.buttonSave.title.save"));
    buttonSave.setAttribute("org.jepria.web.ssr.ControlButtons.button.title.no_mod", text.getString("org.jepria.web.ssr.ControlButtons.button.title.no_mod"));
    
    formSave.appendChild(buttonSave);
    
    appendChild(formSave);
  }
  
  // TODO this actually is a RELOAD button. It needs no arguments, and must just reload the current url
  public void addButtonReset(String resetActionUrl) {
    
    final Text text = context.getText();
    
    // TODO if resetActionUrl == null then assign current url
    
    final El formReset = new El("form", context).setAttribute("action", resetActionUrl).setAttribute("method", "get")
        .addClass("button-form");
    
    El buttonReset = new El("button", context)
        .setAttribute("type", "submit")
        .setAttribute("disabled") // disabled by default
        .addClass("control-button")
        .addClass("control-button_reset")
        .addClass("big-black-button")
        .setInnerHTML(text.getString("org.jepria.web.ssr.ControlButtons.buttonReset.text"), true);
        
    buttonReset.setAttribute("org.jepria.web.ssr.ControlButtons.buttonReset.title.reset", text.getString("org.jepria.web.ssr.ControlButtons.buttonReset.title.reset"));
    buttonReset.setAttribute("org.jepria.web.ssr.ControlButtons.button.title.no_mod", text.getString("org.jepria.web.ssr.ControlButtons.button.title.no_mod"));
    
    formReset.appendChild(buttonReset);
    
    appendChild(formReset);
  }
}
