package org.jepria.web.ssr;

import org.jepria.web.ssr.table.Collection;

public class ControlButtons extends El {
  
  public final El buttonCreate;
  public final El buttonSave;
  public final El buttonReset;
  
  public ControlButtons() {
    super("div");
    classList.add("control-buttons");
    
    buttonCreate = new El("button")
        .addClass("control-button")
        .addClass("control-button_create")
        .addClass("big-black-button")
        .setInnerHTML("НОВАЯ ЗАПИСЬ"); // NON-NLS
    
    
    buttonSave = new El("button")
        .addClass("control-button")
        .addClass("control-button_save")
        .addClass("big-black-button")
        .setInnerHTML("СОХРАНИТЬ ВСЁ"); // NON-NLS
    
    
    final El formReset = new El("form").setAttribute("action", "jdbc/mod-reset").setAttribute("method", "post")
        .addClass("control-button-form");
    
    buttonReset = new El("button")
        .setAttribute("type", "submit")
        .addClass("control-button")
        .addClass("control-button_reset")
        .addClass("big-black-button")
        .setInnerHTML("СБРОСИТЬ ВСЁ"); // NON-NLS
    formReset.appendChild(buttonReset);
    
    
    appendChild(buttonCreate);
    appendChild(buttonSave);
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
