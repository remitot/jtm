<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Tomcat manager: порты</title> <!-- NON-NLS -->
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 
    
    <link rel="stylesheet" href="jtm.css">
    <link rel="stylesheet" href="table.css">
    <script type="text/javascript" src="jtm.js"></script>
    <script type="text/javascript" src="table.js"></script>
    
    <link rel="stylesheet" href="port/port.css">
    <script type="text/javascript" src="port/port.js"></script>
    
  </head>
  
  <body onload="table_reload();jtm_onload();">
    
    <%@ include file="../page-header.fragment.html" %>
     
    <div id="statusBar" class="statusBar statusBar-none"></div>
  
    <div id="table" style="width:100%"></div>
    
    <%@ include file="../login.fragment.html" %>    
    
  </body>
</html>
