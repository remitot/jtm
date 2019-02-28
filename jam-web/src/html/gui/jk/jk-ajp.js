/* @Override from table.js */
function getApiModUrl() {
  return "api/jk/mod/ajp";
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
  label.innerHTML = "Приложение"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-instance");
  label = document.createElement("label");
  label.innerHTML = "Инстанс Tomcat (по AJP порту)"; // NON-NLS
  cell.appendChild(label);
  
  row.appendChild(div);
  
  return row;
}

var tabindex0 = 1;

/* @Override from table.js */
function createRow(listItem) {
  var row = document.createElement("div");
  row.classList.add("row");
  row.setAttribute("item-id", listItem.id);
  
  var field;
  
  // active
  var cell = createCell(row, "column-active");
  cell.classList.add("column-left");
  cell.classList.add("cell-field");
  field = addCheckbox(cell, listItem.active, true);
  field.setAttribute("value-original", listItem.active);
  cell.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  if (!listItem.active) {
    row.classList.add("inactive");
  }
  
  var cellDelete = createCell(row, "column-delete");
  
  var div = document.createElement("div");
  div.classList.add("flexColumns");
  
  var field;
  
  cell = createCell(div, "column-application");
  cell.classList.add("cell-field");
  field = addField(cell, "application", listItem.application, null);
  field.setAttribute("value-original", listItem.application);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-instance");
  cell.classList.add("cell-field");
  var instanceValue = listItem.host + ":" + listItem.ajpPort;
  field = addField(cell, "instance", instanceValue, "tomcat-server:8080");
  field.setAttribute("value-original", "");
  field.tabIndex = tabindex0++;
  
  var deleteButton = addFieldDelete(cellDelete);
  deleteButton.tabIndex = tabindex0++;
  
  row.appendChild(div);
  
  return row;
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
  
  var field;
  
  // active
  var cell = createCell(row, "column-active");
  cell.classList.add("column-left");
  cell.classList.add("cell-field");
  field = addCheckbox(cell, true, false);
  
  var cellDelete = createCell(row, "column-delete");
  addFieldDelete(cellDelete);
  
  var flexColumns = document.createElement("div");
  flexColumns.classList.add("flexColumns");
  
  var field;
  
  cell = createCell(flexColumns, "column-application");
  cell.classList.add("cell-field");
  field = addField(cell, "application", null, "Application");
  field.tabIndex = tabindex0++;
 
  cell = createCell(flexColumns, "column-instance");
  cell.classList.add("cell-field");
  field = addField(cell, "instance", null, "tomcat-server:8009");
  field.tabIndex = tabindex0++;
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  row.appendChild(flexColumns);
  
  return row;
}


