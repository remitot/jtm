function getInput(checkbox) {
  return checkbox.getElementsByTagName("input")[0];
}

(function() {
  var checkboxes = document.querySelectorAll(".checkbox");
  for (var i = 0; i < checkboxes.length; i++) {
    var checkbox = checkboxes[i];
    
    var input = checkbox.getElementsByTagName("input")[0];
    var checkmark = checkbox.querySelectorAll("span.checkmark")[0];
    
    // add 'hovered' class for checkbox's onfocus and onmouseove
      
    input.onfocus = function(checkmark) {
      return function(event) { // javascript doesn't use block scope for variables
        checkmark.classList.add("hovered");
      }
    }(checkmark);
    
    input.addEventListener("focusout", // .onfocusout not working in some browsers
        function(checkmark) { // javascript doesn't use block scope for variables
      return function(event) {
        checkmark.classList.remove("hovered");
      }
    }(checkmark));
    
    checkmark.onmouseover = function(checkmark) {
      return function(event) { // javascript doesn't use block scope for variables
        checkmark.classList.add("hovered");
      }
    }(checkmark);
    
    checkmark.addEventListener("mouseout", // .onmouseout not working in some browsers
        function(checkmark) { // javascript doesn't use block scope for variables
      return function(event) {
        checkmark.classList.remove("hovered");
      }
    }(checkmark));
  }
}());

function setCheckboxEnabled(checkbox, enabled) {
  if (enabled) {
    checkbox.classList.remove("checkbox-disabled");
    getInput(checkbox).disabled = false;
  } else {
    checkbox.classList.add("checkbox-disabled");
    getInput(checkbox).disabled = true;
  }
}
