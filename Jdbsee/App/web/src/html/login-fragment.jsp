<div id="loginScreen" class="loginForm-hidden">
  <div id="loginFrame">
    <div id="loginStatusBar" class="statusBar statusBar-none"></div>
    <form id="login-form">
      <div class="row">
        <input type="text" class="field-text" id="fieldUsername"
             placeholder="username">
      </div>
      <div class="row">
        <input type="password" class="field-text" id="fieldPassword"
             placeholder="password">
      </div>
      <div class="row">
        <button type="submit" class="big-black-button" 
            onclick="submitForm(); return false;">LOGIN</button>
      </div>
    </form>
  </div>
</div>

<style type="text/css">
  #loginScreen {
    position: fixed;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    
    background-color: rgb(40, 40, 40); /* fallback */
    background-color: rgba(40, 40, 40, 0.85);
  }
  
  #loginScreen #loginFrame {
    position: fixed;
    
    left: 50%;
    margin-left: -150px;
    
    top: 50%;
    margin-top: -144px;
    
    text-align: center;
    
    background-color: white;
  }
  
  #loginScreen.loginForm-raised {
    display: block;
  }
  
  #loginScreen.loginForm-hidden {
    display: none;
  }

  #login-form {
    padding: 40px 40px 20px 40px;
    background-color: white;
  }
  
  #login-form .row {
    text-align: center;
  }
  
  #login-form .row .field-text {
    width: 200px;
    margin-top: 15px;
  }
  
  #login-form .big-black-button {
    margin-top: 40px;
  }
</style>

<script type="text/javascript">
  function submitForm() {
    var xhttp = new XMLHttpRequest();
    xhttp.open("POST", "login", true);
    xhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    var params = "username=" + document.getElementById("fieldUsername").value
        + "&password=" + document.getElementById("fieldPassword").value;
    xhttp.send(params);
    xhttp.onreadystatechange = function() {
      if (this.readyState == 4) {
        if (this.status == 200) {
          if (onLoginSuccessCallback0 != null) {
            onLoginSuccessCallback0();
          }
        } else {
          onLoginFailure(this.status);
        }
      }
    };
  }
  
  function resetFields() {
    fieldUsername = document.getElementById("fieldUsername");
    fieldUsername.value = "";
    fieldUsername.focus();
    
    fieldPassword = document.getElementById("fieldPassword");
    fieldPassword.value = "";
  }
  
  function onLoginFailure(httpStatus) {
    resetFields();
  
    // show status
    statusBar = document.getElementById("loginStatusBar");
    statusBar.className = "statusBar statusBar-error";
    
    if (httpStatus == 401) {
      statusBar.innerHTML = "Incorrect credentials, try again";
    } else {
      statusBar.innerHTML = "Authorization error, try later";
    }
  }
  
  var onLoginSuccessCallback0 = null; 
  
  function raiseLoginForm(onLoginSuccessCallback) {
    document.getElementById("loginScreen").className = "loginForm-raised";
    
    resetFields();

    statusBar = document.getElementById("loginStatusBar");
    statusBar.className = "statusBar statusBar-info";
    statusBar.innerHTML = "Are you the server admin?";

    onLoginSuccessCallback0 = onLoginSuccessCallback;
  } 
  
  function hideLoginForm() {
    document.getElementById("loginScreen").className = "loginForm-hidden";
  }
</script>