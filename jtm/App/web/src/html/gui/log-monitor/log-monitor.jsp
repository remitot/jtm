<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%@ page import="java.util.List" %>
<%@page import="org.jepria.tomcat.manager.web.logmonitor.MonitorGuiParams"%>
<%@page import="org.jepria.tomcat.manager.web.logmonitor.HtmlEscaper"%>

<!DOCTYPE html> 
<html> 

<%
  // read gui params
  MonitorGuiParams guiParams = (MonitorGuiParams)request.getAttribute("org.jepria.tomcat.manager.web.logmonitor.LogMonitorServlet.monitorGuiParams");
  
  if (guiParams == null) {
    // Probably the page has been accessed directly through the URL, but must have been included from LogMonitorServlet
    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    return;
  }
  
  final boolean hasLinesTop = !guiParams.getContentLinesTop().isEmpty();
  final boolean hasLinesBottom = !guiParams.getContentLinesBottom().isEmpty();
  final boolean canLoadTop = !guiParams.isFileBeginReached() && guiParams.getLoadTopUrl() != null;
  final boolean canResetAnchor = hasLinesBottom && guiParams.getResetAnchorUrl() != null;
%>
  
  <head> 
    <title><% out.print(guiParams.getFilename()); %> — <% out.print(guiParams.getHost()); %></title> <!-- NON-NLS --> 
  
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" /> 
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 

    <link rel="stylesheet" href="gui/jtm.css">
    <script type="text/javascript" src="gui/jtm.js"></script>
    
    <link rel="stylesheet" href="gui/log-monitor/log-monitor.css">
  </head> 

  <body onload="logmonitor_onload();">
    
    <div class="anchor-area">
    <% if (hasLinesTop) { %>
      <div class="anchor-area__panel top">&nbsp;</div>
    <% } %>
    <% if (hasLinesBottom) { %>
      <div class="anchor-area__panel bottom">&nbsp;</div>
    <% } %>
    </div>
    
    <div class="content-area">
    <% if (hasLinesTop) { %>
      <div class="content-area__lines top">
        <%
          for (String line: guiParams.getContentLinesTop()) {
            HtmlEscaper.escapeAndWrite(line, out);
            out.println("<br/>");      
          }
        %>
      </div>
    <% } %>
    <% if (hasLinesBottom) { %>
      <div class="content-area__lines bottom">
        <%
          for (String line: guiParams.getContentLinesBottom()) {
            HtmlEscaper.escapeAndWrite(line, out);
            out.println("<br/>");
          }
        %>
      </div>
    <% } %>
    </div>
    
  <% if (canResetAnchor) { %>
    <button 
        onclick="onResetAnchorButtonClick();" 
        class="control-button_reset-anchor control-button big-black-button hidden"
        title="Снять подсветку с новых записей"
        >ПРОЧИТАНО</button> <!-- NON-NLS --> <!-- NON-NLS -->
        
  <% } %>
    
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
      
      function onResetAnchorButtonClick() {
        var resetAnchorButton = document.getElementsByClassName("control-button_reset-anchor")[0];
        resetAnchorButton.disabled = true;
        resetAnchorButton.title = null;
        
        resetAnchor();
      }
      
      // a little user-visible scroll, px 
      var initialScroll = 3;
      
      function logmonitor_onload() { 
        /* scroll to the offset */ 
        
        var offset = getOffset(); 
        if (offset) {
          scrTo(getSplitY() - offset); 
        } else {
          contentArea = document.getElementsByClassName("content-area")[0];
          if (contentArea.clientHeight <= window.innerHeight) {
            if (contentArea.clientHeight > 0) {
              scrTo(initialScroll);
            }
          } else {
            // scroll to the very bottom
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
        
        
        <% if (canResetAnchor) { %>
        addHoverForBigBlackButton(document.getElementsByClassName("big-black-button")[0]);
        <% } %>
      } 
      
      
      function scrTo(y) {
        if (document.body.scrollHeight <= window.innerHeight + y) {
          // adjust body height to the requested scroll, but at least the initial scroll
          adjustScroll = Math.max(y, initialScroll);
          document.body.style.height = (window.innerHeight + adjustScroll) + "px";
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
          
          <% if (canLoadTop) { %>
          loadTop(offset);
          <% } %>
        
        } 
        
        <% if (canResetAnchor) { %>
        if (scrolled + window.innerHeight == getDocHeight()) {
          document.getElementsByClassName("control-button_reset-anchor")[0].classList.remove("hidden");
        } else {
          document.getElementsByClassName("control-button_reset-anchor")[0].classList.add("hidden");
        }
        <% } %>
      }
      
      
      <% if (canLoadTop) { %>
      // blocks script execution after the first request to prevent repeated client requests if the server hangs up
      blockLoadTop = false;
      
      function loadTop(offset) {
        if (!blockLoadTop) {
          blockLoadTop = true;
          windowReload("<% out.print(guiParams.getLoadTopUrl()); %>" + "#" + offset);
        }
      }
      <% } %>
      
      
      <% if (canResetAnchor) { %>
      // blocks script execution after the first request to prevent repeated client requests if the server hangs up
      blockResetAnchor = false;
   
      function resetAnchor() {
        if (!blockResetAnchor) {
          blockResetAnchor = true;

          var offset = getOffset() + document.querySelectorAll(".content-area__lines.bottom")[0].clientHeight;
          window.location.hash = "#" + offset;// TODO remove this action? (The window will be reloaded immediately anyway)
          windowReload("<% out.print(guiParams.getResetAnchorUrl()); %>" + "#" + offset);
        }
      } 
      <% } %>
    </script>
    
  </body>
</html>