<%-- Target (output) page for the server-side-rendered GUI --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Tomcat manager: датасорсы (JDBC)</title> <!-- NON-NLS -->
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 
    
    <link rel="stylesheet" href="gui/jtm-no-statusBar.css">
    <script type="text/javascript" src="gui/jtm-no-statusBar.js"></script>
    
    <link rel="stylesheet" href="gui/jdbc-ssr/jdbc.css">
    
  </head>
  
  <body onload="jtm_onload();table_onload();controlButtons_onload();">
  
    <%= request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.pageHeaderHtml") %>
    <style type="text/css">
      <%= request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.pageHeaderStyle") %>
    </style>
    
    <%
      final Object statusBarHtml = request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.statusBarHtml");
      if (statusBarHtml != null) {
    %>
      <%= statusBarHtml %>
    <%
      }
      final Object statusBarStyle = request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.statusBarStyle");
      if (statusBarStyle != null) {
    %>
      <style type="text/css">
        <%= statusBarStyle %>
      </style>
    <%
      } 
    %>
    
    <div id="table-container">
      <%= request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableHtml") %>
    </div>
    
    <div id="table-new-row-template-container" style="display: none;">
      <%= request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableNewRowTemplateHtml") %>
    </div>
    
    <%= request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.controlButtonsHtml") %>
    <script type="text/javascript">
      <%= request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.controlButtonsScript") %>
    </script>
    <script type="text/javascript">
      function getSsrUrlBase() {
        return "jdbc";
      }
      function getSsrUrlMod() {
        return "jdbc?mod"; // as in web.xml mapping
      }
      function getSsrUrlReset() {
        return "jdbc?mod-reset"; // as in web.xml mapping
      }
      <%= request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableScript") %>
    </script>
    
    <style type="text/css">
      <%= request.getAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableStyle") %>
    </style>
  </body>
</html>
