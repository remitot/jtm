function reload() {
  
  uiGridReloadBegin();
  
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
      if (this.readyState == 4 && this.status == 200) {
        uiGridReloadEnd();
        jsonResponse = JSON.parse(this.responseText);
        jsonConnections = jsonResponse.connections; 
        refillGrid(jsonConnections);
      }
  };
  xhttp.open("GET", "jdbc/api/list", true);
  xhttp.send();
}

function uiGridReloadBegin() {
  setButtonSaveEnabled(false);
  
  statusBar = document.getElementById("statusBar"); 
  statusBar.className = "statusBar-info";
  statusBar.innerHTML = "loading...";
}

function uiGridReloadEnd() {
  document.getElementById("statusBar").className = "statusBar-none";
}

function refillGrid(jsonConnections) {
  table = document.getElementById("connections");
  table.innerHTML = "";
  
  if (jsonConnections.length > 0) {
    table.appendChild(createHeader());
    
    for (var i = 0; i < jsonConnections.length; i++) {
      connection = jsonConnections[i];
      
      row = createRow(connection);
      
      table.appendChild(row);
    }
    
    checkModifications();
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
  addCheckboxCa(cell, connection.active, true);
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

  // add header row if the table is empty
  table = document.getElementById("connections");
  if (table.querySelectorAll(".row.header").length == 0) {
    table.appendChild(createHeader());
  }
  

  row = document.createElement("div");
  row.classList.add("row");
  row.classList.add("created");
  
  // active
  cell = createCell(row, "column-active");
  addCheckboxCa(cell, true, false);
  
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

function setCheckboxCaEnabled(checkboxCa, enabled) {
  if (enabled) {
    checkboxCa.classList.remove("checkbox-ca-disabled");
    checkboxCa.querySelectorAll("input")[0].disabled = false;
  } else {
    checkboxCa.classList.add("checkbox-ca-disabled");
    checkboxCa.querySelectorAll("input")[0].disabled = true;
  }
}

function addCheckboxCa(cell, active, enabled) {
  checkboxCa = createCheckboxCa(active);
  
  setCheckboxCaEnabled(checkboxCa, enabled);
  
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
    if (input.checked && input.getAttribute("value0") == "true" 
        || !input.checked && input.getAttribute("value0") == "false") {
      input.parentElement.classList.remove("modified");
    } else {
      input.parentElement.classList.add("modified");
    }
    
    if (!input.checked) {
      //TODO resolve the relative path!
      input.parentElement.parentElement.parentElement.parentElement.parentElement.classList.add("inactive");
      input.parentElement.title = "Inactive connection";
    } else {
      //TODO resolve the relative path!
      input.parentElement.parentElement.parentElement.parentElement.parentElement.classList.remove("inactive");
      input.parentElement.title = "Active connection";
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
    buttonSave.title = "Save all modifications (orange)";
  } else {
    buttonSave.disabled = true;
    buttonSave.title = "No modifications performed";
  }
}

function onDeleteButtonClick(button) {
  //TODO resolve the relative path!
  row = button.parentElement.parentElement.parentElement.parentElement.parentElement;
  row.classList.add("deleted");
  
  rowInputs = row.querySelectorAll("input.deletable");
  for (var i = 0; i < rowInputs.length; i++) {
    rowInputs[i].disabled = true;
  }
  
  if (!row.classList.contains("created")) {// no change checkboxes for created rows
    checkboxCas = row.querySelectorAll(".checkbox-ca.deletable");
    for (var i = 0; i < checkboxCas.length; i++) {
      setCheckboxCaEnabled(checkboxCas[i], false);
    }
  }
  
  checkModifications();
}

function onUndeleteButtonClick(button) {
  //TODO resolve the relative path!
  row = button.parentElement.parentElement.parentElement.parentElement.parentElement;
  row.classList.remove("deleted");
  
  rowInputs = row.querySelectorAll("input.deletable");
  for (var i = 0; i < rowInputs.length; i++) {
    rowInputs[i].disabled = false;
  }
  
  if (!row.classList.contains("created")) {// no change checkboxes for created rows
    checkboxCas = row.querySelectorAll(".checkbox-ca.deletable");
    for (var i = 0; i < checkboxCas.length; i++) {
      setCheckboxCaEnabled(checkboxCas[i], true);
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
          jsonResponse = JSON.parse(this.responseText);
          
          jsonModStates = jsonResponse.mod_states;
          // if everything is OK, all statuses are 0
          sum = jsonModStates.reduce(function(a, b) {return a + b;});
          if (sum > 0) {
            statusBar = document.getElementById("statusBar");
            statusBar.className = "statusBar-error";
            statusBar.innerHTML = "<h4>Modifications saved, but some of them produced errors.</h4> The server might be restaring now...";
          } else {
            statusBar = document.getElementById("statusBar");
            statusBar.className = "statusBar-success";
            statusBar.innerHTML = "<h4>Modifications successfully saved.</h4> The server might be restaring now...";
          }
          
          jsonConnections = jsonResponse.connections; 
          refillGrid(jsonConnections);
          
          // disable whole grid
          table = document.getElementById("connections");
          // remove column-delete contents
          columnDeletes = table.querySelectorAll(".column-delete");
          for (var i = 0; i < columnDeletes.length; i++) {
            columnDelete = columnDeletes[i];
            columnDelete.innerHTML = "";
          }
          
          inputs = table.querySelectorAll("input");
          for (var i = 0; i < inputs.length; i++) {
            input = inputs[i];
            input.disabled = true;
          }
          checkboxCas = table.querySelectorAll(".checkbox-ca");
          for (var i = 0; i < checkboxCas.length; i++) {
            checkboxCa = checkboxCas[i];
            setCheckboxCaEnabled(checkboxCa, false);
          }
          document.getElementById("controlButtons").style.display = "none";
        }
    };
    xhttp.open("POST", "jdbc/api/mod", true);
    
    requestJson = {mod_requests: connectionModificationRequests};
    xhttp.send(JSON.stringify(requestJson));
    
  } else {
    // TODO report nothing to save
    uiSaveEnd();
  }
}

function uiSaveEnd() {
  document.getElementById("statusBar").className = "statusBar-none";
}

function uiSaveBegin() {
  statusBar = document.getElementById("statusBar");
  statusBar.className = "statusBar-info";
  statusBar.innerHTML = "saving...";
}

function getRowsModified() {
  var rows = document.querySelectorAll("#connections div.row");
  rowsModifiedJson = [];
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    if (!row.classList.contains("deleted") && !row.classList.contains("created") && row.querySelectorAll(".modified") != null) {
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

