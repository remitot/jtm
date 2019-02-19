/* @Override from table.js */
function getApiListUrl() {
  return "api/jdbc/list";
}

/* @Override from table.js */
function getApiModUrl() {
  return "api/jdbc/mod";
}

/* @Override from table.js */
function onTableModSuccess(jsonItemList) {
  uiOnTableModSuccess();
  recreateTable(jsonItemList, false);
}

/* @Override from table.js */
function uiOnTableModSuccess() {
  var message = "<span class=\"span-bold\">Все изменения сохранены.</span>&emsp;Сейчас сервер может перезагрузиться." // NON-NLS // NON-NLS
    + "&emsp;<a href=\"\" onclick=\"table_reload();\">Обновить таблицу</a>"; // NON-NLS
  statusSuccess(message);
}

/* @Override from table.js */
function clientValidate(fieldName, fieldValue) {
  if (fieldName === "name") {
    if (!fieldValue) {
      return false;
    }
  }
  if (fieldName === "server") { 
    if (!fieldValue) {
      return false;
    }
  }
  if (fieldName === "db") { 
    if (!fieldValue) {
      return false;
    }
  }
  if (fieldName === "user") { 
    if (!fieldValue) {
      return false;
    }
  }
  if (fieldName === "password") { 
    if (!fieldValue) {
      return false;
    }
  }
  return true;
}

/* @Override from table.js */
function getServerInvalidFieldMessage(fieldName, errorCode, errorMessage) {
  if (fieldName == "name") {
    if (errorCode) {
      if (errorMessage) {
         console.error(errorMessage + " " + errorCode);
      }
      if (errorCode == "DUPLICATE_NAME") {
        return "Такое название уже есть";// NON-NLS
      } else if (errorCode == "DUPLICATE_GLOBAL") {
        return "Такое название уже есть среди Context/ResourceLink.global " 
            + "или Server/GlobalNamingResources/Resource.name";// NON-NLS
      }
    }
  } 
}

/* @Override from table.js */
function createHeader() {
  var row = document.createElement("div");
  row.classList.add("header");
  // active
  cell = createCell(row, "column-active");// empty cell
  cell.classList.add("column-left");
  
  cell = createCell(row, "column-delete");// empty cell
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  
  cell = createCell(div, "column-name");
  label = document.createElement("label");
  label.innerHTML = "Название"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-server");
  label = document.createElement("label");
  label.innerHTML = "Сервер базы данных"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-db");
  label = document.createElement("label");
  label.innerHTML = "Имя базы"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-user");
  label = document.createElement("label");
  label.innerHTML = "Пользователь базы"; // NON-NLS
  cell.appendChild(label);
  
  cell = createCell(div, "column-password");
  label = document.createElement("label");
  label.innerHTML = "Пароль к базе"; // NON-NLS
  cell.appendChild(label);
  
  row.appendChild(div);
  
  return row;
}

var tabindex0 = 1;

/* @Override from table.js */
function createRow(listItem) {
  var dataModifiable;
  if (!listItem.dataModifiable) {
    dataModifiable = false;
  } else {
    dataModifiable = true;
  }

  row = document.createElement("div");
  row.classList.add("row");
  row.setAttribute("item-location", listItem.location);
  
  var field;
  
  // active
  cell = createCell(row, "column-active");
  cell.classList.add("column-left");
  cell.classList.add("cell-field");
  field = addCheckbox(cell, listItem.active, true);
  if (!dataModifiable) {
    field.classList.add("readonly");
    setCheckboxEnabled(field, false);
  } else {
    field.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  }
  
  if (!listItem.active) {
    row.classList.add("inactive");
  }
  
  
  cellDelete = createCell(row, "column-delete");
  if (dataModifiable) {
    addFieldDelete(cellDelete);
  }
  
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  
  cell = createCell(div, "column-name");
  cell.classList.add("cell-field");
  field = addField(cell, "name", listItem.name, null);
  field.setAttribute("value-original", listItem.name);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-server");
  cell.classList.add("cell-field");
  field = addField(cell, "server", listItem.server, null);
  field.setAttribute("value-original", listItem.server);
  if (!dataModifiable) {
    setFieldReadonly(field);
  } else {
    field.tabIndex = tabindex0++;
  }
  
  cell = createCell(div, "column-db");
  cell.classList.add("cell-field");
  field = addField(cell, "db", listItem.db, null);
  field.setAttribute("value-original", listItem.db);
  if (!dataModifiable) {
    setFieldReadonly(field);
  } else {
    field.tabIndex = tabindex0++;
  }
  
  cell = createCell(div, "column-user");
  cell.classList.add("cell-field");
  field = addField(cell, "user", listItem.user, null);
  field.setAttribute("value-original", listItem.user);
  if (!dataModifiable) {
    setFieldReadonly(field);
  } else {
    field.tabIndex = tabindex0++;
  }
  
  cell = createCell(div, "column-password");
  cell.classList.add("cell-field");
  field = addField(cell, "password", listItem.password, null);
  field.setAttribute("value-original", listItem.password);
  if (!dataModifiable) {
    setFieldReadonly(field);
  } else {
    field.tabIndex = tabindex0++;
  }
  
  if (dataModifiable) {
    cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  }
  
  row.appendChild(div);
  
  return row;
}

function setFieldReadonly(field) {
  field.setAttribute("readonly", "true");
  field.classList.add("readonly");
  field.title = "Поле нередактируемо, поскольку несколько Context/ResourceLink ссылаются на один и тот же Server/Resource в конфигурации Tomcat"; // NON-NLS
}

/* @Override from table.js */
function createRowCreate() {
  var row = document.createElement("div");
  row.classList.add("row");
  row.classList.add("created");
  
  var field;
  
  // active
  cell = createCell(row, "column-active");
  cell.classList.add("column-left");
  cell.classList.add("cell-field");
  field = addCheckbox(cell, true, false);
  field.classList.add("readonly");
  setCheckboxEnabled(field, false);
  field.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  
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
  field = addField(cell, "server", "", "db-server:1521");
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


