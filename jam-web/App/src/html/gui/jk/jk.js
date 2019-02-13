/* @Override from table.js */
function getApiListUrl() {
  return "api/jk/list";
}

/* @Override from table.js */
function getApiModUrl() {
  return "api/jk/mod";
}

/* @Override from table.js */
function uiOnTableModSuccess() {
  var message = "<span class=\"span-bold\">Изменения успешно сохранены.</span>&emsp;Сейчас Apache HTTPD перезагрузится." // NON-NLS // NON-NLS
    + "&emsp;<a href=\"\" onclick=\"document.location.reload();\">Обновить страницу</a>"; // NON-NLS
  
  statusSuccess(message);
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
  
  cell = createCell(div, "column-instance");
  label = document.createElement("label");
  label.innerHTML = "Инстанс Tomcat (по HTTP порту)"; // NON-NLS
  cell.appendChild(label);
  
  row.appendChild(div);
  
  return row;
}

/* @Override from table.js */
function validate(fieldName, fieldValue) {
  if (fieldName === "application") {
    if (!fieldValue) {
      return false;
    }
  }
  if (fieldName === "instance") { 
    // tomcat-server:8080
    return /^[^:]+:\d+$/.test(fieldValue);
  }
  return true;
}

/* @Override from table.js */
function getInvalidFieldMessage(field, error) {
  if (fieldName == "instance") {
    if (error) {
      if (error == "UNKNOWN_HOST") {
        return "Неизвестный хост";// NON-NLS
      } else if (error == "CONNECT_EXCEPTION") {
        return "Не удалось подключиться (порт не работает)";// NON-NLS
      } else if (error == "SOCKET_EXCEPTION") {
        return "Не удалось подключиться (порт не HTTP)";// NON-NLS
      } else if (error.startsWith("JTM_NOT_FOUND:")) {
        var link = error.substring(14);
        // TODO parametrize 'manager-ext'
        return "На инстансе не установлен manager-ext [" + link + "]";// NON-NLS
      } else if (error.startsWith("JTM_BROKEN:")) {
        var link = error.substring(11);
        // TODO parametrize 'manager-ext'
        return "На инстансе сломан manager-ext [" + link + "]";// NON-NLS
      }
    }
  } 
}




var tabindex0 = 1;

/* @Override from table.js */
function createRow(listItem) {
  var row = document.createElement("div");
  row.classList.add("row");
  row.setAttribute("item-location", listItem.location);
  
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
  
  cell = createCell(div, "column-instance");
  cell.classList.add("cell-field");
  field = addFieldInstance(cell, listItem.host, listItem.getHttpPortLink, null);
  field.setAttribute("value-original", "");
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
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
    setGetHttpPortButtonState(button, 0);
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
 * 0 -- initial state, 1 -- loading state
 */
function setGetHttpPortButtonState(button, state) {
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
    button.title = "При запросе инстанса возникла ошибка"; // NON-NLS
  }
}

function getHttpPortButtonClick(button, host, getHttpPortLink) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
        var responseJson = JSON.parse(this.responseText);
        
        if (responseJson.ajpResponse.status == 200) {
          setGetHttpPortButtonState(button, 0);
          
          //TODO resolve the relative path:
          var field = button.parentElement.firstChild;
          var value = host + ":" + responseJson.ajpResponse.responseBody;
          field.setAttribute("value-original", value);
          field.value = value;
          onFieldInput(field);
          
        } else {
          // TODO in the error case we do not know the HTTP port only,
          // but we still know host and ajp port (after the initial /list request)
          // Better to show at least the information we have?
          setGetHttpPortButtonState(button, 2);
          
          console.error("Error subrequesting HTTP port by AJP: status " + responseJson.ajpResponse.status 
              + ", message: " + responseJson.ajpResponse.statusMessage);
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
        setGetHttpPortButtonState(button, 2);
        
        console.error("Get HTTP port error: " + this.status);
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
 
  cell = createCell(flexColumns, "column-instance");
  cell.classList.add("cell-field");
  field = addFieldInstance(cell, null, null, "tomcat-server:8080");
  field.tabIndex = tabindex0++;
  onFieldInput(field);// trigger initial event
  
  cellDelete.getElementsByTagName("input")[0].tabIndex = tabindex0++;
  
  row.appendChild(flexColumns);
  
  return row;
}


