// ControlButtons.js:
function setControlButtonsEnabled(enabled) {
  var saveButton = document.getElementsByClassName("control-button_save")[0];
  if (enabled) {
    saveButton.disabled = false;
    saveButton.title = "Сохранить все изменения (оранжевые)"; // NON-NLS
  } else {
    saveButton.disabled = true;
    saveButton.title = "Изменений нет"; // NON-NLS
  }
}

//TODO the function affects the control buttons only. 
//Better to move into control-buttons.fragment script?
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
// :ControlButtons.js