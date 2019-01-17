<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Tomcat manager: mod_jk</title> <!-- NON-NLS -->
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    
    <link rel="stylesheet" href="jtm.css">
    <link rel="stylesheet" href="table.css">
    <link rel="stylesheet" href="modjk/modjk.css">
    <link rel="stylesheet" href="checkbox.css">
    <script type="text/javascript" src="table.js"></script>
    <script type="text/javascript" src="modjk/modjk.js"></script>
    <script type="text/javascript" src="jtm.js"></script>
    
  </head>
  
  <body onload="reload()">
  
    <div id="pageHeader" class="pageHeader">
      <label>Настройки Apache mod_jk</label> <!-- NON-NLS -->
      <div id="buttonLogout" class="big-black-button" onclick="logout();">ВЫЙТИ</div> <!-- NON-NLS -->
    </div>
    
    <div id="statusBar" class="statusBar statusBar-none"></div>
  
    <div id="table" style="width:100%"></div>
    
    <div id="controlButtons">
      <%@ include file="../control-buttons.fragment.html" %>
    </div>
    
    <%@ include file="../login.fragment.html" %>    
    
  </body>
</html>
