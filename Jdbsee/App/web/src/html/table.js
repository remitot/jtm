/* Common code to draw and manipulate main page tables */

function getJsonListItems(jsonResponse) {
  console.error("getJsonListItems(jsonResponse) function must be overridden in the endpoint JS file (inherited from table.js)");
  return null;
}

function getApiListUrl() {
  console.error("getApiListUrl() function must be overridden in the endpoint JS file (inherited from table.js)");
  return null;
}

function getApiModUrl() {
  console.error("getApiModUrl() function must be overridden in the endpoint JS file (inherited from table.js)");
  return null;
}

function reload() {
  
  setButtonSaveEnabled(false);
  
  statusInfo("loading...");
  
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
      
        statusClear();
        
        jsonResponse = JSON.parse(this.responseText);
        jsonListItems = getJsonListItems(jsonResponse); 
        refillGrid(jsonListItems);
        
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
  xhttp.open("GET", getApiListUrl(), true);
  xhttp.send();
}

function refillGrid(jsonListItems) {
  table = document.getElementById("table");
  table.innerHTML = "";
  
  if (jsonListItems.length > 0) {
    table.appendChild(createHeader());
    
    for (var i = 0; i < jsonListItems.length; i++) {
      listItem = jsonListItems[i];
      
      row = createRow(listItem);
      
      table.appendChild(row);
    }
    
    checkListItemsModified();
  }
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
    checkListItemsModified();
  };
  checkbox.classList.add("deletable");

  wrapper = wrapCellContent(checkbox, "left");  
  
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
      input.parentElement.parentElement.parentElement.parentElement.classList.add("inactive");
      input.parentElement.title = "Inactive item";
    } else {
      //TODO resolve the relative path:
      input.parentElement.parentElement.parentElement.parentElement.classList.remove("inactive");
      input.parentElement.title = "Active item";
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
  
  wrapper = wrapCellContent(field, "center");
  
  cell.appendChild(wrapper);
  
  strike = document.createElement("div");
  strike.classList.add("strike");
  cell.appendChild(strike);
  
  return field;
}

/**
 * Prepares content to be displayed inside a table cell
 * by wrapping into a div (or multiple divs) 
 * to get the desired float inside the cell 
**/
function wrapCellContent(content, floatInside) {
  wrapper = document.createElement("div");
  
  if (floatInside === "center") {
  
    padLft = document.createElement("div");
    padLft.classList.add("cell-pad-left");
    wrapper.appendChild(padLft);
    
    padRgt = document.createElement("div");
    padRgt.classList.add("cell-pad-right");
    wrapper.appendChild(padRgt);
    
    padMid = document.createElement("div");
    padMid.classList.add("cell-pad-mid");
    wrapper.appendChild(padMid);
    
    padMid.appendChild(content);
    
  } else if (floatInside === "right") {
    wrapper.appendChild(content);
    wrapper.style.cssFloat = "right";
    
  } else if (floatInside === "left") {
    wrapper.appendChild(content);
    wrapper.style.cssFloat = "left";
    
  }
  
  return wrapper;
}

function addFieldDelete(cell) {

  button = document.createElement("input");
  button.type = "image";
  button.src = "img/delete.png";
  button.title = "Delete";
  button.onclick = function(event){onDeleteButtonClick(event.target);};
  
  wrapper = wrapCellContent(button, "right");  
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
  
  checkListItemsModified();
}

function checkListItemsModified() {
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
  row = button.parentElement.parentElement.parentElement;
  
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
  
  checkListItemsModified();
}

function onCreateButtonClick() {
  row = createRowCreate();
  document.getElementById("table").appendChild(row);
  
  row.querySelectorAll(".cell input[type='text']")[0].focus(); // focus on the first text input field
      
  checkListItemsModified();
}

function onSaveButtonClick() {
  
  rowsModified = getRowsModified();
  rowsDeleted = getRowsDeleted();
  rowsCreated = getRowsCreated();
  
  
  uiOnSaveBegin();
  
  modificationRequests = [];
  
  if (rowsModified.length > 0) {
    for (var i = 0; i < rowsModified.length; i++) {
      modificationRequests.push(
          {
            action: "update", 
            location: rowsModified[i].itemLocation, 
            data: rowsModified[i].itemData
          }
      );
    }
  }
  
  if (rowsDeleted.length > 0) {
    for (var i = 0; i < rowsDeleted.length; i++) {
      modificationRequests.push(
          {
            action: "delete", 
            location: rowsDeleted[i]
          }
      );
    }
  }
  
  if (rowsCreated.length > 0) {
    for (var i = 0; i < rowsCreated.length; i++) {
      modificationRequests.push(
          {
            action: "create", 
            data: rowsCreated[i]
          }
      );
    }
  }
  
  if (modificationRequests.length > 0) {
    xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        
      if (this.readyState == 4) {
        if (this.status == 200) {
          uiOnSaveEnd();
          jsonResponse = JSON.parse(this.responseText);
          
          jsonStatuses = jsonResponse.statuses;
          // if everything is OK, all statuses are 0
          sum = jsonStatuses.reduce(function(a, b) {return a + b;});
          if (sum > 0) {
            message = "<span class=\"span-bold\">Modifications saved, but some of them produced errors.</span>&emsp;The server might be restaring now...";
            statusError(message);
          } else {
            statusBar = document.getElementById("jdbcStatusBar");
            statusBar.className = "statusBar statusBar-success";
            statusBar.innerHTML = "<span class=\"span-bold\">Modifications successfully saved.</span>&emsp;The server might be restaring now...";
          }
          
          jsonListItems = getJsonListItems(jsonResponse); 
          refillGrid(jsonListItems);
          
          // disable whole grid
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
    xhttp.open("POST", getApiModUrl(), true);
    
    requestJson = {mod_requests: modificationRequests};
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

function adjustBottomShadow() {
  if (document.getElementById("table").getBoundingClientRect().bottom <= 
    document.getElementById("controlButtons").getBoundingClientRect().top) {
    document.getElementById("controlButtons").classList.remove("bottom-shadow");
  } else {
    document.getElementById("controlButtons").classList.add("bottom-shadow");
  }
}

window.onscroll = adjustBottomShadow;