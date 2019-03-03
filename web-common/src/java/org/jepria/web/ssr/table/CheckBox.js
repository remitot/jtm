(function() {
  var checkboxes = document.querySelectorAll(".checkbox");
  for (var i = 0; i < checkboxes.length; i++) {
    var checkbox = checkboxes[i];
    
    var input = checkbox.getElementsByTagName("input")[0];
    var span = checkbox.getElementsByTagName("span")[0];
    
    // add 'hovered' class for checkbox's onfocus and onmouseover  
    input.onfocus = function(event){
      var input = event.target;
      // TODO bad relative path
      input.parentElement.getElementsByClassName("checkmark")[0].classList.add("hovered");
    }
    input.addEventListener("focusout", function(event) { // .onfocusout not working in some browsers
      var input = event.target;
      // TODO bad relative path
      input.parentElement.getElementsByClassName("checkmark")[0].classList.remove("hovered");
    });
    span.onmouseover = function(event) {
      var checkmark = event.target;
      checkmark.classList.add("hovered");
    }
    span.addEventListener("mouseout", function(event) { // .onmouseout not working in some browsers
      var checkmark = event.target;
      checkmark.classList.remove("hovered");
    });
  }
}());