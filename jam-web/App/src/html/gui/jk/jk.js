/* @Override from table.js */
function getApiListUrl() {
  return "api/jk/list";
}

/* @Override from table.js */
function getApiModUrl() {
  return "api/jk/mod";
}

/* @Override from table.js */
function createHeader() {
  var row = document.createElement("div");
  row.classList.add("header");
  
  // active
  var cell = createCell(row, "column-active");// empty cell
  cell.classList.add("column-left");
  
  cell = createCell(row, "column-delete");// empty cell
  
  var div = document.createElement("div");
  div.classList.add("flexColumns");
  
  var label;
  
  cell = createCell(div, "column-application");
  label = document.createElement("label");
  label.innerHTML = "Application"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-host");
  label = document.createElement("label");
  label.innerHTML = "Сервер Tomcat"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-ajp_port");
  label = document.createElement("label");
  label.innerHTML = "AJP порт"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-http_port");
  label = document.createElement("label");
  label.innerHTML = "HTTP порт"; // NON-NLS
  cell.appendChild(label);
  
  row.appendChild(div);
  
  return row;
}

var rowIndex = 0;
var tabindex0 = 1;

/* @Override from table.js */
function createRow(listItem) {
  var row = document.createElement("div");
  row.classList.add("row");
  
  // active
  var cell = createCell(row, "column-active");
  cell.classList.add("column-left");
  cell.classList.add("cell-field");
  addCheckbox(cell, listItem.active, true);
  cell.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  if (!listItem.active) {
    row.classList.add("inactive");
  }
  
  
  var cellDelete = createCell(row, "column-delete");
  addFieldDelete(cellDelete);
  
  
  var div = document.createElement("div");
  div.classList.add("flexColumns");
  
  var field;
  
  cell = createCell(div, "column-application");
  cell.classList.add("cell-field");
  field = addField(cell, "application", listItem.application, null);
  field.setAttribute("value-original", listItem.application);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-host");
  cell.classList.add("cell-field");
  field = addField(cell, "host", listItem.host, null);
  field.setAttribute("value-original", listItem.host);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-ajp_port");
  cell.classList.add("cell-field");
  field = addFieldAjpPort(cell, rowIndex, listItem.ajpPort, null);
  field.setAttribute("value-original", listItem.ajpPort);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-http_port");
  cell.classList.add("cell-field");
  field = addFieldHttpPort(cell, rowIndex, listItem.getHttpPortLink, null);
  field.setAttribute("value-original", "");
  field.tabIndex = tabindex0++;
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  row.appendChild(div);
  
  rowIndex++;
  
  return row;
}

function addFieldAjpPort(cell, rowIndex, value, placeholder) {
  field = addField(cell, "ajp_port", value, placeholder);
  field.id = "ajp_portInputField-" + rowIndex;
  field.addEventListener("input", function(){
    var http_portInputField = document.getElementById("http_portInputField-" + rowIndex);
    http_portInputField.setAttribute("value-original", "");
    http_portInputField.value = "";
  });
  return field;
}

function addFieldHttpPort(cell, rowIndex, getHttpPortLink, placeholder) {
  var div = document.createElement("div");

  field = createField("http_port", "", placeholder);
  field.id = "http_portInputField-" + rowIndex;
  field.addEventListener("input", function(){
    var ajp_portInputField = document.getElementById("ajp_portInputField-" + rowIndex);
    ajp_portInputField.value = "";
  });
  div.appendChild(field);
  
  if (getHttpPortLink) {
    var button = document.createElement("button");
    setGetHttpPortButtonState(button, 0);
    button.onclick = function(){getHttpPortButtonClick(button, getHttpPortLink);}
    div.appendChild(button);
  }
  
  wrapper = wrapCellPad(div);
  cell.appendChild(wrapper);

  if (isEditable()) {
    addStrike(cell);
  }
    
  return field;
}

/**
 * 0 -- initial state, 1 -- loading state
 */
function setGetHttpPortButtonState(button, state) {
  if (state == 0) {
    button.disabled = false;
    button.innerHTML = "Запросить"; // NON-NLS
    button.title = "Сделать запрос HTTP порта"; 
  } else if (state == 1) {
    button.disabled = true;
    button.innerHTML = "Загрузка..."; // NON-NLS
    button.title = "Запрашивается HTTP порт"; // NON-NLS
  } else {
    button.disabled = true;
    button.innerHTML = "Ошибка"; // NON-NLS
    button.title = "При запросе HTTP порта возникла ошибка"; // NON-NLS
  }
}

function getHttpPortButtonClick(button, getHttpPortLink) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
        setGetHttpPortButtonState(button, 0);
        
        //TODO resolve the relative path:
        var field = button.parentElement.firstChild;
        var value = this.responseText;
        field.value = value;
        field.setAttribute("value-original", value);
        onFieldInput(field);// trigger initial event
      } else {
        setGetHttpPortButtonState(button, 2);
      }
    }
  };
  xhttp.open("GET", getHttpPortLink, true);
  xhttp.send();
  
  setGetHttpPortButtonState(button, 1);
}

/* @Override from table.js */
function createRowCreate() {

  // add header row if the table is empty
  var table = document.getElementById("table");
  if (table.getElementsByClassName("header").length == 0) {
    table.appendChild(createHeader());
  }
  

  var row = document.createElement("div");
  row.classList.add("row");
  row.classList.add("created");
  
  // active
  var cell = createCell(row, "column-active");
  cell.classList.add("column-left");
  cell.classList.add("cell-field");
  addCheckbox(cell, true, false);
  cell.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  
  var cellDelete = createCell(row, "column-delete");
  addFieldDelete(cellDelete);
  
  
  var flexColumns = document.createElement("div");
  flexColumns.classList.add("flexColumns");
  
  var field;
  
  cell = createCell(flexColumns, "column-application");
  cell.classList.add("cell-field");
  field = addField(cell, "application", "", "Application");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
 
  cell = createCell(flexColumns, "column-host");
  cell.classList.add("cell-field");
  field = addField(cell, "host", "", "tomcat-server.com");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
  
  cell = createCell(flexColumns, "column-ajp_port");
  cell.classList.add("cell-field");
  field = addFieldAjpPort(cell, rowIndex, "", "8009");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
  
  cell = createCell(flexColumns, "column-http_port");
  cell.classList.add("cell-field");
  field = addFieldHttpPort(cell, rowIndex, null, "8080");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
  
  cell = createCell(flexColumns, "column-get_http_port");
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  row.appendChild(flexColumns);
  
  rowIndex++;
  
  return row;
}


