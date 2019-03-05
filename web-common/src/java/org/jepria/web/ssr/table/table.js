// table.js:
function table_onload() {
  var table = document.getElementById("table-container").firstElementChild;
  
  var fieldsText = table.querySelectorAll("input.field-text");
  for (var i = 0; i < fieldsText.length; i++) {
    fieldsText[i].oninput = function(event){onFieldInput(event.target)};
  }
  
  addFieldsDeleteScript(table);
  
  checkModifications(); // initial
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
  field.classList.remove("invalid");
  
  checkModifications();
}


/**
 * Checks for any user modifications throughout the table
 */
function checkModifications() {
  totalModifications = 
      document.getElementsByClassName("modified").length
      - document.querySelectorAll(".row.created.deleted .modified").length
      + document.getElementsByClassName("row deleted").length;
  
  setSaveButtonEnabled(totalModifications > 0);
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
  
  table.appendChild(rowCreate);
  
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
  
  // TODO set proper tabindexes for control buttons
  
  checkModifications();
}

function onButtonSaveClick() {
	alert("save");
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
  
  // scroll to the created row (bottom)
  window.scrollTo(0, document.body.scrollHeight);
}
// :table.js