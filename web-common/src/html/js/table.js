function table_onload() {
  var table = document.getElementById("table");
  
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
  
  addFieldsDeleteScript(table);
  
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

  adjustBottomShadow();
}

function onFieldInput(field) {
  if (!field.classList.contains("readonly")) {// ignore events for readonly fields
    onFieldValueChanged(field, field.value);
  }
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
  if (!checkbox.classList.contains("readonly")) {// ignore events for readonly fields
    
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
      checkbox.title = checkbox.getAttribute("org.jepria.web.ssr.Table.checkbox_active.title.inactive");
    } else {
      //TODO resolve the relative path to ".row":
      checkbox.parentElement.parentElement.parentElement.parentElement.classList.remove("inactive");
      checkbox.title = checkbox.getAttribute("org.jepria.web.ssr.Table.checkbox_active.title.active");
    }
    
    checkModifications();
  }
}



/**
 * Checks for any user modifications throughout the table
 */
function checkModifications() {
  totalModifications = 
      document.getElementsByClassName("modified").length
      - document.querySelectorAll(".row.created.deleted .modified").length
      + document.getElementsByClassName("row deleted").length;
  
  var buttonEnabled = totalModifications > 0;
  setButtonSaveEnabled(buttonEnabled);
  setButtonResetEnabled(buttonEnabled);
}

/**
 * Disable everything '.deletable' in a composite
 * @param disabled
 * @returns
 */
function setDisabled(composite, disabled) {
  var disableableElements = composite.querySelectorAll("input.deletable, button.deletable");

  for (var i = 0; i < disableableElements.length; i++) {
    var disableableElement = disableableElements[i]; 
    disableableElement.disabled = disabled;
    // disableableElement.setAttribute("readonly", true); // alternative
  }
  
  var checkboxes = composite.querySelectorAll(".checkbox.deletable");
  for (var i = 0; i < checkboxes.length; i++) {
    var checkbox = checkboxes[i];
    if (!checkbox.classList.contains("readonly")) {
      setCheckboxEnabled(checkbox, !disabled);
    }
  }
}

function addFieldsDeleteScript(composite) {
  var fieldsDelete = composite.querySelectorAll("input.button-delete");
  for (var i = 0; i < fieldsDelete.length; i++) {
    fieldsDelete[i].onclick = function(event){onDeleteButtonClick(event.target)};
  }
}

function onDeleteButtonClick(button) {
  //TODO resolve the relative path:
  var row = button.parentElement.parentElement.parentElement.parentElement.parentElement;
  
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
  
  var table = document.getElementById("table");

  var newRowTemplate = document.getElementById("table-new-row-template-container").firstElementChild;
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
  var rows = document.querySelectorAll("#table div.row");
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
  var rows = document.querySelectorAll("#table div.row");
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
  var rows = document.querySelectorAll("#table div.row");
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
  addFieldsDeleteScript(rowCreate);
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