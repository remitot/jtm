function reload() {
  
  setButtonSaveEnabled(false);
  
  statusInfo("loading...");
  
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
      
        statusClear();
        
        jsonResponse = JSON.parse(this.responseText);
        jsonConnections = jsonResponse.connections; 
        refillGrid(jsonConnections);
        
      } else if (this.status == 401) {
        statusError("Authorization required");
    
        raiseLoginForm(function() {
          hideLoginForm();
          reload();  
        });
      } else if (this.status == 403) {
        statusError("<span class=\"span-bold\">Access denied.</span>&emsp;<a href=\"#\" onclick=\"changeUser();\">Logout</a> to change the user");
        
      } else {
        statusError("Network error " + this.status);
      }
    }
  };
  xhttp.open("GET", "api/jdbc/list", true);
  xhttp.send();
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
    
    checkConnectionsModified();
  }
}

function createHeader() {
  var row = document.createElement("div");
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

var tabindex0 = 1;

function createRow(connection) {
  row = document.createElement("div");
  row.classList.add("row");
  row.setAttribute("connection-location", connection.location);
  
  // active
  cell = createCell(row, "column-active");
  cell.classList.add("cell-field");
  addCheckbox(cell, connection.active, true);
  cell.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  if (!connection.active) {
    row.classList.add("inactive");
  }
  
  
  cellDelete = createCell(row, "column-delete");
  addFieldDelete(cellDelete);
  
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  
  cell = createCell(div, "column-name");
  cell.classList.add("cell-field");
  field = addField(cell, "name", connection.name, null);
  field.setAttribute("value0", connection.name);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-server");
  cell.classList.add("cell-field");
  field = addField(cell, "server", connection.server, null);
  field.setAttribute("value0", connection.server);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-db");
  cell.classList.add("cell-field");
  field = addField(cell, "db", connection.db, null);
  field.setAttribute("value0", connection.db);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-user");
  cell.classList.add("cell-field");
  field = addField(cell, "user", connection.user, null);
  field.setAttribute("value0", connection.user);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-password");
  cell.classList.add("cell-field");
  field = addField(cell, "password", connection.password, null);
  field.setAttribute("value0", connection.password);
  field.tabIndex = tabindex0++;
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  row.appendChild(div);
  
  
  return row;
}

function createRowCreate() {

  // add header row if the table is empty
  var table = document.getElementById("connections");
  if (table.getElementsByClassName("header").length == 0) {
    table.appendChild(createHeader());
  }
  

  row = document.createElement("div");
  row.classList.add("row");
  row.classList.add("created");
  
  // active
  cell = createCell(row, "column-active");
  cell.classList.add("cell-field");
  addCheckbox(cell, true, false);
  cell.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  
  cellDelete = createCell(row, "column-delete");
  addFieldDelete(cellDelete);
  
  
  flexColumns = document.createElement("div");
  flexColumns.classList.add("flexColumns");
  
  cell = createCell(flexColumns, "column-name");
  cell.classList.add("cell-field");
  field = addField(cell, "name", "", "jdbc/MyDataSource");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
 
  cell = createCell(flexColumns, "column-server");
  cell.classList.add("cell-field");
  field = addField(cell, "server", "", "mydbserver.com:1521");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
  
  cell = createCell(flexColumns, "column-db");
  cell.classList.add("cell-field");
  field = addField(cell, "db", "", "MYDATABASE");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
  
  cell = createCell(flexColumns, "column-user");
  cell.classList.add("cell-field");
  field = addField(cell, "user", "", "me");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
  
  cell = createCell(flexColumns, "column-password");
  cell.classList.add("cell-field");
  field = addField(cell, "password", "", "mysecret");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  row.appendChild(flexColumns);
  
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
    checkConnectionsModified();
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
  input.setAttribute("value0", active);
  input.onfocus = function(event){
    var input = event.target;
    input.parentElement.getElementsByClassName("checkmark")[0].classList.add("hovered");
  }
  input.addEventListener("focusout", function(event) { // .onfocusout not working in some browsers
    var input = event.target;
    input.parentElement.getElementsByClassName("checkmark")[0].classList.remove("hovered");
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

function onCheckboxInput(input) {
  // this will be SPAN, then INPUT on a single click
  if (input.tagName.toLowerCase() == "input") {
    if (input.checked && input.getAttribute("value0") == "true" 
        || !input.checked && input.getAttribute("value0") == "false") {
      input.parentElement.classList.remove("modified");
    } else {
      input.parentElement.classList.add("modified");
    }
    
    if (!input.checked) {
      //TODO resolve the relative path:
      input.parentElement.parentElement.parentElement.parentElement.parentElement.classList.add("inactive");
      input.parentElement.title = "Inactive connection";
    } else {
      //TODO resolve the relative path:
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

  button = document.createElement("input");
  button.type = "image";
  button.src = "img/delete.png";
  button.title = "Delete";
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
  
  checkConnectionsModified();
}

function checkConnectionsModified() {
  totalModifications = 
      document.getElementsByClassName("modified").length
      - document.querySelectorAll(".row.created.deleted .modified").length
      + document.getElementsByClassName("row deleted").length
      - document.getElementsByClassName("row created deleted").length;
  
  setButtonSaveEnabled(totalModifications > 0);
  
  // graphics:
  adjustBottomShadow();
}

function setButtonSaveEnabled(enabled) {
  var buttonSave = document.getElementById("buttonSave");
  if (enabled) {
    buttonSave.disabled = false;  
    buttonSave.title = "Save all modifications (orange)";
  } else {
    buttonSave.disabled = true;
    buttonSave.title = "No modifications performed";
  }
}

function onDeleteButtonClick(button) {
  //TODO resolve the relative path:
  row = button.parentElement.parentElement.parentElement.parentElement;
  
  rowInputs = row.querySelectorAll("input.deletable");
  
  if (!row.classList.contains("deleted")) {
    row.classList.add("deleted");
    
    // button changes image
    button.src = "img/undelete.png";
    button.title = "Do not delete";
    
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
    button.title = "Delete";
    
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
  
  checkConnectionsModified();
}

function onCreateButtonClick() {
  row = createRowCreate();
  document.getElementById("connections").appendChild(row);
  
  row.querySelectorAll(".cell.column-name input")[0].focus();
      
  checkConnectionsModified();
}

function onSaveButtonClick() {
  
  rowsModified = getRowsModified();
  rowsDeleted = getRowsDeleted();
  rowsCreated = getRowsCreated();
  
  
  uiOnSaveBegin();
  
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
            location: rowsDeleted[i]
          }
      );
    }
  }
  
  if (rowsCreated.length > 0) {
    for (var i = 0; i < rowsCreated.length; i++) {
      connectionModificationRequests.push(
          {
            action: "create", 
            data: rowsCreated[i]
          }
      );
    }
  }
  
  if (connectionModificationRequests.length > 0) {
    xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        
      if (this.readyState == 4) {
        if (this.status == 200) {
          uiOnSaveEnd();
          jsonResponse = JSON.parse(this.responseText);
          
          jsonModStates = jsonResponse.mod_states;
          // if everything is OK, all statuses are 0
          sum = jsonModStates.reduce(function(a, b) {return a + b;});
          if (sum > 0) {
            message = "<span class=\"span-bold\">Modifications saved, but some of them produced errors.</span>&emsp;The server might be restaring now...";
            statusError(message);
          } else {
            statusBar = document.getElementById("jdbcStatusBar");
            statusBar.className = "statusBar statusBar-success";
            statusBar.innerHTML = "<span class=\"span-bold\">Modifications successfully saved.</span>&emsp;The server might be restaring now...";
          }
          
          jsonConnections = jsonResponse.connections; 
          refillGrid(jsonConnections);
          
          // disable whole grid
          table = document.getElementById("connections");
          // remove column-delete contents
          columnDeletes = table.getElementsByClassName("column-delete");
          for (var i = 0; i < columnDeletes.length; i++) {
            columnDelete = columnDeletes[i];
            columnDelete.innerHTML = "";
          }
          
          inputs = table.getElementsByTagName("input");
          for (var i = 0; i < inputs.length; i++) {
            input = inputs[i];
            input.disabled = true;
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
          
          document.getElementById("controlButtons").style.display = "none";
          
        } else if (this.status == 401) {
          statusError("Authorization required");
      
          raiseLoginForm(function() {
            hideLoginForm();
            reload();  
          });
          
        } else if (this.status == 403) {
          statusError("<span class=\"span-bold\">Access denied.</span>&emsp;<a href=\"#\" onclick=\"changeUser();\">Logout</a> to change the user");
          
        } else {
          statusError("Network error " + this.status);
        }
      }
    };
    xhttp.open("POST", "api/jdbc/mod", true);
    
    requestJson = {mod_requests: connectionModificationRequests};
    xhttp.send(JSON.stringify(requestJson));
    
  } else {
    // TODO report nothing to save
    uiOnSaveEnd();
  }
}

function changeUser() {
  logout(function() {
    reload();
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
        statusError("Network error " + this.status);
      }
    }
  };
  xhttp.open("POST", "logout", true);
  xhttp.send();
}

function statusClear() {
  statusBar = document.getElementById("jdbcStatusBar");
  statusBar.className = "statusBar statusBar-none";
  statusBar.innerHTML = "";
}

function statusInfo(message) {
  statusBar = document.getElementById("jdbcStatusBar"); 
  statusBar.className = "statusBar statusBar-info";
  statusBar.innerHTML = message;
}

function statusError(message) {
  statusBar = document.getElementById("jdbcStatusBar"); 
  statusBar.className = "statusBar statusBar-error";
  statusBar.innerHTML = message;
}

function uiOnSaveEnd() {
  statusClear();
}

function uiOnSaveBegin() {
  statusInfo("saving...");
}

function getRowsModified() {
  var rows = document.querySelectorAll("#connections div.row");
  rowsModifiedJson = [];
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    if (!row.classList.contains("deleted") && !row.classList.contains("created") && row.getElementsByClassName("modified").length > 0) {
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

function adjustBottomShadow() {
  if (document.getElementById("connections").getBoundingClientRect().bottom <= 
    document.getElementById("controlButtons").getBoundingClientRect().top) {
    document.getElementById("controlButtons").classList.remove("bottom-shadow");
  } else {
    document.getElementById("controlButtons").classList.add("bottom-shadow");
  }
}

window.onscroll = adjustBottomShadow;
