/**
 * Adds hover style for .big-black-button
 */
function addHoverForBigBlackButton(bigBlackButton) {
  if (bigBlackButton.classList.contains("big-black-button")) {
  
    bigBlackButton.onfocus = function(event){
      event.target.classList.add("hovered");
    }
    bigBlackButton.addEventListener("focusout", function(event) { // .onfocusout not working in some browsers
      event.target.classList.remove("hovered");
    });
    
    bigBlackButton.onmouseover = function(event) {
      event.target.classList.add("hovered");
    }
    bigBlackButton.addEventListener("mouseout", function(event) { // .onmouseout not working in some browsers
      event.target.classList.remove("hovered");
    });
  }
}

/**
  * Public API.
  */
function jtm_onload() {
  
  // add .big-black-button hover style
  bigBlackButtons = document.getElementsByClassName("big-black-button");
  for (var i = 0; i < bigBlackButtons.length; i++) {
    addHoverForBigBlackButton(bigBlackButtons[i]);
  }
  
}


function statusClear() {
  statusBar = document.getElementById("statusBar");
  statusBar.className = "statusBar statusBar-none";
  statusBar.innerHTML = "";
}

function statusInfo(message) {
  statusBar = document.getElementById("statusBar"); 
  statusBar.className = "statusBar statusBar-info";
  statusBar.innerHTML = message;
}

function statusError(message) {
  statusBar = document.getElementById("statusBar"); 
  statusBar.className = "statusBar statusBar-error";
  statusBar.innerHTML = message;
}

function statusSuccess(message) {
  statusBar = document.getElementById("statusBar"); 
  statusBar.className = "statusBar statusBar-success";
  statusBar.innerHTML = message;
}


function windowReload(newUrl) {
  if (newUrl) {
    /* because location.reload() not working in FF and Chrome */ 
    window.location.href = newUrl;
  } else {
    window.location.reload(true);
  }
}

function logout(afterLogoutCallback) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
        statusClear();
        
        if (afterLogoutCallback != null) {
          afterLogoutCallback();
        }
      } else {
        statusError("Сетевая ошибка " + this.status); // NON-NLS
      }
    }
  };
  xhttp.open("POST", "api/logout", true);
  xhttp.send();
}

