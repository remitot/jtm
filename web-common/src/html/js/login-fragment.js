function onButtonLoginClick() {
  xhttp = new XMLHttpRequest();
  xhttp.open("POST", "login", true);
  xhttp.onreadystatechange = function() {
    if (xhttp.readyState != 4) return;
    
    // reload page; location.reload() not working in FF and Chrome 
    window.location.href = window.location.href;
  } 
  xhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
  var params = "username=" + document.getElementById("fieldUsername").value
    + "&password=" + document.getElementById("fieldPassword").value;
  xhttp.send(params);
}

function loginFragment_onload() {
  document.getElementById("fieldUsername").focus();
}
