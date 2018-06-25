<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>mod_jk bindings — Tomcat manager</title>
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    
    <link rel="stylesheet" href="jtm.css">
    <link rel="stylesheet" href="table.css">
    <link rel="stylesheet" href="modjk/modjk.css">
    <link rel="stylesheet" href="checkbox.css">
    <script type="text/javascript" src="table.js"></script>
    <script type="text/javascript" src="modjk/modjk.js"></script>
    
  </head>
  
  <body onload="reload()">
    <div id="table" style="width:100%"></div>
    
    <div id="controlButtons">
      <button 
          onclick="onCreateButtonClick()" 
          class="big-black-button"
          >NEW CONNECTION</button>
      <button 
          id="buttonSave" 
          onclick="onSaveButtonClick()" 
          class="big-black-button" 
          disabled
          >SAVE EVERYTHING</button>
    </div>
    
    <!-- after main content elements: -->
    <div id="jdbcStatusBar" class="statusBar statusBar-none"></div>
    
<%@ include file="../login-fragment.jsp" %>    
    
  </body>
</html>