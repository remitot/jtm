<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jepria.tomcat.manager.web.logmonitor.LogMonitorStaticJavaApi" %>
<%@ page import="org.jepria.tomcat.manager.web.logmonitor.InitMonitorResultDto" %>
<%@ page import="org.jepria.tomcat.manager.web.logmonitor.MonitorResultDto" %>

<!DOCTYPE html>
<html>
  <head>
    <title>Tomcat manager: логи</title> <!-- NON-NLS -->
    
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 
    
    <link rel="stylesheet" href="log-monitor/log-monitor.css">
    
  </head>
  
  <body onload="logmonitor_onload();">
<% 
// 'filename' request parameter
final String filename = request.getParameter("filename");

if (filename == null) {
  // no log file specified for monitoring
  
  %>TODO no log file specified for monitoring<%
    
} else  if (!filename.matches("[^/\\\\]+")) {
  // invalid 'filename' value
  
  %>TODO invalid 'filename' value<%
  
} else {
  final String anchor = request.getParameter("anchor");
  

  // TODO maybe to forward to another jsp?

  //////////////////////////////  
  
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
  
  //////////////////////////////

  List<String> contentLinesBeforeAnchor = null;
  List<String> contentLinesAfterAnchor = null;

  int anchorLine;  
  if (anchor == null) {
    // anchor-undefined (initial) monitor request
  
    InitMonitorResultDto monitor = LogMonitorStaticJavaApi.initMonitor(
        request, filename, lines);
    // TODO handle Exceptions

    // define the anchor    
    anchorLine = monitor.getAnchorLine();
    
    contentLinesBeforeAnchor = monitor.getContentLinesBeforeAnchor();
    
  } else {
    // anchor-defined (repetitive) monitor request
    
    try {
      anchorLine = Integer.parseInt(anchor);//TODO validate anchorLine value
    } catch (java.lang.NumberFormatException e) {
      response.sendError(400); return; // TODO GUI
    }
    
    MonitorResultDto monitor = LogMonitorStaticJavaApi.monitor(
        request, filename, anchorLine, lines);
    // TODO handle Exceptions
    
    contentLinesBeforeAnchor = monitor.getContentLinesBeforeAnchor();
    contentLinesAfterAnchor = monitor.getContentLinesAfterAnchor();
  }
  
%>
    <div id="content">
      <div id="linesBeforeAnchor" class="lines">
<%
  if (contentLinesBeforeAnchor != null) {
    for (String line: contentLinesBeforeAnchor) {
      out.println("<label>" + line + "</label><br/>");//TODO      
    }
  }
%>
      </div>
      <div class="lines">
<%
  if (contentLinesAfterAnchor != null) {
    for (String line: contentLinesAfterAnchor) {
      out.println("<label style=\"color: green;\">" + line + "</label><br/>");//TODO      
    }
  }
%>
      </div>
    </div>
<%    
  
  String url = "http://localhost:8081/jtm/log-monitor?"
      + "filename=" + filename
      + "&anchor=" + anchorLine
      + "&lines=" + (lines + bufferSize);
      
%>
    <script type="text/javascript">
      var linesBeforeAnchor = document.getElementById("linesBeforeAnchor");
      
      /**
       * Returns scroll offset (the viewport position) from the bottom of the page, in pixels
       */
      function getOffset() {
        var offset = window.location.hash.substring(1);
        if (offset) {
          return offset;
        } else {
          return null;
        }
      }
      
      function logmonitor_onload() {
        // scroll to the offset
        
        var offset = getOffset();
        if (offset) {
          scrTo(linesBeforeAnchor.clientHeight - offset);
        } else {
          if (document.getElementById("content").clientHeight <= window.innerHeight) {
            scrTo(10);
          } else {
            scrTo(document.getElementById("content").clientHeight - window.innerHeight);
          }        
        }
      }
      
      
      function scrTo(y) {
        if (document.body.scrollHeight < window.innerHeight + y) {
          // adjust scrollHeight
          document.body.style.height = (window.innerHeight + y) + "px";
        }
        window.scrollTo(0, y);
      }

    
      window.onscroll = function() {
        var scrolled = window.pageYOffset || document.documentElement.scrollTop;

        var offset = linesBeforeAnchor.clientHeight - scrolled;    
             
        window.location.hash = "#" + offset; 
        
        if (scrolled == 0) {
          // top reached
          
          // because location.reload() not wotking in FF and Chrome
          window.location.href = "<% out.print(url); %>"
             + "#" + offset;
        }
      }
    </script>
<%
}
%>

  </body>
</html>
