// script for the TableCheckBox class
function onCheckboxInput(checkbox) {
  var input = checkbox.getElementsByTagName("input")[0];
    
  if (input.checked && checkbox.getAttribute("value-original") == "true" 
      || !input.checked && checkbox.getAttribute("value-original") == "false") {
    // affect both input (to get the modified fields selected by input.modified) 
    // and label (to graphically display the field modification state)
    checkbox.classList.remove("modified");
    input.classList.remove("modified");
  } else {
    // affect both input (to get the modified fields selected by input.modified) 
    // and label (to graphically display the field modification state)
    checkbox.classList.add("modified");
    input.classList.add("modified");
  }
  
  if (!input.checked) {
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