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
  
  if (jsonConnections.length > 0) {
    fillGrid(jsonConnections);
  } else {
    document.getElementById("abc").innerHTML = "no data";
  }
}

function fillGrid(jsonConnections) {
  var table = document.getElementById("connections");
  table.innerHTML = "";
  
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
  
  
  cell = createCell(row, "column-name");
  addField(cell, "name", connection.name, null);
  
  
  cell = createCell(row, "column-server");
  addField(cell, "server", connection.server, null);
  
  
  cell = createCell(row, "column-db");
  addField(cell, "db", connection.db, null);
  
  
  cell = createCell(row, "column-user");
  addField(cell, "user", connection.user, null);
  
  
  cell = createCell(row, "column-password");
  addField(cell, "password", connection.password, null);
  
  
  cell = createCell(row, "column-delete");
  addFieldDelete(cell);
  
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
  
  cell = createCell(row, "column-name");
  addField(cell, "name", "", "jdbc/MyDataSource");
 
  
  cell = createCell(row, "column-server");
  addField(cell, "server", "", "mydbserver.com:1521");
  
  
  cell = createCell(row, "column-db");
  addField(cell, "db", "", "MYDATABASE");
  
  
  cell = createCell(row, "column-user");
  addField(cell, "user", "", "me");
  
  
  cell = createCell(row, "column-password");
  addField(cell, "password", "", "mysecret");
  
  
  cell = createCell(row, "column-delete");
  addFieldDelete(cell);
  
  return row;
}

function addFieldActive(cell, active) {
  wrapper = document.createElement("div");
  wrapper.classList.add("fieldWrapper");
  
  checkboxActive = createCheckboxActive(active);
  checkboxActive.onclick = function(){onCheckboxActiveInput(event)};
  checkboxActive.classList.add("deletable");
  wrapper.appendChild(checkboxActive);
  
  cell.appendChild(wrapper);
  
  strike = document.createElement("div");
  strike.classList.add("strike");
  cell.appendChild(strike);
}

function addField(cell, name, value, placeholder) {
  wrapper = document.createElement("div");
  wrapper.classList.add("fieldWrapper");
  
  field = document.createElement("input");
  field.type = "text";
  field.name = name;
  field.value = value;
  if (placeholder != null) {
    field.placeholder = placeholder;
  }
  field.setAttribute("value0", value);
  field.oninput = function(){onFieldInput(event)};
  field.classList.add("field-text");
  field.classList.add("inactivatible");
  field.classList.add("deletable");
  wrapper.appendChild(field);
  
  cell.appendChild(wrapper);
  
  strike = document.createElement("div");
  strike.classList.add("strike");
  cell.appendChild(strike);
}

function addFieldDelete(cell) {
  wrapper = document.createElement("div");
  wrapper.classList.add("fieldWrapper");
  
  field = document.createElement("button");
  field.innerHTML = "Delete";
  field.classList.add("button-delete");
  field.onclick = function() {onDeleteButtonClick(event);};
  wrapper.appendChild(field);
  
  field = document.createElement("button");
  field.innerHTML = "Restore";
  field.classList.add("button-undelete");
  field.onclick = function() {onUndeleteButtonClick(event);};
  wrapper.appendChild(field);
  
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
  //TODO make abstract
  row = button.parentElement.parentElement.parentElement;
  row.classList.add("deleted");
  
  rowInputs = row.querySelectorAll("input");
  for (var i = 0; i < rowInputs.length; i++) {
    if (!row.classList.contains("created")) {// no affect for new rows
      rowInputs[i].disabled = true;
    }
  }
}

function onUndeleteButtonClick(event) {
  button = event.target;
  //TODO make abstract
  row = button.parentElement.parentElement.parentElement;
  row.classList.remove("deleted");
  
  rowInputs = row.querySelectorAll("input");
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

