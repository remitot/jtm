
function onCheckboxActiveInput(event) {
  var checkbox = event.target;// this will be SPAN, then INPUT on a single click
  if (checkbox.tagName.toLowerCase() == "input") {
    if (checkbox.checked && checkbox.getAttribute("value0") == "true" || !checkbox.checked && checkbox.getAttribute("value0") == "false") {
      checkbox.parentElement.classList.remove("modified");
    } else {
      checkbox.parentElement.classList.add("modified");
    }
    
    if (!checkbox.checked) {
      //TODO make abstract
      checkbox.parentElement.parentElement.parentElement.parentElement.classList.add("inactive");
    } else {
    //TODO make abstract
      checkbox.parentElement.parentElement.parentElement.parentElement.classList.remove("inactive");
    }
  }
}

function createCheckboxActive(active) {
  var field = document.createElement("label");
  field.classList.add("checkbox-active");
  
  var checkbox = document.createElement("input");
  checkbox.type = "checkbox";
  checkbox.name = "active";
  checkbox.checked = active;
  checkbox.setAttribute("value0", active);
  checkbox.onfocus = function(event){onCheckboxActiveFocusOn(event);}
  checkbox.addEventListener("focusout", function(event){onCheckboxActiveFocusOff(event);});// .onfocusout not working in some browsers
  field.appendChild(checkbox);
  
  var span = document.createElement("span");
  span.classList.add("checkmark");
  span.onmouseover = function(event){onCheckboxActiveHoverOn(event);}
  span.addEventListener("mouseout", function(event){onCheckboxActiveHoverOff(event);});// .onmouseout not working in some browsers
  field.appendChild(span);
  
  return field;
}

function onCheckboxActiveFocusOn(event) {
  var checkbox = event.target;
  checkbox.parentElement.querySelector(".checkmark").classList.add("hovered");
}

function onCheckboxActiveFocusOff(event) {
  var checkbox = event.target;
  checkbox.parentElement.querySelector(".checkmark").classList.remove("hovered");
}

function onCheckboxActiveHoverOn(event) {
  var checkmark = event.target;
  checkmark.classList.add("hovered");
}

function onCheckboxActiveHoverOff(event) {
  var checkmark = event.target;
  checkmark.classList.remove("hovered");
}
