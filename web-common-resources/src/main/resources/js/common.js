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
function common_onload() {
  
  // add .big-black-button hover style
  bigBlackButtons = document.getElementsByClassName("big-black-button");
  for (var i = 0; i < bigBlackButtons.length; i++) {
    addHoverForBigBlackButton(bigBlackButtons[i]);
  }
  
}


function windowReload(newUrl) {
  if (newUrl) {
    /* because location.reload() not working in FF and Chrome */ 
    window.location.href = newUrl;
  } else {
    window.location.reload(true);
  }
}
