<!DOCTYPE html>
<html>
<head>
  <link rel="stylesheet" href="login.css">
  <title>Login Page</title>
</head>
<body>

<% if (request.getParameter("error") != null) { %>
  <h2>Error!</h2>
<% } %>

  <h2>Hello, please log in:</h2>
  <br><br>
  <form id="login-form" action="j_security_check" method=post>
    <div class="row">
      <div class="column-center">
        <input type="text" name="j_username" size="25">
      </div>
      <div class="column-left">
        <span class="field-label">Username</span>
      </div>
      <div class="column-right"></div>
    </div>
    <br/>
    <div class="row">
      <div class="column-center">
        <input type="password" size="15" name="j_password">
      </div>
      <div class="column-left">
        <span class="field-label">Password</span>
      </div>
      <div class="column-right"></div>
    </div>
    <br/>
    <div class="row">
      <div class="column-center">
        <input type="submit" value="Submit">
      </div>
      <div class="column-left"></div>
      <div class="column-right"></div>
    </div>
  </form>
</body>
</html>

<% 
  response.setStatus(401); 
%>