<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.util.List" %>
<!DOCTYPE html> 
<html> 
  <head> 
    <title>Tomcat manager: логи</title> <!-- NON-NLS --> 
  
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" /> 
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 

    <link rel="stylesheet" href="jtm.css">
    <script type="text/javascript" src="jtm.js"></script>
    
    <link rel="stylesheet" href="log-monitor/log-monitor.css">
  </head> 

  <%
    // read all attributes
    final boolean fileBeginReached = Boolean.TRUE.equals(request.getAttribute("fileBeginReached"));
    final List<String> contentLinesBeforeAnchor = (List<String>) request.getAttribute("contentLinesBeforeAnchor");
    final List<String> contentLinesAfterAnchor = (List<String>) request.getAttribute("contentLinesAfterAnchor");
    final String loadMoreLinesUrl = (String)request.getAttribute("loadMoreLinesUrl");
    final String resetAnchorUrl = (String)request.getAttribute("resetAnchorUrl");
    //
    
    boolean isLinesBefore = contentLinesBeforeAnchor != null && !contentLinesBeforeAnchor.isEmpty();
    boolean isLinesAfter = contentLinesAfterAnchor != null && !contentLinesAfterAnchor.isEmpty();
  %>

  <body onload="logmonitor_onload();">
    
    <div class="anchor-area">
    <% if (isLinesBefore) { %>
      <div class="anchor-area__panel top">&nbsp;</div>
    <% } %>
    <% if (isLinesAfter) { %>
      <div class="anchor-area__panel bottom">&nbsp;</div>
    <% } %>
    </div>
    
    <div class="content-area">
    <% if (isLinesBefore) { %>
      <div class="content-area__lines top">
        <%
          for (String line: contentLinesBeforeAnchor) {
            out.println(line + "<br/>");      
          }
        %>
      </div>
    <% } %>
    <% if (isLinesAfter) { %>
      <div class="content-area__lines bottom">
        <%
          for (String line: contentLinesAfterAnchor) {
            out.println(line + "<br/>");      
          }
        %>
      </div>
    <% } %>
    
    <% if (isLinesAfter && resetAnchorUrl != null) { %>
      <button 
          onclick="resetAnchor();" 
          class="control-button_reset-anchor control-button big-black-button hidden"
          title="Снять подсветку с новых записей"
          >ПРОЧИТАНО</button> <!-- NON-NLS -->
          
    <% } %>
    </div>
    
    <script type="text/javascript">

      var linesTop = document.querySelectorAll(".content-area__lines.top")[0];
    
      function getSplitY() {
        return linesTop.offsetTop + linesTop.clientHeight;
      }
      
      /** 
       * Returns scroll offset (the viewport position) from the bottom of the page, in pixels 
       */ 
      function getOffset() { 
        var offset = window.location.hash.substring(1); 
        if (offset) { 
          return Number(offset); 
        } else { 
          return null; 
        } 
      } 
      
      function logmonitor_onload() { 
        /* scroll to the offset */ 
        
        var offset = getOffset(); 
        if (offset) { 
          scrTo(getSplitY() - offset); 
        } else {
          contentArea = document.getElementsByClassName("content-area")[0];
          if (contentArea.clientHeight <= window.innerHeight) {
            if (contentArea.clientHeight > 0) {
              scrTo(1); 
            }
          } else { 
            scrTo(contentArea.clientHeight - window.innerHeight); 
          } 
        }
        
        
        /* set anchor-area size */
        var anchorAreaTop = document.querySelectorAll(".anchor-area__panel.top")[0];
        anchorAreaTop.style.height = linesTop.clientHeight + "px";
        
        var anchorAreaBottom = document.querySelectorAll(".anchor-area__panel.bottom")[0];
        if (anchorAreaBottom) {
          var linesBottom = document.querySelectorAll(".content-area__lines.bottom")[0];
          anchorAreaBottom.style.height = linesBottom.clientHeight + "px";
        }
        
        
        addHoverForBigBlackButton(document.getElementsByClassName("big-black-button")[0]);
        
      } 
      
      
      function scrTo(y) { 
        if (document.body.scrollHeight < window.innerHeight + y) { 
          /* adjust scrollHeight */ 
          document.body.style.height = (window.innerHeight + y) + "px"; 
        } 
        window.scrollTo(0, y); 
      } 
      
      function getDocHeight() {
        return Math.max(
            document.body.scrollHeight, document.documentElement.scrollHeight,
            document.body.offsetHeight, document.documentElement.offsetHeight,
            document.body.clientHeight, document.documentElement.clientHeight
        );
      }
      
      window.onscroll = function() { 
        var scrolled = window.pageYOffset || document.documentElement.scrollTop; 
        
        var offset = getSplitY() - scrolled; 
        window.location.hash = "#" + offset; 
        
        if (scrolled <= linesTop.offsetTop) { 
          /* top reached */
          
        <% if (!fileBeginReached && loadMoreLinesUrl != null) { %>
          /* because location.reload() not wotking in FF and Chrome */ 
          window.location.href = "<% out.print(loadMoreLinesUrl); %>" + "#" + offset;
        <% } %>
        } 
        
        <% if (isLinesAfter) { %>
        if (scrolled + window.innerHeight == getDocHeight()) {
          document.getElementsByClassName("control-button_reset-anchor")[0].style.display = "block";
        } else {
          document.getElementsByClassName("control-button_reset-anchor")[0].style.display = "none";
        }
        <% } %>
      }
      
      <% if (isLinesAfter && resetAnchorUrl != null) { %>
      function resetAnchor() {
        var offset = getOffset() + document.querySelectorAll(".content-area__lines.bottom")[0].clientHeight;
        window.location.hash = "#" + offset;
        
        /* because location.reload() not wotking in FF and Chrome */
        window.location.href = "<% out.print(resetAnchorUrl); %>" + "#" + offset;
      } 
      <% } %>
    </script>
    
  </body>
</html>