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

function reloadTable() {
  
  setControlButtonsEnabled(false);
  
  statusInfo("загрузка..."); // NON-NLS
  
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
      
        statusClear();
        
        jsonResponse = JSON.parse(this.responseText);
        jsonItemList = getJsonItemList(jsonResponse); 
        
        refillGrid(jsonItemList, isEditable());
        
      } else if (this.status == 401) {
        statusError("Требуется авторизация"); // NON-NLS
    
        raiseLoginForm(function() {
          hideLoginForm();
          reloadTable();  
        });
      } else if (this.status == 403) {
        statusError("<span class=\"span-bold\">Доступ запрещён.</span>&emsp;<a href=\"#\" onclick=\"changeUser();\">Выйти</a> чтобы сменить пользователя"); // NON-NLS // NON-NLS // NON-NLS
        
      } else {
        statusError("Сетевая ошибка " + this.status); // NON-NLS
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
    
    addHeaderIfNeeded();
    
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
      input.parentElement.classList.remove("modified");
    } else {
      input.parentElement.classList.add("modified");
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
  if (isEditable()) {
    return addFieldInput(cell, name, value, placeholder);
  } else {
    return addFieldLabel(cell, value);    
  }
}

function addFieldInput(cell, name, value, placeholder) {
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
  
  wrapper = wrapCellPad(field);
  
  cell.appendChild(wrapper);
  
  strike = document.createElement("div");
  strike.classList.add("strike");
  cell.appendChild(strike);
  
  return field;
}

function addFieldLabel(cell, value) {
  field = document.createElement("label");
  field.innerHTML = value;
  
  field.classList.add("field-text");
  field.classList.add("inactivatible");
  field.classList.add("deletable");
  
  wrapper = wrapCellPad(field);
  
  cell.appendChild(wrapper);
  
  strike = document.createElement("div");
  strike.classList.add("strike");
  cell.appendChild(strike);
  
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
  button.src = "img/delete.png";
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
  valueOriginal = field.getAttribute("value-original");
  if (typeof valueOriginal === 'undefined') {
    // treat as modified
    field.classList.add("modified");
  } else {
    if (valueOriginal !== field.value) {
      field.classList.add("modified");
    } else {
      field.classList.remove("modified");
    }
  }
  
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
  row = button.parentElement.parentElement.parentElement.parentElement;
  
  rowInputs = row.querySelectorAll("input.deletable");
  
  if (row.classList.contains("created")) {
    // for newly created rows just remove them from table
    row.parentNode.removeChild(row);
    
    removeHeaderIfNeeded();
      
  } else if (!row.classList.contains("deleted")) {
    row.classList.add("deleted");
    
    // button changes image
    button.src = "img/undelete.png";
    button.title = "Не удалять"; // NON-NLS
    
    for (var i = 0; i < rowInputs.length; i++) {
      rowInputs[i].disabled = true;
    }
    
    if (!row.classList.contains("created")) {// no change checkboxes for created rows
      checkboxes = row.getElementsByClassName("checkbox deletable");
      for (var i = 0; i < checkboxes.length; i++) {
        setCheckboxEnabled(checkboxes[i], false);
      }
    }
  } else {
    row.classList.remove("deleted");
    
    // button changes image
    button.src = "img/delete.png";
    button.title = "Удалить"; // NON-NLS
    
    for (var i = 0; i < rowInputs.length; i++) {
      rowInputs[i].disabled = false;
    }
    
    if (!row.classList.contains("created")) {// no change checkboxes for created rows
      checkboxes = row.getElementsByClassName("checkbox deletable");
      for (var i = 0; i < checkboxes.length; i++) {
        setCheckboxEnabled(checkboxes[i], true);
      }
    }
  }
  
  checkModifications();
}

/**
 * Adds the header if the table has no header yet
 */
function addHeaderIfNeeded() {
  var table = document.getElementById("table");
  var tableHeaders = table.getElementsByClassName("header");
  if (tableHeaders.length == 0) {
    var header = createHeader();
    table.insertBefore(header, table.firstChild);
  }
}

/**
 * Removes the header if the table is empty
 */
function removeHeaderIfNeeded() {
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

function onButtonCreateClick() {
  
  addHeaderIfNeeded();
  
  var table = document.getElementById("table");

  rowCreate = createRowCreate();
  table.insertBefore(rowCreate, table.lastChild);
  
  rowCreate.querySelectorAll(".cell input[type='text']")[0].focus(false); // focus on the first text input field
  
  checkModifications();
}

// TODO the logic below is almost JDBC-specific! move it into jdbc.js
function onSaveButtonClick() {
  
  rowsModified = getRowsModified();
  rowsDeleted = getRowsDeleted();
  rowsCreated = getRowsCreated();
  
  
  uiOnSaveBegin();
  
  modRequestList = [];
  
  var id = 0;
  
  if (rowsModified.length > 0) {
    for (var i = 0; i < rowsModified.length; i++) {
      modRequestList.push(
          {
            modRequestId: ++id,
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
            modRequestId: ++id,
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
            modRequestId: ++id,
            modRequestBody: {
              action: "create", 
              data: rowsCreated[i]
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
          uiOnSaveEnd();
          jsonResponse = JSON.parse(this.responseText);
          
          
          modStatusList = jsonResponse.modStatusList;
          
          // check all modStatus ok
          var allModStatusOk = true;
          for (var i = 0; i < modStatusList.length; i++) {
            if (modStatusList[i].modStatusCode != 0) {
              allModStatusOk = false;
              break;
            }
          }
          
          if (!allModStatusOk) {
            message = "<span class=\"span-bold\">Изменения сохранены, но некоторые из них вызвали ошибки.</span>&emsp;Сейчас сервер может перезагрузиться." // NON-NLS // NON-NLS 
                + "&emsp;<a href=\"\" onclick=\"document.location.reload();\">Обновить страницу</a>"; // NON-NLS
            statusError(message);
            
          } else {
            statusBar = document.getElementById("statusBar");
            statusBar.className = "statusBar statusBar-success";
            statusBar.innerHTML = "<span class=\"span-bold\">Изменения успешно сохранены.</span>&emsp;Сейчас сервер может перезагрузиться." // NON-NLS // NON-NLS
                + "&emsp;<a href=\"\" onclick=\"document.location.reload();\">Обновить страницу</a>"; // NON-NLS
          }
          
          
          jsonItemList = getJsonItemList(jsonResponse); 
          refillGrid(jsonItemList, false);
          
          document.getElementsByClassName("control-buttons")[0].style.display = "none";
          
        } else if (this.status == 401) {
          statusError("Требуется авторизация"); // NON-NLS
      
          raiseLoginForm(function() {
            hideLoginForm();
            reloadTable();  
          });
          
        } else if (this.status == 403) {
          statusError("<span class=\"span-bold\">Доступ запрещён.</span>&emsp;<a href=\"#\" onclick=\"changeUser();\">Выйти</a> чтобы сменить пользователя"); // NON-NLS // NON-NLS // NON-NLS
          
        } else {
          statusError("Сетевая ошибка " + this.status); // NON-NLS
        }
      }
    };
    xhttp.open("POST", getApiModUrl(), true);
    
    
    xhttp.send(JSON.stringify(modRequestList));
    
  } else {
    // TODO report nothing to save
    uiOnSaveEnd();
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

function changeUser() {
  logout(function() {
    reloadTable();
  });
}

function logout(afterLogoutCallback) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
        statusClear();
        
        if (afterLogoutCallback != null) {
          afterLogoutCallback();
        }
      } else {
        statusError("Сетевая ошибка " + this.status); // NON-NLS
      }
    }
  };
  xhttp.open("POST", "api/logout", true);
  xhttp.send();
}

function statusClear() {
  statusBar = document.getElementById("statusBar");
  statusBar.className = "statusBar statusBar-none";
  statusBar.innerHTML = "";
}

function statusInfo(message) {
  statusBar = document.getElementById("statusBar"); 
  statusBar.className = "statusBar statusBar-info";
  statusBar.innerHTML = message;
}

function statusError(message) {
  statusBar = document.getElementById("statusBar"); 
  statusBar.className = "statusBar statusBar-error";
  statusBar.innerHTML = message;
}

function uiOnSaveEnd() {
  statusClear();
}

function uiOnSaveBegin() {
  statusInfo("сохраняем..."); // NON-NLS
}

function getRowsModified() {
  var rows = document.querySelectorAll("#table div.row");
  rowsModifiedJson = [];
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    if (!row.classList.contains("deleted") && !row.classList.contains("created") && row.getElementsByClassName("modified").length > 0) {
      rowJson = rowToJson(row);
      rowsModifiedJson.push({itemLocation: row.getAttribute("item-location"), itemData: rowJson});
    }
  }
  return rowsModifiedJson;
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

function getRowsCreated() {
  var rows = document.querySelectorAll("#table div.row");
  rowsCreatedJson = [];
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    if (row.classList.contains("created") && !row.classList.contains("deleted")) {
      rowJson = rowToJson(row);
      rowsCreatedJson.push(rowJson);
    }
  }
  return rowsCreatedJson;
}

function rowToJson(row) {
  rowJson = {};
  fields = row.querySelectorAll(".cell-field input");
  for (var j = 0; j < fields.length; j++) {
    field = fields[j];
    if (field.name === "active") {
      // TODO workaround. If checkbox becomes a class, make its own property 'value'
      rowJson[field.name] = field.checked;
    } else {
      rowJson[field.name] = field.value;
    }
  }
  return rowJson;
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