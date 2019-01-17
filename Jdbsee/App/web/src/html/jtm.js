/**
 * Adds graphical hovering for a button of a class 'big-black-button'
 */
function addHoverForBigBlackButton(button) {
  if (button.classList.contains("big-black-button")) {
  
    button.onfocus = function(event){
      var button = event.target;
      button.classList.add("hovered");
    }
    button.addEventListener("focusout", function(event) { // .onfocusout not working in some browsers
      var button = event.target;
      button.classList.remove("hovered");
    });
    
    button.onmouseover = function(event) {
      var button = event.target;
      button.classList.add("hovered");
    }
    button.addEventListener("mouseout", function(event) { // .onmouseout not working in some browsers
      var button = event.target;
      button.classList.remove("hovered");
    });
  }
}

function reload() {
  reloadTable();
  
  bbbs = document.getElementsByClassName("big-black-button");
  for (var i = 0; i < bbbs.length; i++) {
    bbb = bbbs[i];
    addHoverForBigBlackButton(bbb);
  }
}

function logout() {
  var xhttp = new XMLHttpRequest();
  xhttp.open("POST", "api/logout", false); // synchronous invocation
  xhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
  xhttp.send();
  
  document.location.reload(false);
}


