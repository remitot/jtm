<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Tomcat manager: датасорсы (JDBC)</title> <!-- NON-NLS -->
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 
    
    <link rel="stylesheet" href="jtm.css">
    <link rel="stylesheet" href="table.css">
    <link rel="stylesheet" href="checkbox.css">
    <script type="text/javascript" src="jtm.js"></script>
    <script type="text/javascript" src="table.js"></script>
    
    <link rel="stylesheet" href="jdbc/jdbc.css">
    <script type="text/javascript" src="jdbc/jdbc.js"></script>
    
  </head>
  
  <body onload="reload()">
  
    <%@ include file="../page-header.fragment.html" %>
    
    <div id="statusBar" class="statusBar statusBar-none"></div>
    
    <div id="table" style="width:100%"></div>
    
    <%@ include file="../control-buttons.fragment.html" %>
    
    <%@ include file="../login.fragment.html" %>    
    
  </body>
</html>
