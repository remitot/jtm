/* @Override from table.js */
function getApiListUrl() {
  return "api/port/list";
}

/* @Override from table.js */
function getApiModUrl() {
  // unsupported
}

/* @Override from table.js */
function createHeader() {
  var row = document.createElement("div");
  row.classList.add("header");
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  div.classList.add("column-left");
    
  cell = createCell(div, "column-type");
  label = document.createElement("label");
  label.innerHTML = "Тип"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-port");
  label = document.createElement("label");
  label.innerHTML = "Порт"; // NON-NLS
  cell.appendChild(label);
  
  row.appendChild(div);
  
  return row;
}

var tabindex0 = 1;

/* @Override from table.js */
function createRow(listItem) {
  row = document.createElement("div");
  row.classList.add("row");
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  div.classList.add("column-left");
  
  cell = createCell(div, "column-type");
  cell.classList.add("cell-field");
  field = addField(cell, "type", listItem.type, null);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-port");
  cell.classList.add("cell-field");
  field = addField(cell, "port", listItem.number, null);
  field.tabIndex = tabindex0++;
  
  row.appendChild(div);
  
  
  return row;
}

/* @Override from table.js */
function createRowCreate() {
  // unsupported
}

/* @Override from table.js */
function isEditable() {
  return false;
}

