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
