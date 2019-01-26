/* @Override from table.js */
function getApiListUrl() {
  return "api/log/list";
}

/* @Override from table.js */
function getApiModUrl() {
  return "api/jdbc/mod";
}

/* @Override from table.js */
function createHeader() {
  var row = document.createElement("div");
  row.classList.add("header");
  
  cell = createCell(row, "column-delete");// empty cell
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  div.classList.add("column-left");
  
  cell = createCell(div, "column-name");
  label = document.createElement("label");
  label.innerHTML = "Имя файла"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-lastmod");
  label = document.createElement("label");
  label.innerHTML = "Последнее изменение"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-download");
  
  cell = createCell(div, "column-open");
  
  cell = createCell(div, "column-monitor");
  
  row.appendChild(div);
  
  return row;
}

var tabindex0 = 1;

/* @Override from table.js */
function createRow(listItem) {
  row = document.createElement("div");
  row.classList.add("row");
  
  cellDelete = createCell(row, "column-delete");
  addFieldDelete(cellDelete);
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  div.classList.add("column-left");
  
  cell = createCell(div, "column-name");
  cell.classList.add("cell-field");
  field = addField(cell, "name", listItem.name, null);
  
  cell = createCell(div, "column-lastmod");
  cell.classList.add("cell-field");
  cellValue = listItem.lastModifiedDate + " <b>" + listItem.lastModifiedTime + "</b>";
  field = addField(cell, "lastmod", cellValue, null);
  
  cell = createCell(div, "column-download");
  cell.classList.add("cell-field");
  cellValue = "<a href=\"api/log?filename=" + listItem.name 
      + "\" title=\"Скачать на компьютер\">Сохранить</a>"; // NON-NLS // NON-NLS
  field = addField(cell, "download", cellValue, null);
  
  cell = createCell(div, "column-open");
  cell.classList.add("cell-field");
  cellValue = "<a href=\"api/log?filename=" + listItem.name + "&inline\""
      + " target=\"_blank\" title=\"Открыть в новой вкладке браузера\">Посмотреть</a>"; // NON-NLS // NON-NLS
  field = addField(cell, "open", cellValue, null);
  
  cell = createCell(div, "column-monitor");
  cell.classList.add("cell-monitor");
  cellValue = "<a href=\"log-monitor-entry?filename=" + listItem.name + "\""
      + " target=\"_blank\" title=\"Открыть в читалке\">Отслеживать</a>"; // NON-NLS // NON-NLS
  field = addField(cell, "monitor", cellValue, null);
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
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


