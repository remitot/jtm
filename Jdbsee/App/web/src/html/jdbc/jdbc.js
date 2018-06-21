function getJsonListItems(jsonResponse) {
  return jsonResponse.connections;
}

function getApiListUrl() {
  return "api/jdbc/list";
}

function getApiModUrl() {
  return "api/jdbc/mod";
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

function createRow(listItem) {
  row = document.createElement("div");
  row.classList.add("row");
  row.setAttribute("item-location", listItem.location);
  
  // active
  cell = createCell(row, "column-active");
  cell.classList.add("cell-field");
  addCheckbox(cell, listItem.active, true);
  cell.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  if (!listItem.active) {
    row.classList.add("inactive");
  }
  
  
  cellDelete = createCell(row, "column-delete");
  addFieldDelete(cellDelete);
  
  
  div = document.createElement("div");
  div.classList.add("flexColumns");
  
  cell = createCell(div, "column-name");
  cell.classList.add("cell-field");
  field = addField(cell, "name", listItem.name, null);
  field.setAttribute("value0", listItem.name);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-server");
  cell.classList.add("cell-field");
  field = addField(cell, "server", listItem.server, null);
  field.setAttribute("value0", listItem.server);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-db");
  cell.classList.add("cell-field");
  field = addField(cell, "db", listItem.db, null);
  field.setAttribute("value0", listItem.db);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-user");
  cell.classList.add("cell-field");
  field = addField(cell, "user", listItem.user, null);
  field.setAttribute("value0", listItem.user);
  field.tabIndex = tabindex0++;
  
  cell = createCell(div, "column-password");
  cell.classList.add("cell-field");
  field = addField(cell, "password", listItem.password, null);
  field.setAttribute("value0", listItem.password);
  field.tabIndex = tabindex0++;
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  row.appendChild(div);
  
  
  return row;
}

function createRowCreate() {

  // add header row if the table is empty
  var table = document.getElementById("table");
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


