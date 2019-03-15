function setButtonSaveEnabled(enabled) {
  var button = document.getElementsByClassName("control-button_save")[0];
  if (enabled) {
    button.disabled = false;
    button.title = "Сохранить все изменения (оранжевые)"; // NON-NLS
  } else {
    button.disabled = true;
    button.title = "Изменений нет"; // NON-NLS
  }
}

function setButtonResetEnabled(enabled) {
  var button = document.getElementsByClassName("control-button_reset")[0];
  if (enabled) {
    button.disabled = false;
    button.title = "Сбросить все изменения (оранжевые)"; // NON-NLS
  } else {
    button.disabled = true;
    button.title = "Изменений нет"; // NON-NLS
  }
}

function controlButtons_onload() {
  document.getElementsByClassName("control-button_create")[0].onclick = onButtonCreateClick;
}
