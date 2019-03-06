// ControlButtons.js:
function setSaveButtonEnabled(enabled) {
  var saveButton = document.getElementsByClassName("control-button_save")[0];
  if (enabled) {
    saveButton.disabled = false;
    saveButton.title = "Сохранить все изменения (оранжевые)"; // NON-NLS
  } else {
    saveButton.disabled = true;
    saveButton.title = "Изменений нет"; // NON-NLS
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
  
  controlButtons.getElementsByClassName("control-button_create")[0].onclick = onButtonCreateClick; // TODO reference to function from Table.js
  controlButtons.getElementsByClassName("control-button_save")[0].onclick = onButtonSaveClick; // TODO reference to function from Table.js
}
// :ControlButtons.js