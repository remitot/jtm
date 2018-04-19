function reload() {
  
  uiGridReloadBegin();
  
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
      if (this.readyState == 4 && this.status == 200) {
        
        uiGridReloadEnd(this.responseText);
        
      }
  };
  xhttp.open("GET", "api/list", true);
  xhttp.send();
}

function uiGridReloadBegin() {
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
}

function fillGrid(jsonConnections) {
  var table = document.getElementById("connections");
  
  for (var i = 0; i < jsonConnections.length; i++) {
    connection = jsonConnections[i];
    
    row = createRow(connection);
    
    table.appendChild(row);
  }
}

function createRow(connection) {
  row = document.createElement("div");
  row.classList.add("row");
  row.setAttribute("connection-location", connection.location);
  
  // active
  cell = createCell(row, "column-active");
  addFieldActive(cell, connection.active);
  if (!connection.active) {
    row.classList.add("inactive");
  }
  
  cell = createCell(row, "column-delete");
  addFieldDelete(cell);
  
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  
  cell = createCell(div, "column-name");
  addField(cell, "name", connection.name, null);
  
  
  cell = createCell(div, "column-server");
  addField(cell, "server", connection.server, null);
  
  
  cell = createCell(div, "column-db");
  addField(cell, "db", connection.db, null);
  
  
  cell = createCell(div, "column-user");
  addField(cell, "user", connection.user, null);
  
  
  cell = createCell(div, "column-password");
  addField(cell, "password", connection.password, null);
  
  row.appendChild(div);
  
  
  return row;
}

function createRowCreate() {
  row = document.createElement("div");
  row.classList.add("row");
  row.classList.add("created");
  
  // active
  cell = createCell(row, "column-active");
  addFieldActive(cell, true);
  // disable checkbox
  cell.querySelectorAll("input")[0].disabled = true; 
  
  cell = createCell(row, "column-delete");
  addFieldDelete(cell);
  
  
  flexColumns = document.createElement("div");
  flexColumns.classList.add("flexColumns");
  
  cell = createCell(flexColumns, "column-name");
  addField(cell, "name", "", "jdbc/MyDataSource");
 
  
  cell = createCell(flexColumns, "column-server");
  addField(cell, "server", "", "mydbserver.com:1521");
  
  
  cell = createCell(flexColumns, "column-db");
  addField(cell, "db", "", "MYDATABASE");
  
  
  cell = createCell(flexColumns, "column-user");
  addField(cell, "user", "", "me");
  
  
  cell = createCell(flexColumns, "column-password");
  addField(cell, "password", "", "mysecret");
  
  row.appendChild(flexColumns);
  
  return row;
}

function addFieldActive(cell, active) {
  checkboxActive = createCheckboxActive(active);
  checkboxActive.onclick = function(event){onCheckboxActiveInput(event)};
  checkboxActive.classList.add("deletable");

  wrapper = wrapCellPad(checkboxActive);  
  
  cell.appendChild(wrapper);
  
  strike = document.createElement("div");
  strike.classList.add("strike");
  cell.appendChild(strike);
}

function addField(cell, name, value, placeholder) {
  field = document.createElement("input");
  field.type = "text";
  field.name = name;
  field.value = value;
  if (placeholder != null) {
    field.placeholder = placeholder;
  }
  field.setAttribute("value0", value);
  field.oninput = function(event){onFieldInput(event)};
  field.classList.add("field-text");
  field.classList.add("inactivatible");
  field.classList.add("deletable");
  
  wrapper = wrapCellPad(field);
  
  cell.appendChild(wrapper);
  
  strike = document.createElement("div");
  strike.classList.add("strike");
  cell.appendChild(strike);
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
  field.src = "img/delete.png";
  field.title = "Delete";
  field.classList.add("button-delete");
  field.onclick = function(event){onDeleteButtonClick(event);};
  container.appendChild(field);
  
  field = document.createElement("input");
  field.type = "image";
  field.src = "img/undelete.png";
  field.title = "Do not delete";
  field.classList.add("button-undelete");
  field.onclick = function(event){onUndeleteButtonClick(event);};
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

function onFieldInput(event) {
  var field = event.target;
  if (field.getAttribute("value0") !== field.value) {
    field.classList.add("modified");
  } else {
    field.classList.remove("modified");
  }
}

function onDeleteButtonClick(event) {
  button = event.target;
  //TODO resolve the relative path!
  row = button.parentElement.parentElement.parentElement.parentElement.parentElement;
  row.classList.add("deleted");
  
  rowInputs = row.querySelectorAll("input.deletable, .deletable input");
  for (var i = 0; i < rowInputs.length; i++) {
    if (!row.classList.contains("created")) {// no affect for new rows
      rowInputs[i].disabled = true;
    }
  }
}

function onUndeleteButtonClick(event) {
  button = event.target;
  //TODO resolve the relative path!
  row = button.parentElement.parentElement.parentElement.parentElement.parentElement;
  row.classList.remove("deleted");
  
  rowInputs = row.querySelectorAll("input.deletable, .deletable input");
  for (var i = 0; i < rowInputs.length; i++) {
    if (!row.classList.contains("created")) {// no affect for new rows
      rowInputs[i].disabled = false;
    }
  }
}

function onCreateButtonClick() {
  row = createRowCreate();
  document.getElementById("connections").appendChild(row);
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
    xhttp.open("POST", "api/mod", true);
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
      // TODO workaround. If checkboxActive becomes a class, make its own property 'value'
      rowJson[field.name] = field.checked;
    } else {
      rowJson[field.name] = field.value;
    }
  }
  return rowJson;
}

