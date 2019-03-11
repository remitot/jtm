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

function adjustBottomShadow() {
  var controlButtons = document.getElementsByClassName("control-buttons")[0];
  
  if (controlButtons != null) {
   if (document.getElementById("table").getBoundingClientRect().bottom <= 
     controlButtons.getBoundingClientRect().top) {
     controlButtons.classList.remove("bottom-shadow");
   } else {
     controlButtons.classList.add("bottom-shadow");
   }
  }
}

window.onscroll = adjustBottomShadow;

function controlButtons_onload() {
  var controlButtons = document.getElementsByClassName("control-buttons")[0];
  
  controlButtons.getElementsByClassName("control-button_create")[0].onclick = onButtonCreateClick;
  controlButtons.getElementsByClassName("control-button_save")[0].onclick = onButtonSaveClick;
  controlButtons.getElementsByClassName("control-button_reset")[0].onclick = onButtonResetClick;
}
