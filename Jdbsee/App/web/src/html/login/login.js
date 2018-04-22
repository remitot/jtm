function raiseLoginForm() {

  var xhttp = new XMLHttpRequest();
  xhttp.open("POST", "login/login-fragment.html", true);
  xhttp.send();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
      
        loginFragment = document.createElement("div");
        loginFragment.id = "loginFragment";
        
        loginFrame = document.createElement("div");
        loginFrame.id = "loginFrame";
        loginFrame.innerHTML = this.responseText;
        
        loginFragment.appendChild(loginFrame);
        document.body.appendChild(loginFragment);
        
        // prepare form
        document.getElementById("field-username").focus();
    
        statusBar = document.getElementById("loginStatusBar"); 
        statusBar.className = "statusBar statusBar-info";
        statusBar.innerHTML = "Are you the server admin?";
      }
    }
  };
}
    
function submitForm() {
  var xhttp = new XMLHttpRequest();
  xhttp.open("POST", "auth", true);
  xhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
  var params = "username=" + document.getElementById("field-username").value
      + "&password=" + document.getElementById("field-password").value;
  xhttp.send(params);
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4) {
      if (this.status == 200) {
        if (this.responseText === 'SUCCESS') {
          onLoginSuccess();
        } else {
          onLoginFailure(1);
        }
      } else {
        onLoginFailure(2);
      }
    }
  };
}

function onLoginFailure(reasonCode) {
  // clear fields
  fieldUsername = document.getElementById("field-username");
  fieldUsername.value = "";
  fieldUsername.focus();
  
  fieldPassword = document.getElementById("field-password");
  fieldPassword.value = "";

  // show status
  statusBar = document.getElementById("loginStatusBar");
  statusBar.className = "statusBar statusBar-error";
  
  if (reasonCode == 1) {
    statusBar.innerHTML = "Incorrect credentials, try again";
  } else {
    statusBar.innerHTML = "Authorization error, try later";
  }
} 