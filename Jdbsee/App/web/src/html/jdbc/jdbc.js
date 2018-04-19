function reload() {
  
  uiGridReloadBegin();
  
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
      if (this.readyState == 4 && this.status == 200) {
        
        uiGridReloadEnd(this.responseText);
        
      }
  };
  xhttp.open("GET", "jdbc/api/list", true);
  xhttp.send();
}

function uiGridReloadBegin() {
  setButtonSaveEnabled(false);
  document.getElementById("abc").innerHTML = "loading...";
}

function uiGridReloadEnd(responseText) {
  
  var jsonConnections = JSON.parse(responseText);

  document.getElementById("abc").innerHTML = "";
  document.getElementById("connections").innerHTML = "";
  
  if (jsonConnections.length > 0) {
    fillGrid(jsonConnections);
  } else {
    document.getElementById("abc").innerHTML = "no data";
  }
  
  checkModifications();
}

function fillGrid(jsonConnections) {
  var table = document.getElementById("connections");
  
  table.appendChild(createHeader());
  
  for (var i = 0; i < jsonConnections.length; i++) {
    connection = jsonConnections[i];
    
    row = createRow(connection);
    
    table.appendChild(row);
  }
}

function createHeader() {
  row = document.createElement("div");
  row.classList.add("row");
  row.classList.add("header");
  // active
  cell = createCell(row, "column-active");// empty cell
  
  cell = createCell(row, "column-delete");// empty cell
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  
  cell = createCell(div, "column-name");
  label = document.createElement("label");
  label.innerHTML = "Name";
  cell.appendChild(label);
  
  cell = createCell(div, "column-server");
  label = document.createElement("label");
  label.innerHTML = "Server";
  cell.appendChild(label);
  
  cell = createCell(div, "column-db");
  label = document.createElement("label");
  label.innerHTML = "Database";
  cell.appendChild(label);
  
  cell = createCell(div, "column-user");
  label = document.createElement("label");
  label.innerHTML = "User";
  cell.appendChild(label);
  
  cell = createCell(div, "column-password");
  label = document.createElement("label");
  label.innerHTML = "Password";
  cell.appendChild(label);
  
  row.appendChild(div);
  
  return row;
}

function createRow(connection) {
  row = document.createElement("div");
  row.classList.add("row");
  row.setAttribute("connection-location", connection.location);
  
  // active
  cell = createCell(row, "column-active");
  addCheckboxCa(cell, connection.active);
  if (!connection.active) {
    row.classList.add("inactive");
  }
  
  cell = createCell(row, "column-delete");
  addFieldDelete(cell);
  
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  
  cell = createCell(div, "column-name");
  field = addField(cell, "name", connection.name, null);
  field.setAttribute("value0", connection.name);
  
  
  cell = createCell(div, "column-server");
  field = addField(cell, "server", connection.server, null);
  field.setAttribute("value0", connection.server);
  
  cell = createCell(div, "column-db");
  field = addField(cell, "db", connection.db, null);
  field.setAttribute("value0", connection.db);
  
  cell = createCell(div, "column-user");
  field = addField(cell, "user", connection.user, null);
  field.setAttribute("value0", connection.user);
  
  cell = createCell(div, "column-password");
  field = addField(cell, "password", connection.password, null);
  field.setAttribute("value0", connection.password);
  
  row.appendChild(div);
  
  
  return row;
}

function createRowCreate() {
  row = document.createElement("div");
  row.classList.add("row");
  row.classList.add("created");
  
  // active
  cell = createCell(row, "column-active");
  addCheckboxCa(cell, true);
  // disable checkbox
  cell.querySelectorAll("input")[0].disabled = true; 
  
  cell = createCell(row, "column-delete");
  addFieldDelete(cell);
  
  
  flexColumns = document.createElement("div");
  flexColumns.classList.add("flexColumns");
  
  cell = createCell(flexColumns, "column-name");
  field = addField(cell, "name", "", "jdbc/MyDataSource");
  onFieldInput(field);// trigger initial event
 
  cell = createCell(flexColumns, "column-server");
  field = addField(cell, "server", "", "mydbserver.com:1521");
  onFieldInput(field);// trigger initial event
  
  cell = createCell(flexColumns, "column-db");
  field = addField(cell, "db", "", "MYDATABASE");
  onFieldInput(field);// trigger initial event
  
  cell = createCell(flexColumns, "column-user");
  field = addField(cell, "user", "", "me");
  onFieldInput(field);// trigger initial event
  
  cell = createCell(flexColumns, "column-password");
  field = addField(cell, "password", "", "mysecret");
  onFieldInput(field);// trigger initial event
  
  row.appendChild(flexColumns);
  
  return row;
}

function addCheckboxCa(cell, active) {
  checkboxCa = createCheckboxCa(active);
  
  checkboxCa.onclick = function(event){
    onCheckboxCaInput(event.target);
    checkModifications();
  };
  checkboxCa.classList.add("deletable");

  wrapper = wrapCellPad(checkboxCa);  
  
  cell.appendChild(wrapper);
  
  onCheckboxCaInput(checkboxCa.querySelectorAll("input")[0]);// trigger initial event
  
  strike = document.createElement("div");
  strike.classList.add("strike");
  cell.appendChild(strike);
}

function createCheckboxCa(active) {
  var field = document.createElement("label");
  field.classList.add("checkbox-ca");
  
  var input = document.createElement("input");
  input.type = "checkbox";
  input.name = "active";
  input.checked = active;
  input.setAttribute("value0", active);
  input.onfocus = function(event){
    var input = event.target;
    input.parentElement.querySelector(".checkmark").classList.add("hovered");
  }
  input.addEventListener("focusout", function(event) { // .onfocusout not working in some browsers
    var input = event.target;
    input.parentElement.querySelector(".checkmark").classList.remove("hovered");
  });
  field.appendChild(input);
  
  var span = document.createElement("span");
  span.classList.add("checkmark");
  span.onmouseover = function(event) {
    var checkmark = event.target;
    checkmark.classList.add("hovered");
  }
  span.addEventListener("mouseout", function(event) { // .onmouseout not working in some browsers
    var checkmark = event.target;
    checkmark.classList.remove("hovered");
  });
  field.appendChild(span);
  
  return field;
}

function onCheckboxCaInput(input) {
  // this will be SPAN, then INPUT on a single click
  if (input.tagName.toLowerCase() == "input") {
    if (input.checked && input.getAttribute("value0") == "true" || !input.checked && input.getAttribute("value0") == "false") {
      input.parentElement.classList.remove("modified");
    } else {
      input.parentElement.classList.add("modified");
    }
    
    if (!input.checked) {
      //TODO resolve the relative path!
      input.parentElement.parentElement.parentElement.parentElement.parentElement.classList.add("inactive");
      input.parentElement.title = "Connection is inactive";
    } else {
      //TODO resolve the relative path!
      input.parentElement.parentElement.parentElement.parentElement.parentElement.classList.remove("inactive");
      input.parentElement.title = "Connection is active";
    }
  }
}

function addField(cell, name, value, placeholder) {
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

  container = document.createElement("div");
  
  field = document.createElement("input");
  field.type = "image";
  field.src = "jdbc/img/delete.png";
  field.title = "Delete";
  field.classList.add("button-delete");
  field.onclick = function(event){onDeleteButtonClick(event.target);};
  container.appendChild(field);
  
  field = document.createElement("input");
  field.type = "image";
  field.src = "jdbc/img/undelete.png";
  field.title = "Do not delete";
  field.classList.add("button-undelete");
  field.onclick = function(event){onUndeleteButtonClick(event.target);};
  container.appendChild(field);

  wrapper = wrapCellPad(container);  
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
  value0 = field.getAttribute("value0");
  if (typeof value0 === 'undefined') {
    // treat as modified
    field.classList.add("modified");
  } else {
    if (value0 !== field.value) {
      field.classList.add("modified");
    } else {
      field.classList.remove("modified");
    }
  }
  
  checkModifications();
}

function checkModifications() {
  totalModifications = 
      document.querySelectorAll(".modified").length
      - document.querySelectorAll(".row.created.deleted .modified").length
      + document.querySelectorAll(".row.deleted").length
      - document.querySelectorAll(".row.created.deleted").length;
  
  setButtonSaveEnabled(totalModifications > 0);
}

function setButtonSaveEnabled(enabled) {
  buttonSave = document.getElementById("buttonSave");
  if (enabled) {
    buttonSave.disabled = false;  
    buttonSave.title = "Save all modifications (highlighted)";
  } else {
    buttonSave.disabled = true;
    buttonSave.title = "No modifications performed";
  }
}

function onDeleteButtonClick(button) {
  //TODO resolve the relative path!
  row = button.parentElement.parentElement.parentElement.parentElement.parentElement;
  row.classList.add("deleted");
  
  isCreated = row.classList.contains("created");
  
  rowInputs = row.querySelectorAll("input.deletable, .deletable input");
  for (var i = 0; i < rowInputs.length; i++) {
    if (!isCreated) {// no affect for new rows
      rowInputs[i].disabled = true;
    }
  }
  
  checkModifications();
}

function onUndeleteButtonClick(button) {
  //TODO resolve the relative path!
  row = button.parentElement.parentElement.parentElement.parentElement.parentElement;
  row.classList.remove("deleted");
  
  isCreated = row.classList.contains("created");
  
  rowInputs = row.querySelectorAll("input.deletable, .deletable input");
  for (var i = 0; i < rowInputs.length; i++) {
    if (!isCreated) {// no affect for new rows
      rowInputs[i].disabled = false;
    }
  }
  
  checkModifications();
}

function onCreateButtonClick() {
  row = createRowCreate();
  document.getElementById("connections").appendChild(row);
  
  row.querySelectorAll(".cell.column-name input")[0].focus();
  checkModifications();
}

function onSaveButtonClick() {
  
  rowsModified = getRowsModified();
  rowsDeleted = getRowsDeleted();
  rowsCreated = getRowsCreated();
  
  
  uiSaveBegin();
  
  connectionModificationRequests = [];
  
  if (rowsModified.length > 0) {
    for (var i = 0; i < rowsModified.length; i++) {
      connectionLocation = rowsModified[i].connectionLocation;
      connection = rowsModified[i].connection;
      connectionModificationRequests.push(
          {
            action: "update", 
            location: rowsModified[i].connectionLocation, 
            data: rowsModified[i].connection
          }
      );
    }
  }
  
  if (rowsDeleted.length > 0) {
    for (var i = 0; i < rowsDeleted.length; i++) {
      connectionModificationRequests.push(
          {
            action: "delete", 
            location: rowsDeleted[i], 
          }
      );
    }
  }
  
  if (rowsCreated.length > 0) {
    for (var i = 0; i < rowsCreated.length; i++) {
      connectionModificationRequests.push(
          {
            action: "create", 
            data: rowsCreated[i], 
          }
      );
    }
  }
  
  if (connectionModificationRequests.length > 0) {
    xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
          uiSaveEnd();
        }
    };
    xhttp.open("POST", "jdbc/api/mod", true);
    xhttp.send(JSON.stringify(connectionModificationRequests));
    
  } else {
    // TODO report nothing to save
    uiSaveEnd();
  }
}

function uiSaveEnd() {
  document.getElementById("abc").innerHTML = "";
  reload();
}

function uiSaveBegin() {
  document.getElementById("abc").innerHTML = "saving...";
}

function getRowsModified() {
  var rows = document.querySelectorAll("#connections div.row");
  rowsModifiedJson = [];
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    if (!row.classList.contains("deleted") && !row.classList.contains("created") && row.querySelector(".modified") != null) {
      rowJson = rowToJson(row);
      rowsModifiedJson.push({connectionLocation: row.getAttribute("connection-location"), connection: rowJson});
    }
  }
  return rowsModifiedJson;
}

function getRowsDeleted() {
  var rows = document.querySelectorAll("#connections div.row");
  rowsDeletedLocations = [];
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    if (row.classList.contains("deleted") && !row.classList.contains("created")) {
      rowsDeletedLocations.push(row.getAttribute("connection-location"));
    }
  }
  return rowsDeletedLocations;
}

function getRowsCreated() {
  var rows = document.querySelectorAll("#connections div.row");
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
  fields = row.querySelectorAll("input");
  for (var j = 0; j < fields.length; j++) {
    field = fields[j];
    if (field.name === "active") {
      // TODO workaround. If checkboxCa becomes a class, make its own property 'value'
      rowJson[field.name] = field.checked;
    } else {
      rowJson[field.name] = field.value;
    }
  }
  return rowJson;
}

