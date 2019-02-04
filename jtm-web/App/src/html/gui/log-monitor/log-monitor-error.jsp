<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Tomcat manager: логи</title> <!-- NON-NLS -->
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 
    
    <link rel="stylesheet" href="gui/jtm.css">
    <script type="text/javascript" src="gui/jtm.js"></script>
    
  </head>
  
  <body onload="jtm_onload();logMonitorError_onload();">
  
    <%@ include file="/gui/login.fragment.html" %>    
    
    <script type="text/javascript">
      function logMonitorError_onload() {
        raiseLoginForm(function() {
          hideLoginForm();
          windowReload();
        });
      }
    </script>
  </body>
</html>
