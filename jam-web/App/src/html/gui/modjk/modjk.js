/* @Override from table.js */
function getApiListUrl() {
  return "api/modjk/list";
}

/* @Override from table.js */
function getApiModUrl() {
  return "api/modjk/mod";
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
  label.innerHTML = "Application";
  cell.appendChild(label);
  
  cell = createCell(div, "column-worker");
  label = document.createElement("label");
  label.innerHTML = "Apache worker";
  cell.appendChild(label);
  
  cell = createCell(div, "column-instance");
  label = document.createElement("label");
  label.innerHTML = "Direct tomcat instance";
  cell.appendChild(label);
  
  row.appendChild(div);
  
  return row;
}

/* @Override from table.js */
function onAfterRefillGrid() {
  loadWorkerOptions();
}

function loadWorkerOptions() {

  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
      
        jsonResponse = JSON.parse(this.responseText);
        jsonItemList = getJsonItemList(jsonResponse); 
        
        setWorkerOptions(jsonItemList);
        
      } else if (this.status == 401) {
        statusError("Требуется авторизация"); // NON-NLS
    
        raiseLoginForm(function() {
          hideLoginForm();
          table_reload();  
        });
      } else if (this.status == 403) {
        statusError("<span class=\"span-bold\">Доступ запрещён.</span>&emsp;<a href=\"#\" onclick=\"logout(table_reload);\">Выйти</a> чтобы сменить пользователя"); // NON-NLS // NON-NLS // NON-NLS
        
      } else {
        statusError("Сетевая ошибка " + this.status); // NON-NLS
      }
    }
  };
  xhttp.open("GET", "api/modjk/workers", true);
  xhttp.send();
}

function setWorkerOptions(workerNames) {
  var selects = document.querySelectorAll("#table .column-worker select");
  
  for (var i = 0; i < jsonItemList.length; i++) {
    var workerName = jsonItemList[i];
    
    for (var j = 0; j < selects.length; j++) {
      var select = selects[j];
      
      // create option
      var option = document.createElement("option");
      option.innerHTML = workerName;
      option.value = workerName;
    
      select.options.add(option);
      
      var selectedValue = select.getAttribute("selected-value");
      if (option.value == selectedValue) {
        option.selected = "selected";
      }
    }
  }
}

var tabindex0 = 1;

var rowIndex = 0;

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
  var workerInstanceBinding = rowIndex++;
  
  cell = createCell(div, "column-application");
  cell.classList.add("cell-field");
  field = addField(cell, "application", listItem.application, null);
  field.setAttribute("value-original", listItem.application);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-worker");
  cell.classList.add("cell-field");
  field = addFieldWorker(cell, listItem.worker);
  field.id = "workerField__" + workerInstanceBinding;
  field.setAttribute("worker-instance-binding", workerInstanceBinding);
  field.setAttribute("value-original", listItem.worker);//TODO
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-instance");
  cell.classList.add("cell-field");
  field = addFieldInstance(cell);
  field.id = "instanceField__" + workerInstanceBinding;
  field.setAttribute("worker-instance-binding", workerInstanceBinding);
  field.setAttribute("value-original", "");
  field.tabIndex = tabindex0++;
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  row.appendChild(div);
  
  return row;
}

function addFieldWorker(cell, value) {
  var select = document.createElement("select");
  select.classList.add("field-text");
  select.classList.add("inactivatible");
  select.setAttribute("selected-value", listItem.worker);
  
  select.addEventListener("change", function() {onWorkerSelectChanged(select);}) // .onchange not working in some browsers
  select.onfocus = function() {onWorkerSelectFocus(select);}
  
  if (isEditable()) {
    addStrike(cell);
  }
  
  var wrapper = wrapCellPad(select);
  cell.appendChild(wrapper);
  
  return select;
}

function addFieldInstance(cell) {
  var field = addField(cell, "instance", "", null);
  
  // one more listener (but not the only one!)
  field.addEventListener("input", function() {onInstanceFieldInput(field);});
  
  return field;
}

function onWorkerSelectChanged(select) {
  onFieldValueChanged(select, select.options[select.selectedIndex].value);
}

function onWorkerSelectFocus(select) {
  select.classList.remove("discarded");
  
  // clear instance field
  var instanceField = document.getElementById("instanceField__" + select.getAttribute("worker-instance-binding"));
  instanceField.value = "";
  onFieldValueChanged(instanceField, "");// trigger the event manually
}

function onInstanceFieldInput(field) {
  var workerField = document.getElementById("workerField__" + field.getAttribute("worker-instance-binding"));
  // discard or undiscard the select
  if (field.value === "") {
    workerField.classList.remove("discarded");
  } else {
    workerField.classList.add("discarded");
  }
}

function createWorkerSelectHTML() {
  var select = document.createElement("select");
  for (var i = 0; i < 10; i++) {
    var option = document.createElement("option");
    option.innerHTML = "Option " + i;
    select.appendChild(option);
  }
  return select;
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
 
  cell = createCell(flexColumns, "column-worker");
  cell.classList.add("cell-field");
  field = addField(cell, "worker", "", "tomcat-server:8080");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
  
  cell = createCell(flexColumns, "column-instance");
  cell.classList.add("cell-field");
  field = addField(cell, "instance", "", "tomcat-server:8080");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  row.appendChild(flexColumns);
  
  return row;
}


