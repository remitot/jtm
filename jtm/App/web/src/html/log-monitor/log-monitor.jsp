<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jepria.tomcat.manager.web.logmonitor.LogMonitorStaticJavaApi" %>

<!DOCTYPE html>
<html>
  <head>
    <title>Tomcat manager: логи</title> <!-- NON-NLS -->
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 
    
    <link rel="stylesheet" href="log-monitor/log-monitor.css">
    <script type="text/javascript" src="log-monitor/log-monitor.js"></script>
    
  </head>
  
  <body onload="logmonitor_onload();">
<% 
// 'filename' request parameter
final String filename = request.getParameter("filename");
if (filename == null || !filename.matches("[^/\\\\]+")) {
  // no log file specified for monitoring
%>
    no log file specified for monitoring
    
    <script type="text/javascript">
      function loadNext() {}
    </script>
<%  
} else {
  final String anchorStr = request.getParameter("anchor");
  
  int LINES = 10;//TODO
  
  int lines;
  String linesStr = request.getParameter("lines");
  if (linesStr != null) {
    try {
      lines = Integer.parseInt(linesStr);
    } catch (java.lang.NumberFormatException e) {
      response.sendError(400); return; // TODO GUI
    }
  } else {
    lines = LINES;
  }
  
  
  int bufferSize = 10;//TODO
  

  int anchor;  
  if (anchorStr == null) {
  
    int[] anchorRef = new int[1];
    
    List<String> contentLines = LogMonitorStaticJavaApi.initMonitor(
        request, filename, LINES, anchorRef);
    // TODO handle Exceptions
    
    anchor = anchorRef[0];
    
    for (String line: contentLines) {
      out.println("<label>" + line + "</label><br/>");//TODO      
    }
    
  } else {
  
    try {
      anchor = Integer.parseInt(anchorStr);
    } catch (java.lang.NumberFormatException e) {
      response.sendError(400); return; // TODO GUI
    }
    
    List<String> contentLines = LogMonitorStaticJavaApi.monitor(
      request, filename, anchor, lines);
    // TODO handle Exceptions
    for (String line: contentLines) {
      out.println("<label>" + line + "</label><br/>");//TODO      
    } 
  }

  
  String url = "http://localhost:8081/jtm/log-monitor?"
      + "filename=" + filename
      + "&anchor=" + anchor
      + "&lines=" + (lines + bufferSize);
      
%>
    <script type="text/javascript">
      function loadNext() {
        // because location.reload() not wotking in FF and Chrome
        window.location.href = "<% out.print(url); %>"
            + "#" + document.body.scrollHeight;
      }
    </script>
<%
}
%>

  </body>
</html>
