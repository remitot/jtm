<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Tomcat manager: mod_jk</title> <!-- NON-NLS -->
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    
    <link rel="stylesheet" href="jtm.css">
    <link rel="stylesheet" href="table.css">
    <link rel="stylesheet" href="checkbox.css">
    <script type="text/javascript" src="jtm.js"></script>
    <script type="text/javascript" src="table.js"></script>
    
    <link rel="stylesheet" href="modjk/modjk.css">
    <script type="text/javascript" src="modjk/modjk.js"></script>
    
  </head>
  
  <body onload="table_reload();jtm_onload();">
  
    <%@ include file="../page-header.fragment.html" %>
    
    <div id="statusBar" class="statusBar statusBar-none"></div>
  
    <div id="table" style="width:100%"></div>
    
    <%@ include file="../control-buttons.fragment.html" %>
    
    <%@ include file="../login.fragment.html" %>    
    
  </body>
</html>
