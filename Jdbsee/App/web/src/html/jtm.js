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
        statusBar.innerHTML = "Login as a server administrator";
      }
    }
  };
  
}