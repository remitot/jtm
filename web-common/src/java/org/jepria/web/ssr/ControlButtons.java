package org.jepria.web.ssr;

import org.jepria.web.ssr.table.Collection;

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
  }
  
  @Override
  protected void addStyles(Collection styles) {
    super.addStyles(styles);
    styles.add("css/control-buttons.css");
    styles.add("css/jtm-common.css"); // for .big-black-button
  }
  
  @Override
  protected void addScripts(Collection scripts) {
    super.addScripts(scripts);
    scripts.add("js/control-buttons.js");
    scripts.add("js/jtm-common.js"); // for .big-black-button
  }
}
