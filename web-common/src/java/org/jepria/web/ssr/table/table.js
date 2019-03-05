// table.js:
function addTableScript(table) {
  var fieldsText = table.querySelectorAll("input.field-text");
  for (var i = 0; i < fieldsText.length; i++) {
    fieldsText[i].oninput = function(event){onFieldInput(event.target)};
  }
  
  addFieldsDeleteScript(table);
  
  addRowButtonCreateScript(table);
}

function addRowButtonCreateScript(table) {
  var buttonCreate = table.querySelectorAll(".row-button-create__button-create")[0];
  buttonCreate.onclick = function(event){onButtonCreateClick();};
  addHoverForBigBlackButton(buttonCreate);
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
  
  // clear field 'invalid' state
  uiOnFieldValidate(field, true, null);
  field.classList.remove("invalid");
  
  checkModifications();
}

/**
 * @param field
 * @param fieldValid
 * @param invalidMessage message to display if the field is invalid (currently the input's title)
 * @returns
 */
function uiOnFieldValidate(field, fieldValid, invalidMessage) {
  if (!fieldValid) {
    
    field.classList.add("invalid");
    if (invalidMessage) {
      field.setAttribute("title", invalidMessage);
    } else {
      field.removeAttribute("title");
    }
  } else {
    field.classList.remove("invalid");
    field.removeAttribute("title");
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
  
  setControlButtonsEnabled(totalModifications > 0);
  
  // graphics:
  adjustBottomShadow();
}

// TODO the function affects the control buttons only. 
// Better to move into control-buttons.fragment script?
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

/**
 * Sets enability of control buttons (which depend on the table elements modification status)
 */
function setControlButtonsEnabled(enabled) {
  // the function affects the control buttons only, so it is overridden in control-buttons.fragment script
  return null;
}


function addFieldsDeleteScript(composite) {
  var fieldsDelete = composite.querySelectorAll("input.field-delete");
  for (var i = 0; i < fieldsDelete.length; i++) {
    fieldsDelete[i].onclick = function(event){onDeleteButtonClick(event.target)};
  }
}

function onDeleteButtonClick(button) {
  //TODO resolve the relative path:
  var row = button.parentElement.parentElement.parentElement.parentElement;
  
  if (row.classList.contains("created")) {
    // for newly created rows just remove them from table
    row.parentNode.removeChild(row);
    
  } else if (!row.classList.contains("deleted")) {
    row.classList.add("deleted");
    
    // button changes image
    button.src = "gui/img/undelete.png";
    button.title = "Не удалять"; // NON-NLS

    setRowDisabled(row, true);
    // show all delete buttons (as they were disabled too)
    var deleteButtons = document.querySelectorAll("#table .row .cell.column-delete input");
    for (var i = 0; i < deleteButtons.length; i++) {
      deleteButtons[i].classList.remove("hidden");
    }
    
  } else {
    row.classList.remove("deleted");
    
    // button changes image
    button.src = "gui/img/delete.png";
    button.title = "Удалить"; // NON-NLS
    
    setRowDisabled(row, false);
  }
  
  checkModifications();
}

var createRowId = 1;

function onButtonCreateClick() {
  
  var table = document.getElementById("table");

  var tableRowCreateTemplate = document.getElementById("table-row-create-template-container").firstElementChild;
  var rowCreate = tableRowCreateTemplate.cloneNode(true);
  
  rowCreate.setAttribute("item-id", "row-create-" + createRowId++);
  
  var createRowCreate = table.getElementsByClassName("table__row-button-create")[0];
  table.insertBefore(rowCreate, createRowCreate);
  
  addRowCreateScript(rowCreate);
  
  
  //set 'tabindex' attributes by 'tabindex-rel' attribute
  var tabIndexAnchor = +table.getAttribute("tabindex-next");
  var maxTabIndexRel = 0;
  var hasTabIndexRels = Array.from(rowCreate.getElementsByClassName("has-tabindex-rel"));// element.getElementsByClassName FETCHES the elements 
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
  
  
  checkModifications();
}

function addRowCreateScript(rowCreate) {
  addFieldsDeleteScript(rowCreate);
  
  // trigger initial events
  var fields = rowCreate.querySelectorAll("input.field-text");
  for (var i = 0; i < fields.length; i++) {
    onFieldInput(fields[i]);
  }
  
  // focus on the first text input field
  rowCreate.querySelectorAll(".cell input[type='text']")[0].focus();
}
// :table.js