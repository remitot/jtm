// table__checkbox.js:
function onCheckboxInput(checkbox) {
    
  if (getInput(checkbox).checked && checkbox.getAttribute("value-original") == "true" 
      || !getInput(checkbox).checked && checkbox.getAttribute("value-original") == "false") {
    // affect both input (to get the modified fields selected by input.modified) 
    // and label (to graphically display the field modification state)
    checkbox.classList.remove("modified");
    getInput(checkbox).classList.remove("modified");
  } else {
    // affect both input (to get the modified fields selected by input.modified) 
    // and label (to graphically display the field modification state)
    checkbox.classList.add("modified");
    getInput(checkbox).classList.add("modified");
  }
  
  if (!getInput(checkbox).checked) {
    //TODO resolve the relative path to ".row" :
    checkbox.parentElement.parentElement.parentElement.parentElement.classList.add("inactive");
    checkbox.title = "Запись неактивна"; // NON-NLS
  } else {
    //TODO resolve the relative path to ".row":
    checkbox.parentElement.parentElement.parentElement.parentElement.classList.remove("inactive");
    checkbox.title = "Запись активна"; // NON-NLS
  }
  
  checkModifications();
}

(function() {
  // TODO find elements relative to the particular table, not to the entire document
  var tableCheckboxes = document.getElementsByClassName("table__checkbox");
  for (var i = 0; i < tableCheckboxes.length; i++) {
    var checkbox = tableCheckboxes[i];
    
    checkbox.onclick = function(checkbox) {// javascript doesn't use block scope for variables
      return function(event) {
        // JS note: the handler is being invoked twice per single click 
        // (first for onclick:event.target=SPAN, second for onclick:event.target=INPUT)
        if (event.target.tagName.toLowerCase() == "input") {
          onCheckboxInput(checkbox);
        }
      }
    }(checkbox);
  }
}());
// :table__checkbox.js