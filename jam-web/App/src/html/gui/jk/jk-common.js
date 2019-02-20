/* @Override from table.js */
function getApiListUrl() {
  return "api/jk/list";
}

/* @Override from table.js */
function onTableModSuccess(jsonItemList) {
  // super:
  uiOnTableModSuccess();
  recreateTable(jsonItemList, isEditable());
  // :super
  
  restartApacheAfterMod();
}

function getApiRestartUrl() {
  return "api/restart";
}

/**
 * Send Apache restart request after modifications succeeded 
 */
function restartApacheAfterMod() {
  var message = "<span class=\"span-bold\">Все изменения сохранены.</span>&emsp;" + // NON-NLS
      "Подожите, пока Apache перезагрузится..."; // NON-NLS
  statusInfo(message);

  
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
      
        onApacheRestartAfterModSuccess();
        
      } else {
        onApacheRestartAfterModError(this.status);
      }
    }
  };
  xhttp.open("POST", getApiRestartUrl(), true);
  xhttp.send();
}


function onApacheRestartAfterModSuccess() {
  var message = "<span class=\"span-bold\">Все изменения сохранены.&emsp;Apache перезагрузился.</span>"; // NON-NLS
  statusSuccess(message);
}

function onApacheRestartAfterModError(status) {
  // TODO logic leak: if the modifications successfully saved on the server, 
  // but after that (before restart request) any error occurred, then, after table_reload() or page refresh, 
  // the user will not know that the service actually has not been restarted
  if (status == 401) {
    statusError("Требуется авторизация"); // NON-NLS
    
    raiseLoginForm(function() {
      hideLoginForm();
      table_reload();  
    });
    
  } else if (status == 403) {
    var message = "<span class=\"span-bold\">Доступ запрещён.</span>&emsp;" + // NON-NLS
        "<a href=\"#\" onclick=\"logout(table_reload);\">Выйти</a> чтобы сменить пользователя"; // NON-NLS // NON-NLS 
    statusError(message);
    
  } else {
    console.error("Failed to restart apache server (status " + status + ")");
    var message = "<span class=\"span-bold\">Все изменения сохранены, но при перезагрузке Apache произошла ошибка.</span>&emsp;" + // NON-NLS
        "<a href=\"#\" onclick=\"restartApache();\">Перезагрузить ещё раз</a>"; // NON-NLS
    statusError(message); // NON-NLS
  }
}  

/**
 * Send Apache restart request 
 */
function restartApache() {
  var message = "Подожите, пока Apache перезагрузится..."; // NON-NLS
  statusInfo(message);

  
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
      
        onApacheRestartSuccess();
        
      } else {
        onApacheRestartError(this.status);
      }
    }
  };
  xhttp.open("POST", getApiRestartUrl(), true);
  xhttp.send();
}

function onApacheRestartSuccess() {
  var message = "<span class=\"span-bold\">Apache перезагрузился.</span>"; // NON-NLS
  statusSuccess(message);
}

function onApacheRestartError(status) {
  // TODO logic leak: if the modifications successfully saved on the server, 
  // but after that (before restart request) any error occurred, then, after table_reload() or page refresh, 
  // the user will not know that the service actually has not been restarted
  if (status == 401) {
    statusError("Требуется авторизация"); // NON-NLS
    
    raiseLoginForm(function() {
      hideLoginForm();
      table_reload();  
    });
    
  } else if (status == 403) {
    var message = "<span class=\"span-bold\">Доступ запрещён.</span>&emsp;" +
        "<a href=\"#\" onclick=\"logout(table_reload);\">Выйти</a> чтобы сменить пользователя"; // NON-NLS // NON-NLS // NON-NLS 
    statusError(message);
    
  } else {
    console.error("Failed to restart apache server (status " + status + ")");
    var message = "<span class=\"span-bold\">При перезагрузке Apache произошла ошибка.</span>&emsp;" +
        "<a href=\"#\" onclick=\"restartApache();\">Перезагрузить ещё раз</a>"; // NON-NLS // NON-NLS
    statusError(message); // NON-NLS
  }
}  

/* @Override from table.js */
function clientValidate(fieldName, fieldValue) {
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
function getServerInvalidFieldMessage(fieldName, errorCode, errorMessage) {
  if (fieldName == "application") {
    if (errorCode) {
      if (errorMessage) {
        console.error(errorMessage + " " + errorCode);
      }
      if (errorCode == "DUPLICATE_NAME") {
        return "Такое приложение уже есть";// NON-NLS
      }
    }
  } else if (fieldName == "instance") {
    if (errorCode) {
      if (errorMessage) {
         console.error(errorMessage + " " + errorCode);
      }
      if (errorCode == "UNKNOWN_HOST") {
        return "Неизвестный хост";// NON-NLS
      } else if (errorCode == "CONNECT_EXCEPTION") {
        return "Похоже, не работает порт";// NON-NLS
      } else if (errorCode == "SOCKET_EXCEPTION" || errorCode == "CONNECT_TIMEOUT") {
        return "Похоже, не http порт";// NON-NLS
      } else if (errorCode == "UNAUTHORIZED") {
        return "Похоже, инстанс не в общем кластере с текущим";// NON-NLS
      } else if (errorCode == "NOT_FOUND" || errorCode == "EXECUTION_ERROR") {
        return "Похоже, на инстансе сломан менеджер";// NON-NLS
      }
    }
  } 
}