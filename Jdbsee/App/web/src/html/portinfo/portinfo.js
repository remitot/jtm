function getJsonListItems(jsonResponse) {
  return jsonResponse // the response itself is a JSON array of port infos
}

function getApiListUrl() {
  return "api/portinfo/list";
}

function getApiModUrl() {
  // unsupported
}

function createHeader() {
  var row = document.createElement("div");
  row.classList.add("header");
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  
  cell = createCell(div, "column-type");
  cell.classList.add("column-left");
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

function createRow(listItem) {
  row = document.createElement("div");
  row.classList.add("row");
  row.setAttribute("item-location", listItem.location);
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  
  cell = createCell(div, "column-type");
  cell.classList.add("column-left");
  cell.classList.add("cell-field");
  field = addField(cell, "type", listItem.type, null);
  field.setAttribute("value0", listItem.type);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-port");
  cell.classList.add("cell-field");
  field = addField(cell, "port", listItem.port, null);
  field.setAttribute("value0", listItem.port);
  field.tabIndex = tabindex0++;
  
  row.appendChild(div);
  
  
  return row;
}

function createRowCreate() {
  // unsupported
}


