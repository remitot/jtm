<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Tomcat manager: датасорсы (JDBC)</title> <!-- NON-NLS -->
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 
    
    <link rel="stylesheet" href="gui/jtm.css">
    <link rel="stylesheet" href="gui/table.css">
    <link rel="stylesheet" href="gui/checkbox.css">
    <script type="text/javascript" src="gui/jtm.js"></script>
    <script type="text/javascript" src="gui/table.js"></script>
    
    <link rel="stylesheet" href="gui/jdbc/jdbc.css">
    <script type="text/javascript" src="gui/jdbc/jdbc.js"></script>
    
  </head>
  
  <body onload="table_reload();jtm_onload();">
  
    <%@ include file="/gui/page-header.fragment.html" %>
    
    <div id="statusBar" class="statusBar statusBar-none"></div>
    
    <div id="table" style="width:100%"></div>
    
    <%@ include file="/gui/control-buttons.fragment.html" %>
    
    <%@ include file="/gui/login.fragment.html" %>    
    
  </body>
</html>