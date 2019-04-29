function setButtonSaveEnabled(enabled) {
  var button = document.getElementsByClassName("control-button_save")[0];
  if (enabled) {
    button.disabled = false;
    button.title = button.getAttribute("org.jepria.web.ssr.ControlButtons.buttonSave.title.save");
  } else {
    button.disabled = true;
    button.title = button.getAttribute("org.jepria.web.ssr.ControlButtons.button.title.no_mod");
  }
}

function setButtonResetEnabled(enabled) {
  var button = document.getElementsByClassName("control-button_reset")[0];
  if (enabled) {
    button.disabled = false;
    button.title = button.getAttribute("org.jepria.web.ssr.ControlButtons.buttonReset.title.reset");
  } else {
    button.disabled = true;
    button.title = button.getAttribute("org.jepria.web.ssr.ControlButtons.button.title.no_mod");
  }
}

function controlButtons_onload() {
  document.getElementsByClassName("control-button_create")[0].onclick = onButtonCreateClick;
}
