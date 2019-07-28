function table_onload() {
  var table = document.getElementsByClassName("table")[0];
  
  if (table) {
    
    var fieldsText = table.querySelectorAll("input.field-text");
    for (var i = 0; i < fieldsText.length; i++) {
      var fieldText = fieldsText[i];
      
      fieldText.oninput = function(field) {// javascript doesn't use block scope for variables
        return function() {
          onFieldInput(field);
        
          // clear field 'invalid' state on manual value change only
          field.classList.remove("invalid");
          field.removeAttribute("title"); // TODO what if a field's title is not related to the invalid state?
        }
      }(fieldText);
    }
    
    var checkboxes = document.getElementsByClassName("table__checkbox");
    for (var i = 0; i < checkboxes.length; i++) {
      var checkbox = checkboxes[i];
      
      checkbox.onclick = function(checkbox) {// javascript doesn't use block scope for variables
        return function(event) {
          // JS note: the handler is being invoked twice per single click 
          // (first for onclick:event.target=SPAN, second for onclick:event.target=INPUT)
          if (event.target.tagName.toLowerCase() == "input") {
            onCheckboxInput(checkbox);
            
            // checkbox cannot have invalid value: either it is not modifiable at all, or the value is valid
          }
        }
      }(checkbox);
    }
    
    addRowDeleteScript(table);
    
    // disable deleted rows
    var deletedRows = table.querySelectorAll(".row.deleted");
    for (var i = 0; i < deletedRows.length; i++) {
      setDisabled(deletedRows[i], true);
    }
    
    triggerFieldsInput(table);
    
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
    
    adjustBottomShadow();
  }
}

function onFieldInput(field) {
  onFieldValueChanged(field, field.value);
}

function onFieldValueChanged(field, newValue) {
  valueOriginal = field.getAttribute("value-original");
  if (typeof valueOriginal === 'undefined') {
    // treat as modified
    field.classList.add("modified");
  } else {
    if (valueOriginal !== newValue) {
      field.classList.add("modified");
    } else {
      field.classList.remove("modified");
    }
  }
  
  checkModifications();
}

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
    //TODO resolve the relative path:
    var row = checkbox.parentElement.parentElement.parentElement;
    if (!row.classList.contains("row")) {
      console.error("The relative path did not match the row");
    }
    
    row.classList.add("inactive");
    var title = checkbox.getAttribute("org.jepria.web.ssr.field.CheckBox.title.inactive");
    if (title) {
      checkbox.title = title;
    } else {
      checkbox.removeAttribute("title");
    }
  } else {
    //TODO resolve the relative path:
    var row = checkbox.parentElement.parentElement.parentElement;
    if (!row.classList.contains("row")) {
      console.error("The relative path did not match the row");
    }
    
    row.classList.remove("inactive");
    var title = checkbox.getAttribute("org.jepria.web.ssr.field.CheckBox.title.active");
    if (title) {
      checkbox.title = title;
    } else {
      checkbox.removeAttribute("title");
    }
  }
  
  checkModifications();
}



/**
 * Checks for any user modifications throughout the table
 */
function checkModifications() {
  //TODO check modifications not through the document, but through the particular table
  totalModifications = 
      document.getElementsByClassName("modified").length
      - document.querySelectorAll(".row.created.deleted .modified").length
      + document.querySelectorAll(".row.deleted").length;
  
  var buttonEnabled = totalModifications > 0;
  setButtonSaveEnabled(buttonEnabled);
  setButtonResetEnabled(buttonEnabled);
}

/**
 * Disable everything '.table__field_disableable' in a composite
 * @param disabled
 * @returns
 */
function setDisabled(composite, disabled) {
  var disableableElements = composite.querySelectorAll(
      "input.table__field_disableable, button.table__field_disableable");

  for (var i = 0; i < disableableElements.length; i++) {
    var disableableElement = disableableElements[i]; 
    disableableElement.disabled = disabled;
    // disableableElement.setAttribute("readonly", true); // alternative
  }
  
  var checkboxes = composite.querySelectorAll(".checkbox.table__field_disableable");
  for (var i = 0; i < checkboxes.length; i++) {
    var checkbox = checkboxes[i];
    setCheckboxEnabled(checkbox, !disabled);
  }
}

function addRowDeleteScript(composite) {
  var buttonsDelete = composite.querySelectorAll(".row input.button-delete");
  for (var i = 0; i < buttonsDelete.length; i++) {
    buttonsDelete[i].onclick = function(event){onDeleteButtonClick(event.target)};
  }
}

function onDeleteButtonClick(button) {
  //TODO resolve the relative path:
  var row = button.parentElement.parentElement.parentElement.parentElement;
  if (!row.classList.contains("row")) {
    console.error("The relative path did not match the row");
  }
  
  if (row.classList.contains("created")) {
    // for newly created rows just remove them from table
    row.parentNode.removeChild(row);
    
  } else if (!row.classList.contains("deleted")) {
    row.classList.add("deleted");

    setDisabled(row, true);
    
  } else {
    row.classList.remove("deleted");
    
    setDisabled(row, false);
  }
  
  checkModifications();
}

function onButtonCreateClick() {
  
  var table = document.getElementsByClassName("table")[0];
  
  if (table) {

    var newRowTemplate = document.getElementsByClassName("table-new-row-template-container")[0].firstElementChild;
    var newRow = newRowTemplate.cloneNode(true);
    
    table.appendChild(newRow);
    
    addNewRowScript(newRow);
    
    
    //set 'tabindex' attributes by 'tabindex-rel' attribute
    var tabIndexAnchor = +table.getAttribute("tabindex-next");
    var maxTabIndexRel = 0;
    var hasTabIndexRels = Array.from(newRow.getElementsByClassName("has-tabindex-rel"));// element.getElementsByClassName FETCHES the elements 
    for (var i = 0; i < hasTabIndexRels.length; i++) {
      var hasTabIndexRel = hasTabIndexRels[i];
      var tabIndexRel = +hasTabIndexRel.getAttribute("tabindex-rel");
      maxTabIndexRel = Math.max(maxTabIndexRel, tabIndexRel);
      hasTabIndexRel.setAttribute("tabindex", tabIndexAnchor + tabIndexRel);
      
      // cleanup
      hasTabIndexRel.removeAttribute("tabindex-rel");
      hasTabIndexRel.classList.remove("has-tabindex-rel");
    }
    table.setAttribute("tabindex-next", tabIndexAnchor + maxTabIndexRel + 1);
    
    // TODO set proper tabindexes for control buttons
    
    
    // focus on the first text input field
    newRow.querySelectorAll(".cell input[type='text']")[0].focus();
    
    // scroll to the created row (bottom)
    window.scrollTo(0, document.body.scrollHeight);
    
    
    checkModifications();
  }
}

function prepareModData() {
  
  var rowsModified = getRowsModified();
  var rowsDeleted = getRowsDeleted();
  var rowsCreated = getRowsCreated();
  
  modRequestList = [];
  
  if (rowsModified.length > 0) {
    for (var i = 0; i < rowsModified.length; i++) {
      modRequestList.push(
          {
            id: rowsModified[i].itemId, // same as id
            action: "update", 
            data: rowsModified[i].itemData
          }
      );
    }
  }
  
  if (rowsDeleted.length > 0) {
    for (var i = 0; i < rowsDeleted.length; i++) {
      modRequestList.push(
          {
            id: rowsDeleted[i], // same as id
            action: "delete"
          }
      );
    }
  }
  
  if (rowsCreated.length > 0) {
    for (var i = 0; i < rowsCreated.length; i++) {
      modRequestList.push(
          {
            id: "row-create-" + (i + 1),
            action: "create",
            data: rowsCreated[i].itemData
          }
      );
    }
  }
  
  return modRequestList;
}

function getRowsModified() {
  var rows = document.querySelectorAll(".table div.row");
  var data = [];
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    if (!row.classList.contains("deleted") && !row.classList.contains("created") && row.getElementsByClassName("modified").length > 0) {
      var rowData = collectRowData(row);
      data.push({itemId: row.getAttribute("item-id"), itemData: rowData});
    }
  }
  return data;
}

function getRowsDeleted() {
  var rows = document.querySelectorAll(".table div.row");
  rowsDeletedIds = [];
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    if (row.classList.contains("deleted") && !row.classList.contains("created")) {
      rowsDeletedIds.push(row.getAttribute("item-id"));
    }
  }
  return rowsDeletedIds;
}

function getRowsCreated() {
  var rows = document.querySelectorAll(".table div.row");
  var data = [];
  for (var i = 0; i < rows.length; i++) {
    var row = rows[i];
    if (row.classList.contains("created") && !row.classList.contains("deleted")) {
      var rowData = collectRowData(row);
      data.push({itemId: row.getAttribute("item-id"), itemData: rowData});
    }
  }
  return data;
}

/**
 * Collect modified row fields
 * @param row to collect data from
 */
function collectRowData(row) {
  var data = {};
  
  var fields = row.querySelectorAll(".cell-field input.modified");
  
  for (var j = 0; j < fields.length; j++) {
    var field = fields[j];
    if (field.name === "active") {
      data[field.name] = field.checked;
    } else {
      data[field.name] = field.value;
    }
  }
  
  return data;
}

function addNewRowScript(rowCreate) {
  addRowDeleteScript(rowCreate);
  triggerFieldsInput(rowCreate);
}

function triggerFieldsInput(composite) {
  var fields = composite.querySelectorAll("input.field-text");
  for (var i = 0; i < fields.length; i++) {
    onFieldInput(fields[i]);
  }
  
  var checkboxes = composite.querySelectorAll(".checkbox");
  for (var i = 0; i < checkboxes.length; i++) {
    onCheckboxInput(checkboxes[i]);
  }
}

function adjustBottomShadow() {
  var controlButtons = document.getElementsByClassName("control-buttons")[0];
  var table = document.getElementsByClassName("table")[0];
  
  if (controlButtons) {
    if (table) {
      if (table.getBoundingClientRect().bottom <= 
        controlButtons.getBoundingClientRect().top) {
        controlButtons.classList.remove("bottom-shadow");
      } else {
        controlButtons.classList.add("bottom-shadow");
      }
    }
  }
}

window.onscroll = adjustBottomShadow;




////// control buttons //////

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

function table__controlButtons_onload() {
  var button = document.getElementsByClassName("control-button_create")[0];
  if (button) {
    button.onclick = onButtonCreateClick;
  }
}
