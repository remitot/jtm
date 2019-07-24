function restart_onload() {
  
  // place the restart control button directly below the current menu item
  var currentHeaderMenuItem = document.getElementsByClassName("page-header__menu-item_current")[0];
  if (currentHeaderMenuItem) {
    
    var controlButtonRestart = document.getElementsByClassName("control-button_restart")[0];
    if (controlButtonRestart) {

      var rect0 = currentHeaderMenuItem.getBoundingClientRect();
      var centerX0 = (rect0.left + rect0.right) / 2;
      
      var rect1 = controlButtonRestart.getBoundingClientRect();
      var centerX1 = (rect1.left + rect1.right) / 2;
      
      controlButtonRestart.style.marginLeft = (centerX0 - centerX1) + "px";
    }
  }
}
