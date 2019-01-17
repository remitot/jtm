<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Tomcat manager: порты</title> <!-- NON-NLS -->
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 
    
    <link rel="stylesheet" href="jtm.css">
    <link rel="stylesheet" href="table.css">
    <link rel="stylesheet" href="portinfo/portinfo.css">
    <script type="text/javascript" src="table.js"></script>
    <script type="text/javascript" src="portinfo/portinfo.js"></script>
    
  </head>
  
  <body onload="reload()">
    <div id="statusBar" class="statusBar statusBar-none"></div>
  
    <div id="table" style="width:100%"></div>
    
    <%@ include file="../login.fragment.html" %>    
    
  </body>
</html>
