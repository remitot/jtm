function statusBar_onload() {
  addCloseScript();
  statusBar_position();
}

function addCloseScript() {
  var closeButton = document.getElementsByClassName("status-bar__header-close")[0];
  var statusBar = document.getElementsByClassName("status-bar")[0];
  if (closeButton && statusBar) {
    closeButton.onclick = function(statusBar){
      return function(event) { // javascript doesn't use block scope for variables
        statusBar.parentNode.removeChild(statusBar);
      }
    }(statusBar);
  }
}

function statusBar_position() {
  //center the status bar horizontally within the window
  var statusBar = document.getElementsByClassName("status-bar")[0];
  if (statusBar) {
    var statusBarWidth = statusBar.getBoundingClientRect().right - statusBar.getBoundingClientRect().left;
    statusBar.style.left = ((window.innerWidth - statusBarWidth) / 2) + "px";
  }
}