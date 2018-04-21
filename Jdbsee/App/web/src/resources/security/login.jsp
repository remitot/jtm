<!DOCTYPE html>
<html>
<head>
  <link rel="stylesheet" href="jtm.css">
  <link rel="stylesheet" href="login/login.css">
  <title>Login Page</title>
</head>
<body>
  
  <%-- TODO resolve the relative path! --%>
  <%@include file="../../login/login-fragment.html" %>
  
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