<!DOCTYPE html>
<html>
  <head>
    <link rel="stylesheet" href="jtm.css">
    <link rel="stylesheet" href="login/login.css">
    <script src="jtm.js"></script>
    <script src="login/login.js"></script>
    <title>Login</title>
  </head>
  <body>
    <script type="text/javascript">
      
      raiseLoginForm();
      
      function onLoginSuccess() {
        location.reload();
      }
    </script>
  
  </body>
</html>

<% 
  response.setStatus(401); 
%>