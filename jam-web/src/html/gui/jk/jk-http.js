/* @Override from table.js */
function getApiModUrl() {
  return "api/jk/mod/http";
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
  label.innerHTML = "Инстанс Tomcat (по HTTP порту)"; // NON-NLS
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
  field = addFieldInstance(cell, listItem.host, listItem.getHttpPortLink, "tomcat-server:8080");
  field.setAttribute("value-original", "");
  
  var deleteButton = addFieldDelete(cellDelete);
  deleteButton.tabIndex = tabindex0++;
  
  row.appendChild(div);
  
  return row;
}

function addFieldInstance(cell, host, getHttpPortLink, placeholder) {
  var div = document.createElement("div");

  var field = createField("instance", "", placeholder);
  field.tabIndex = tabindex0++;
  div.appendChild(field);
  
  if (getHttpPortLink) {
    var button = document.createElement("button");
    button.classList.add("deletable");
    setGetHttpPortButtonState(button, 0, null);
    button.onclick = function(){getHttpPortButtonClick(button, host, getHttpPortLink);}
    button.tabIndex = tabindex0++;
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
 * @param state 0 -- initial state, 1 -- loading state, 2 -- error state (together with errorMessage)
 */
function setGetHttpPortButtonState(button, state, errorMessage) {
  if (state == 0) {
    button.disabled = false;
    button.innerHTML = "Запросить"; // NON-NLS
    button.title = "Сделать запрос: к какому инстансу привязано приложение"; 
  } else if (state == 1) {
    button.disabled = true;
    button.innerHTML = "Загрузка..."; // NON-NLS
    button.title = "Запрашивается инстанс"; // NON-NLS
  } else {
    button.disabled = true;
    button.innerHTML = "Ошибка"; // NON-NLS
    if (errorMessage) {
      button.title = errorMessage;
    } else {
      button.title = null;
    }
  }
}

function getHttpPortButtonClick(button, host, getHttpPortLink) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
        var responseJson = JSON.parse(this.responseText);
        
        if (responseJson.status == "SUCCESS") {
          setGetHttpPortButtonState(button, 0, null);
          
          //TODO resolve the relative path:
          var field = button.parentElement.firstChild;
          var value = host + ":" + responseJson.httpPort;
          field.setAttribute("value-original", value);
          field.value = value;
          onFieldInput(field);
          
        } else {
          // TODO in the error case we do not know the HTTP port only,
          // but we still know host and ajp port (after the initial /list request)
          // Better to show at least the information we have?
          
          var errorMessage = "При запросе http порта возникла ошибка"; // NON-NLS
          var errorCode = responseJson.status;
          if (errorCode) {
            if (errorCode.startsWith("UNKNOWN_HOST@@")) {
              var split = errorCode.split("@@");
              errorMessage = "При запросе http порта по URL " + split[1] + " возникла ошибка: неизвестный хост";// NON-NLS
            } else if (errorCode.startsWith("CONNECT_EXCEPTION@@")) {
              var split = errorCode.split("@@");
              errorMessage = "При запросе http порта по URL " + split[1] + " возникла ошибка: похоже, на хосте не работает порт";// NON-NLS
            } else if (errorCode.startsWith("SOCKET_EXCEPTION@@") || errorCode.startsWith("CONNECT_TIMEOUT@@")) {
              var split = errorCode.split("@@");
              errorMessage = "При запросе http порта по URL " + split[1] + " возникла ошибка: похоже, указанный порт не http";// NON-NLS
            } else if (errorCode.startsWith("UNSUCCESS_STATUS@@")) {
              // UNSUCCESS_STATUS@@status_int@@url_string 
              var split = errorCode.split("@@");
              errorMessage = "При запросе http порта возникла ошибка: запрос на " + split[2] + " вернул статус " + split[1];// NON-NLS
            }
          }
          setGetHttpPortButtonState(button, 2, errorMessage);
        }
        
      } else if (this.status == 401) {
        statusError("Требуется авторизация"); // NON-NLS
    
        raiseLoginForm(function() {
          hideLoginForm();
          table_reload();  
        });
        
      } else {
        // TODO in the error case we do not know the HTTP port only,
        // but we still know host and ajp port (after the initial /list request)
        // Better to show at least the information we have?
        var errorMessage = "При запросе http порта возникла ошибка"; // NON-NLS
        setGetHttpPortButtonState(button, 2, errorMessage);
        
        console.error("Get HTTP port error: " + this.status);
      }
    }
  };
  xhttp.open("GET", getHttpPortLink, true);
  xhttp.send();
  
  setGetHttpPortButtonState(button, 1, null);
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
  
  var flexColumns = document.createElement("div");
  flexColumns.classList.add("flexColumns");
  
  var field;
  
  cell = createCell(flexColumns, "column-application");
  cell.classList.add("cell-field");
  field = addField(cell, "application", null, "Application");
  field.tabIndex = tabindex0++;
 
  cell = createCell(flexColumns, "column-instance");
  cell.classList.add("cell-field");
  field = addFieldInstance(cell, null, null, "tomcat-server:8080");
  field.tabIndex = tabindex0++;
  
  var deleteButton = addFieldDelete(cellDelete);
  deleteButton.tabIndex = tabindex0++;
  
  row.appendChild(flexColumns);
  
  return row;
}


