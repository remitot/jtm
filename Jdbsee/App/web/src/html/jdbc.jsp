<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>JDBC connections — Tomcat manager</title>
    <link rel="stylesheet" href="jtm.css">
    <link rel="stylesheet" href="jdbc.css">
    <link rel="stylesheet" href="checkbox-ca.css">
    <script src="jdbc.js"></script>
  </head>
  
  <body onload="reload()">
    <div id="jdbcStatusBar" class="statusBar statusBar-none"></div>
    <div id="connections" style="width:100%"></div>
    
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

<%@ include file="login-fragment.jsp" %>    
    
  </body>
</html>