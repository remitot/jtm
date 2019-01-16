<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>JDBC connections — Tomcat manager</title>
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    
    <link rel="stylesheet" href="jtm.css">
    <link rel="stylesheet" href="table.css">
    <link rel="stylesheet" href="jdbc/jdbc.css">
    <link rel="stylesheet" href="checkbox.css">
    <script type="text/javascript" src="table.js"></script>
    <script type="text/javascript" src="jdbc/jdbc.js"></script>
    
  </head>
  
  <body onload="reload()">
    <div id="table" style="width:100%"></div>
    
    <div id="controlButtons">
      <%@ include file="../control-buttons.html" %>
    </div>
    
    <!-- after main content elements: -->
    <div id="jdbcStatusBar" class="statusBar statusBar-none"></div>
    
    <%@ include file="../login-fragment.jsp" %>    
    
  </body>
</html>
