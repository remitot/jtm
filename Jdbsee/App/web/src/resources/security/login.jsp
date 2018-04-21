<!DOCTYPE html>
<html>
<head>
  <link rel="stylesheet" href="jtm.css">
  <link rel="stylesheet" href="login.css">
  <title>Login Page</title>
</head>
<body>
  <div id="statusBar" class="statusBar-none"></div>
  <form id="login-form" action="j_security_check" method=post>
    <div class="row">
      <input id="username" type="text" class="field-text" name="j_username"
           placeholder="username">
    </div>
    <br/>
    <div class="row">
      <input type="password" class="field-text" name="j_password"
           placeholder="password">
    </div>
    <br/>
    <div class="row">
      <input type="submit" class="big-black-button" value="LOGIN">
    </div>
  </form>
  
  <script type="text/javascript">
    document.getElementById("username").focus();
  </script>
  
<% if (request.getParameter("error") != null) { %>
  <script type="text/javascript">
    statusBar = document.getElementById("statusBar"); 
    statusBar.className = "statusBar-error";
    statusBar.innerHTML = "Incorrect credentials, try again";
  </script>
<% } else { %>
  <script type="text/javascript">
    statusBar = document.getElementById("statusBar"); 
    statusBar.className = "statusBar-info";
    statusBar.innerHTML = "Log in to proceed";
  </script>
<% } %>

</body>
</html>

<% 
  response.setStatus(401); 
%>