/* @Override from table.js */
function getApiListUrl() {
  return "api/log/list?localTzOffset=" + (-new Date().getTimezoneOffset());
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
  label.innerHTML = "Файл"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-lastmod");
  label = document.createElement("label");
  label.innerHTML = "Последняя запись"; // NON-NLS
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
  cellValue = createLastModifiedHTML(listItem);
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
  cellValue = "<a href=\"log-monitor?filename=" + listItem.name + "\""
      + " target=\"_blank\" title=\"Открыть в читалке\">Отслеживать</a>"; // NON-NLS // NON-NLS
  field = addField(cell, "monitor", cellValue, null);
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  row.appendChild(div);
  
  
  return row;
}

function createLastModifiedHTML(listItem) {
  if (listItem.local != null) {
    var html = listItem.local.lastModifiedDate + " " + listItem.local.lastModifiedTime;
    
    var lastModifiedAgoVerb = listItem.local.lastModifiedAgoVerb;
    if (lastModifiedAgoVerb) {
      if (lastModifiedAgoVerb == 1) {
        html += ", <b>только что</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 2) {
        html += ", <b>минуту назад</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 3) {
        html += ", <b>две минуты назад</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 4) {
        html += ", <b>три минуты назад</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 5) {
        html += ", <b>пять минут назад</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 6) {
        html += ", <b>10 минут назад</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 7) {
        html += ", <b>полчаса назад</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 8) {
        html += ", <b>час назад</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 9) {
        html += ", <b>два часа назад</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 10) {
        html += ", <b>три часа назад</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 11) {
        html += ", <b>сегодня</b>"; // NON-NLS;
      } else if (lastModifiedAgoVerb == 12) {
        html += ", <b>вчера</b>"; // NON-NLS;
      }
    }
    return html;
  } else {
    var date = new Date(listItem.lastModified);
    var html = date.toDateString() + " " + date.toTimeString();
    return html;
  }
}

/* @Override from table.js */
function createRowCreate() {
  // unsupported
}

/* @Override from table.js */
function isEditable() {
  return false;
}


