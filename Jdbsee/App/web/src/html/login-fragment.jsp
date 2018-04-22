<div id="loginFragment">
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
  #loginFragment {
    position: fixed;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    
    background-color: rgba(40, 40, 40, 0.85);
  }
  
  #loginFragment #loginFrame {
    position: fixed;
    
    left: 50%;
    margin-left: -150px;
    
    top: 50%;
    margin-top: -146px;
    
    text-align: center;
    
    background-color: white;
  }

  #login-form {
    padding: 40px 40px 20px 40px;
    background-color: white;
  }
  
  #login-form .row {
    position: relative;
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
  hideLoginForm();
</script>

<script type="text/javascript">
  function submitForm() {
    var xhttp = new XMLHttpRequest();
    xhttp.open("POST", "auth", true);
    xhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    var params = "username=" + document.getElementById("fieldUsername").value
        + "&password=" + document.getElementById("fieldPassword").value;
    xhttp.send(params);
    xhttp.onreadystatechange = function() {
      if (this.readyState == 4) {
        if (this.status == 200) {
          if (this.responseText === 'SUCCESS') {
            if (onLoginSuccessCallback0 != null) {
              onLoginSuccessCallback0();
            }
          } else {
            onLoginFailure(1);
          }
        } else {
          onLoginFailure(2);
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
  
  function onLoginFailure(reasonCode) {
    resetFields();
  
    // show status
    statusBar = document.getElementById("loginStatusBar");
    statusBar.className = "statusBar statusBar-error";
    
    if (reasonCode == 1) {
      statusBar.innerHTML = "Incorrect credentials, try again";
    } else {
      statusBar.innerHTML = "Authorization error, try later";
    }
  }
  
  var onLoginSuccessCallback0 = null; 
  
  function raiseLoginForm(statusText, onLoginSuccessCallback) {
    document.getElementById("loginFragment").style.display = "block";
    
    resetFields();

    statusBar = document.getElementById("loginStatusBar");
    statusBar.className = "statusBar statusBar-info";
    statusBar.innerHTML = statusText;

    onLoginSuccessCallback0 = onLoginSuccessCallback;
  } 
  
  function hideLoginForm() {
    document.getElementById("loginFragment").style.display = "none";
  }
</script>