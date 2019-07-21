function textContent_onload() {
  var textarea = document.getElementsByClassName("text-content")[0];
  
  if (textarea) {
    textarea.oninput = function(field) {// javascript doesn't use block scope for variables
      return function() {
        onTextContentInput(field);
      }(textarea);
    }
  }
  
  
  var formSave = document.getElementsByClassName("control-button-form_save")[0];
  
  if (formSave) { // the save form (as indeed the control buttons) might not be present
    formSave.onsubmit = function(event) {
      var dataField = document.createElement("input");
      dataField.type = "hidden";
      dataField.name = "data";
      dataField.value = JSON.stringify(prepareModData());
      formSave.appendChild(dataField);
      return true;
    }
  }
  
  
  checkModifications();
}

function checkModifications() {
  var buttonEnabled = document.querySelectorAll(".text-content.modified").length > 0;
  setButtonSaveEnabled(buttonEnabled);
  setButtonResetEnabled(buttonEnabled);
}

function prepareModData() {
  
  modRequestList = [];
  
  var textarea = document.getElementsByClassName("text-content")[0];
  
  if (textarea) {
    modRequestList.push(
        {
          id: "text-content",
          action: "update", 
          data: {text: textarea.value}
        }
    );
  }
  
  return modRequestList;
}

function onTextContentInput(textarea) {
  var textarea = document.getElementsByClassName("text-content")[0];
  if (textarea) {
    textarea.classList.add("modified");
  }
  
  checkModifications();
}


// TODO the same code is in table.js:
//////control buttons //////

function setButtonSaveEnabled(enabled) {
  var button = document.getElementsByClassName("control-button_save")[0];
  if (button) {
    
    var titleText;
    
    if (enabled) {
      button.disabled = false;
      titleText = button.getAttribute("org.jepria.web.ssr.ControlButtons.buttonSave.title.save");
    } else {
      button.disabled = true;
      titleText = button.getAttribute("org.jepria.web.ssr.ControlButtons.button.title.no_mod");
    }
    
    if (titleText) {
      button.title = titleText;
    } else {
      button.removeAttribute("title");
    }
  }
}

function setButtonResetEnabled(enabled) {
  var button = document.getElementsByClassName("control-button_reset")[0];
  if (button) {
    if (enabled) {
      button.disabled = false;
      button.title = button.getAttribute("org.jepria.web.ssr.ControlButtons.buttonReset.title.reset");
    } else {
      button.disabled = true;
      button.title = button.getAttribute("org.jepria.web.ssr.ControlButtons.button.title.no_mod");
    }
  }
}