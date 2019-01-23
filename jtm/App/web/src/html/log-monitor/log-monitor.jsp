<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.util.List" %>
<!DOCTYPE html> 
<html> 
  <head> 
    <title>Tomcat manager: логи</title> <!-- NON-NLS --> 
  
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" /> 
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 

    <link rel="stylesheet" href="log-monitor/log-monitor.css">
  </head> 

  <body onload="logmonitor_onload();">
    <div id="content">
      <div class="lines lines_before-anchor">
        <%
        List<String> contentLinesBeforeAnchor = (List<String>)request.getAttribute("contentLinesBeforeAnchor");
        if (contentLinesBeforeAnchor != null) {
          for (String line: contentLinesBeforeAnchor) {
            out.println("<div class=\"hf\">" + line + "</div><br/>");      
          }
        }
        %>
      </div>
      <div class="lines lines_after-anchor">
        <%
        List<String> contentLinesAfterAnchor = (List<String>)request.getAttribute("contentLinesAfterAnchor");
        if (contentLinesAfterAnchor != null) {
          for (String line: contentLinesAfterAnchor) {
            out.println("<div class=\"hf\">" + line + "</div><br/>");      
          }
        }
        %>
      </div>
    </div>

    <script type="text/javascript">
      var linesBeforeAnchor = document.getElementsByClassName("lines_before-anchor")[0];
       
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
        /* scroll to the offset */ 
        
        var offset = getOffset(); 
        if (offset) { 
          scrTo(linesBeforeAnchor.clientHeight - offset); 
        } else {
          content = document.getElementById("content");
          if (content.clientHeight <= window.innerHeight) {
            if (content.clientHeight > 0) {
              scrTo(1); 
            }
          } else { 
            scrTo(document.getElementById("content").clientHeight - window.innerHeight); 
          } 
        } 
      } 
      
      
      function scrTo(y) { 
        if (document.body.scrollHeight < window.innerHeight + y) { 
          /* adjust scrollHeight */ 
          document.body.style.height = (window.innerHeight + y) + "px"; 
        } 
        window.scrollTo(0, y); 
      } 
      
      
      window.onscroll = function() { 
        var scrolled = window.pageYOffset || document.documentElement.scrollTop; 
        
        var offset = linesBeforeAnchor.clientHeight - scrolled; 
        
        window.location.hash = "#" + offset; 
        
        if (scrolled == 0) { 
          /* top reached */ 
          
          /* because location.reload() not wotking in FF and Chrome */ 
          window.location.href = "<%= (String)request.getAttribute("loadMoreLinesUrl") %>" + "#" + offset; 
        } 
      } 
    </script>
    
  </body>
</html>