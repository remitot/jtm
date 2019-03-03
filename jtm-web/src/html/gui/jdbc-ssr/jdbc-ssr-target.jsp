<%-- Target (output) page for the server-side-rendered GUI --%>
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
    
    <link rel="stylesheet" href="gui/jdbc/jdbc.css">
    <script type="text/javascript" src="gui/jdbc-ssr/table.js"></script>
    
  </head>
  
  <body onload="jtm_onload();">
  
    <%@ include file="/gui/page-header.fragment.jsp" %>
    
    <div id="statusBar" class="statusBar statusBar-none"></div>
    
    <div id="table-container">
      <%= request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableHtml") %>
    </div>
    <script type="text/javascript">
      <%= request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableScript") %>
    </script>
    <script type="text/javascript">
      addTableScript(document.getElementById("table-container").firstElementChild);
    </script>
    
    <%@ include file="/gui/control-buttons.fragment.html" %>
    
    <%@ include file="/gui/login.fragment.html" %>    
    
  </body>
</html>
