/* Common code to draw and manipulate main page tables */


// Abstract functions to be overridden in JS endpoints
function getApiListUrl() {
  console.error("getApiListUrl() function must be overridden in the endpoint JS file (inherited from table.js)");
  return null;
}

function getApiModUrl() {
  console.error("getApiModUrl() function must be overridden in the endpoint JS file (inherited from table.js)");
  return null;
}

function createHeader() {
  console.error("createHeader() function must be overridden in the endpoint JS file (inherited from table.js)");
  return null;
}

function createRow(listItem) {
  console.error("createRow(listItem) function must be overridden in the endpoint JS file (inherited from table.js)");
  return null;
}

function onAfterRefillGrid() {}

/**
 * Validate a single field value before sending to the server in a modification request
 * @param fieldName
 * @param fieldValue
 * @returns true or false
 */
function validate(fieldName, fieldValue) {
  return true;
}

/**
 * After a modification request has been sent to the server and resulted INVALID_FIELD_DATA status,
 * this method is invoked for all invalid fields
 * @param fieldName
 * @param errorCode error code related to the field
 * @param errorCode error message related to the field
 * @returns message to display on the invalid field
 */
function getInvalidFieldMessage(fieldName, errorCode, errorMessage) {
  return null;
}



/**
 * Creates a new empty row for creating a new table record
 */
function createRowCreate() {
  console.error("createRowCreate() function must be overridden in the endpoint JS file (inherited from table.js)");
  return null;
}

function getJsonItemList(jsonResponse) {
  return jsonResponse._list;
}

function uiOnTableReloadSuccess() {
  statusClear();
}

function uiOnTableReloadError(status) {
  if (status == 401) {
    statusError("Требуется авторизация"); // NON-NLS
    
    raiseLoginForm(function() {
      hideLoginForm();
      table_reload();  
    });
    
  } else if (status == 403) {
    var message = "<span class=\"span-bold\">Доступ запрещён.</span>&emsp;<a href=\"#\" onclick=\"logout(table_reload);\">Выйти</a> чтобы сменить пользователя"; // NON-NLS // NON-NLS // NON-NLS 
    statusError(message);
    
  } else {
    statusError("Ошибка " + status); // NON-NLS
  }
}

/**
 * All ModRequests succeeded, all modifications saved on the server
 * @returns
 */
function uiOnTableModSuccess() {
  document.getElementsByClassName("control-buttons")[0].style.display = "none";
  
  var message = "<span class=\"span-bold\">Все изменения успешно сохранены на сервере.</span>&emsp;Сейчас сервер может перезагрузиться." // NON-NLS // NON-NLS
    + "&emsp;<a href=\"\" onclick=\"document.location.reload();\">Обновить страницу</a>"; // NON-NLS
  statusSuccess(message);
}

/**
 * Not all ModRequests succeeded (but possibly some of them), no modifications performed on the server, some ModRequests resulted INVALID_FIELD_DATA status
 */
function uiOnTableModNotSuccessInvalidFieldData() {
  var message = "При попытке сохранить изменения обнаружились некорректные значения полей (выделены красным). " +
      "<span class=\"span-bold\">На сервере всё осталось без изменений.</span>"; // NON-NLS 
  statusError(message);
}

/**
 * Not all ModRequests succeeded (but possibly some of them), no modifications performed on the server
 */
function uiOnTableModNotSuccess() {
  var message = "<span class=\"span-bold\">При попытке сохранить изменения произошла ошибка. На сервере всё осталось без изменений.</span>"; // NON-NLS 
  statusError(message);
}

function uiOnAfterFieldsValidated(fieldsValid) {
  if (!fieldsValid) {
    statusError("Исправьте некорректные значения полей (выделены красным)"); // NON-NLS
  }
}

/**
  * Public API
  */
function table_reload() {
  
  setControlButtonsEnabled(false);
  
  statusInfo("загрузка..."); // NON-NLS
  
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
      
        jsonResponse = JSON.parse(this.responseText);
        jsonItemList = getJsonItemList(jsonResponse); 
        
        refillGrid(jsonItemList, isEditable());

        uiOnTableReloadSuccess();
        
      } else {
        uiOnTableReloadError(this.status);
      }
    }
  };
  xhttp.open("GET", getApiListUrl(), true);
  xhttp.send();
}

function refillGrid(jsonItemList, editable) {
  table = document.getElementById("table");
  table.innerHTML = "";
  
  if (jsonItemList.length > 0) {
    
    addHeaderFooter();
    
    for (var i = 0; i < jsonItemList.length; i++) {
      listItem = jsonItemList[i];
      
      row = createRow(listItem);
      
      table.appendChild(row);
    }
    
    checkModifications();
  }
  

  if (editable) {
    rowButtonCreate = createRowButtonCreate();
    table.appendChild(rowButtonCreate);
  } else {
    disableGrid();
  }
  
  onAfterRefillGrid();
}

function createRowButtonCreate() {
  row = document.createElement("div");
  row.classList.add("row-button-create");
  
  // active
  cell = createCell(row, "column-button-create");
  cell.classList.add("column-left");
  
  buttonCreate = document.createElement("button");
  buttonCreate.classList.add("big-black-button");
  buttonCreate.innerHTML = "НОВАЯ ЗАПИСЬ"; // NON-NLS
  buttonCreate.onclick = function(event){onButtonCreateClick();};
  
  addHoverForBigBlackButton(buttonCreate);
  
  addHoverForBigBlackButton(buttonCreate);
  
  wrapper = wrapCellPad(buttonCreate);
  
  cell.appendChild(wrapper);
  
  return row;
}

function setCheckboxEnabled(checkbox, enabled) {
  if (enabled) {
    checkbox.classList.remove("checkbox-disabled");
    checkbox.getElementsByTagName("input")[0].disabled = false;
  } else {
    checkbox.classList.add("checkbox-disabled");
    checkbox.getElementsByTagName("input")[0].disabled = true;
  }
}

function addCheckbox(cell, active, enabled) {
  checkbox = createCheckbox(active);
  
  setCheckboxEnabled(checkbox, enabled);
  
  checkbox.onclick = function(event){
    onCheckboxInput(event.target);
    checkModifications();
  };
  checkbox.classList.add("deletable");

  wrapper = wrapCellPad(checkbox);  
  
  cell.appendChild(wrapper);
  
  onCheckboxInput(checkbox.getElementsByTagName("input")[0]);// trigger initial event
  
  strike = document.createElement("div");
  strike.classList.add("strike");
  cell.appendChild(strike);
}

function createCheckbox(active) {
  var field = document.createElement("label");
  field.classList.add("checkbox");
  
  var input = document.createElement("input");
  input.type = "checkbox";
  input.name = "active";
  input.checked = active;
  input.setAttribute("value-original", active);
  field.appendChild(input);
  
  var span = document.createElement("span");
  span.classList.add("checkmark");
  field.appendChild(span);


  // add 'hovered' class for checkbox's onfocus and onmouseover  
  input.onfocus = function(event){
    var input = event.target;
    input.parentElement.getElementsByClassName("checkmark")[0].classList.add("hovered");
  }
  input.addEventListener("focusout", function(event) { // .onfocusout not working in some browsers
    var input = event.target;
    input.parentElement.getElementsByClassName("checkmark")[0].classList.remove("hovered");
  });
  span.onmouseover = function(event) {
    var checkmark = event.target;
    checkmark.classList.add("hovered");
  }
  span.addEventListener("mouseout", function(event) { // .onmouseout not working in some browsers
    var checkmark = event.target;
    checkmark.classList.remove("hovered");
  });

  
  return field;
}

function onCheckboxInput(input) {
  // this will be SPAN, then INPUT on a single click
  if (input.tagName.toLowerCase() == "input") {
    if (input.checked && input.getAttribute("value-original") == "true" 
        || !input.checked && input.getAttribute("value-original") == "false") {
      // affect both input (to get the modified fields selected by input.modified) 
      // and label (to graphically display the field modification state)
      input.parentElement.classList.remove("modified");
      input.classList.remove("modified");
    } else {
      // affect both input (to get the modified fields selected by input.modified) 
      // and label (to graphically display the field modification state)
      input.parentElement.classList.add("modified");
      input.classList.add("modified");
    }
    
    if (!input.checked) {
      //TODO resolve the relative path:
      input.parentElement.parentElement.parentElement.parentElement.parentElement.classList.add("inactive");
      input.parentElement.title = "Запись неактивна"; // NON-NLS
    } else {
      //TODO resolve the relative path:
      input.parentElement.parentElement.parentElement.parentElement.parentElement.classList.remove("inactive");
      input.parentElement.title = "Запись активна"; // NON-NLS
    }
  }
}

function addField(cell, name, value, placeholder) {
  field = createField(name, value, placeholder);
  
  wrapper = wrapCellPad(field);
  cell.appendChild(wrapper);

  if (isEditable()) {
    addStrike(cell);
  }
    
  return field;
}

function createField(name, value, placeholder) {
  if (isEditable()) {
    return createFieldInput(name, value, placeholder);
  } else {
    return createFieldLabel(value);
  }
}

function createFieldInput(name, value, placeholder) {
  field = document.createElement("input");
  field.type = "text";
  field.name = name;
  field.value = value;
  if (placeholder != null) {
    field.placeholder = placeholder;
  }
  
  field.oninput = function(event){onFieldInput(event.target)};
  field.classList.add("field-text");
  field.classList.add("inactivatible");
  field.classList.add("deletable");
  
  return field;
}

function addStrike(cell) {
  strike = document.createElement("div");
  strike.classList.add("strike");
  cell.appendChild(strike);
}

function createFieldLabel(value) {
  field = document.createElement("label");
  field.innerHTML = value;
  
  field.classList.add("field-text");
  field.classList.add("inactivatible");
  field.classList.add("deletable");
  
  return field;
}

function wrapCellPad(element) {
  wrapper = document.createElement("div");
  
  leftDiv = document.createElement("div");
  leftDiv.classList.add("cell-pad-left");
  wrapper.appendChild(leftDiv);
  
  leftDiv = document.createElement("div");
  leftDiv.classList.add("cell-pad-right");
  wrapper.appendChild(leftDiv);
  
  midDiv = document.createElement("div");
  midDiv.classList.add("cell-pad-mid");
  wrapper.appendChild(midDiv);
  
  midDiv.appendChild(element);
  
  return wrapper;
}

function addFieldDelete(cell) {

  button = document.createElement("input");
  button.type = "image";
  button.src = "gui/img/delete.png";
  button.title = "Удалить"; // NON-NLS
  button.onclick = function(event){onDeleteButtonClick(event.target);};
  
  wrapper = wrapCellPad(button);  
  cell.appendChild(wrapper);
}

function createCell(row, columnClass) {
  cell = document.createElement("div");
  cell.classList.add("cell");
  cell.classList.add(columnClass);
  row.appendChild(cell);
  return cell;
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

/**
 * Sets enability of control buttons (which depend on the table elements modification status)
 */
function setControlButtonsEnabled(enabled) {
  // the function affects the control buttons only, so it is overridden in control-buttons.fragment script
  return null;
}

function onDeleteButtonClick(button) {
  //TODO resolve the relative path:
  var row = button.parentElement.parentElement.parentElement.parentElement;
  
  var disableableElements = row.querySelectorAll("input.deletable");
  
  if (row.classList.contains("created")) {
    // for newly created rows just remove them from table
    row.parentNode.removeChild(row);
    
    removeHeaderFooter();
      
  } else if (!row.classList.contains("deleted")) {
    row.classList.add("deleted");
    
    // button changes image
    button.src = "gui/img/undelete.png";
    button.title = "Не удалять"; // NON-NLS
    
    for (var i = 0; i < disableableElements.length; i++) {
      disableableElements[i].disabled = true;
    }
    
    if (!row.classList.contains("created")) {// no change checkboxes for created rows
      var checkboxes = row.getElementsByClassName("checkbox deletable");
      for (var i = 0; i < checkboxes.length; i++) {
        setCheckboxEnabled(checkboxes[i], false);
      }
    }
  } else {
    row.classList.remove("deleted");
    
    // button changes image
    button.src = "gui/img/delete.png";
    button.title = "Удалить"; // NON-NLS
    
    for (var i = 0; i < disableableElements.length; i++) {
      disableableElements[i].disabled = false;
    }
    
    if (!row.classList.contains("created")) {// no change checkboxes for created rows
      var checkboxes = row.getElementsByClassName("checkbox deletable");
      for (var i = 0; i < checkboxes.length; i++) {
        setCheckboxEnabled(checkboxes[i], true);
      }
    }
  }
  
  checkModifications();
}

/**
 * Adds header and footer if the table has no header yet
 */
function addHeaderFooter() {
  var table = document.getElementById("table");
  var tableHeaders = table.getElementsByClassName("header");
  if (tableHeaders.length == 0) {
    var header = createHeader();
    table.insertBefore(header, table.firstChild);
  }
}

/**
 * Removes header and footer if the table is empty
 */
function removeHeaderFooter() {
  var table = document.getElementById("table");
  
  rows = table.getElementsByClassName("row");
  if (rows.length == 0) {
    var tableHeaders = table.getElementsByClassName("header");
    if (tableHeaders.length != 0) {
      var header = tableHeaders[0];
      header.parentNode.removeChild(header);
    }
  }
}

var createRowId = 1;

function onButtonCreateClick() {
  
  addHeaderFooter();
  
  var table = document.getElementById("table");

  var rowCreate = createRowCreate();
  rowCreate.setAttribute("item-location", "row-create-" + createRowId++);
  
  table.insertBefore(rowCreate, table.lastChild);
  
  rowCreate.querySelectorAll(".cell input[type='text']")[0].focus(); // focus on the first text input field
  
  checkModifications();
}

// TODO the logic below is almost JDBC-specific! move it into jdbc.js
function onSaveButtonClick() {
  
  
  
  var rowsModified = getRowsModified();
  var rowsDeleted = getRowsDeleted();
  var rowsCreated = getRowsCreated();
  
  if (!rowsModified.rowsValid || !rowsCreated.rowsValid) {
    uiOnAfterFieldsValidated(false);
    return;
  } else {
    uiOnAfterFieldsValidated(true);
  }
  
  
  uiOnSaveBegin();
  
  
  rowsModified = rowsModified.data;
  rowsCreated = rowsCreated.data;
  
  modRequestList = [];
  
  if (rowsModified.length > 0) {
    for (var i = 0; i < rowsModified.length; i++) {
      modRequestList.push(
          {
            modRequestId: rowsModified[i].itemLocation, // same as location
            modRequestBody: {
              action: "update", 
              location: rowsModified[i].itemLocation, 
              data: rowsModified[i].itemData
            }
          }
      );
    }
  }
  
  if (rowsDeleted.length > 0) {
    for (var i = 0; i < rowsDeleted.length; i++) {
      modRequestList.push(
          {
            modRequestId: rowsDeleted[i], // same as location
            modRequestBody: {
              action: "delete", 
              location: rowsDeleted[i]
            }
          }
      );
    }
  }
  
  if (rowsCreated.length > 0) {
    for (var i = 0; i < rowsCreated.length; i++) {
      modRequestList.push(
          {
            modRequestId: rowsCreated[i].itemLocation, // same as location
            modRequestBody: {
              action: "create", 
              data: rowsCreated[i].itemData
            }
          }
      );
    }
  }
  
  if (modRequestList.length > 0) {
    xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        
      if (this.readyState == 4) {
        
        if (this.status == 200) {
          
          jsonResponse = JSON.parse(this.responseText);
          
          
          modStatusList = jsonResponse.modStatusList;
          
          // check all modifications succeeded
          var allModSuccess = true;
          var invalidFieldDataStatus = false;
          
          for (var i = 0; i < modStatusList.length; i++) {
            if (modStatusList[i].modStatusCode != 0) {
              allModSuccess = false;
            }
            if (modStatusList[i].modStatusCode == 1) {
              // invalid field data
              invalidFieldDataStatus = true;
              var invalidFieldDataMap = modStatusList[i].invalidFieldData;
              for (fieldName in invalidFieldDataMap) {
                onInvalidFieldData(modStatusList[i].modRequestId, fieldName, invalidFieldDataMap[fieldName].errorCode, invalidFieldDataMap[fieldName].errorMessage);
              }
            }
          }
          
          if (allModSuccess) {
            
            uiOnTableModSuccess();
            
            jsonItemList = getJsonItemList(jsonResponse); 
            refillGrid(jsonItemList, false);
            
          } else if (invalidFieldDataStatus) {
            uiOnTableModNotSuccessInvalidFieldData();
          } else {
            uiOnTableModNotSuccess();
          }
          
        } else {
          uiOnTableReloadError(this.status);
        }
        
        uiOnSaveEnd();
      }
    };
    xhttp.open("POST", getApiModUrl(), true);
    
    
    xhttp.send(JSON.stringify(modRequestList));
    
  } else {
    // TODO report nothing to save
    uiOnSaveEnd();
  }
}

/**
 * @param modRequestId
 * @param fieldName
 * @param errorTag
 * @param errorMessage
 * @returns
 */
function onInvalidFieldData(modRequestId, fieldName, errorCode, errorMessage) {
  var rows = document.querySelectorAll("#table div.row");
  for (var i = 0; i < rows.length; i++) {
    var row = rows[i];
    if (row.getAttribute("item-location") == modRequestId) {
      var fields = row.querySelectorAll("input");
      for (var j = 0; j < fields.length; j++) {
        var field = fields[j];
        if (field.getAttribute("name") == fieldName) {
          var message = getInvalidFieldMessage(fieldName, errorCode, errorMessage);
          uiOnFieldValidate(field, false, message);
          return;
        }
      }
      return;
    }
  }
}

function disableGrid() {

  table = document.getElementById("table");
  // remove column-delete contents
  columnDeletes = table.getElementsByClassName("column-delete");
  for (var i = 0; i < columnDeletes.length; i++) {
    columnDelete = columnDeletes[i];
    columnDelete.innerHTML = "";
  }
  
  inputs = table.getElementsByTagName("input");
  for (var i = 0; i < inputs.length; i++) {
    input = inputs[i];
    
    // when set 'input.disabled = true' then unable to select text in a field, so set readonly 
    input.setAttribute("readonly", true);
    input.classList.add("readonly");
  }
  checkboxes = table.getElementsByClassName("checkbox");
  for (var i = 0; i < checkboxes.length; i++) {
    checkbox = checkboxes[i];
    setCheckboxEnabled(checkbox, false);
  }
  
  // gray out every second row
  rows = table.getElementsByClassName("row");
  for (var i = 0; i < rows.length; i += 2) {
    rows[i].classList.add("even-odd-gray");
  }
}

function uiOnSaveEnd() {
  setControlButtonsEnabled(true);
}

function uiOnSaveBegin() {
  setControlButtonsEnabled(false);
  statusInfo("сохраняем..."); // NON-NLS
}

/**
 * Prepare and validate data for rows modified
 * @return {data: [{itemLocation: string, itemData: {}}, ...], rowsValid: boolean}
 */
function getRowsModified() {
  var rows = document.querySelectorAll("#table div.row");
  var rowsValid = true;
  var data = [];
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    if (!row.classList.contains("deleted") && !row.classList.contains("created") && row.getElementsByClassName("modified").length > 0) {
      var rowData = collectRowData(row);
      data.push({itemLocation: row.getAttribute("item-location"), itemData: rowData.data});
      if (!rowData.rowValid) {
        rowsValid = false;
      }
    }
  }
  return {data: data, rowsValid: rowsValid};
}

function getRowsDeleted() {
  var rows = document.querySelectorAll("#table div.row");
  rowsDeletedLocations = [];
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    if (row.classList.contains("deleted") && !row.classList.contains("created")) {
      rowsDeletedLocations.push(row.getAttribute("item-location"));
    }
  }
  return rowsDeletedLocations;
}

/**
 * Prepare and validate data for rows modified
 * @return {data: [{}, ...], rowsValid: boolean}
 */
function getRowsCreated() {
  var rows = document.querySelectorAll("#table div.row");
  var rowsValid = true;
  var data = [];
  for (var i = 0; i < rows.length; i++) {
    var row = rows[i];
    if (row.classList.contains("created") && !row.classList.contains("deleted")) {
      var rowData = collectRowData(row);
      data.push({itemLocation: row.getAttribute("item-location"), itemData: rowData.data});
      if (!rowData.rowValid) {
        rowsValid = false;
      }
    }
  }
  return {data: data, rowsValid: rowsValid};
}

/**
 * Prepare and validate row data (modified fields only)
 * @param row to collect data from
 * @return {rowValid: boolean, data: {}}
 */
function collectRowData(row) {
  var rowValid = true;
  var data = {};
  
  var fields = row.querySelectorAll(".cell-field input.modified");
  
  for (var j = 0; j < fields.length; j++) {
    var field = fields[j];
    var fieldValid = true;
    if (field.name === "active") {
      // TODO workaround. If checkbox becomes a class, make its own property 'value'
      data[field.name] = field.checked;
    } else {
      fieldValid = validate(field.name, field.value);
      data[field.name] = field.value;
    }
    if (!fieldValid) {
      rowValid = false;
    }
    uiOnFieldValidate(field, fieldValid, null);
  }
  
  return {rowValid: rowValid, data: data};
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

/**
 * Overridable function: whether the table is editable or readonly in general
 */
function isEditable() {
  return true;
}

window.onscroll = adjustBottomShadow;