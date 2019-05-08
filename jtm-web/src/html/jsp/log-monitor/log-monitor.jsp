<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%@ page import="java.util.List" %>
<%@page import="org.jepria.tomcat.manager.web.logmonitor.MonitorGuiParams"%>
<%@page import="org.jepria.web.ssr.HtmlEscaper"%>

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
  final boolean canLoadMore = !guiParams.isFileBeginReached() && guiParams.getLoadTopUrl() != null;
  final boolean canResetAnchor = hasLinesBottom && guiParams.getResetAnchorUrl() != null;
%>
  
  <head> 
    <title><% out.print(guiParams.getFilename()); %> — <% out.print(guiParams.getHost()); %></title> <!-- NON-NLS --> 
  
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" /> 
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 

    <link rel="stylesheet" href="css/jtm-common.css">
    <link rel="stylesheet" href="css/control-buttons.css">
    <script type="text/javascript" src="js/jtm-common.js"></script>
    
    <link rel="stylesheet" href="css/log-monitor/log-monitor.css">
    <script type="text/javascript" src="js/log-monitor/log-monitor.js"></script>
  </head> 

  <body onload="logmonitor_onload();">

    <% if (canLoadMore) { %>
      <button class="control-top control-top__load-more-lines" 
          onclick="onControlTopClick();" 
          >загрузить ещё 500 строк</button>   <!-- NON-NLS -->
    <% } else { %>
      <% if (hasLinesTop || hasLinesBottom) { %>
      <button class="control-top control-top__file-begin-reached" disabled>Начало файла.</button><!-- NON-NLS -->
      <% } else { %>
      <button class="control-top" disabled>Файл пуст.</button> <!-- NON-NLS -->
      <% } %>
    <% } %>
    
    
    <div>      
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
          <% for (String line: guiParams.getContentLinesTop()) {
               HtmlEscaper.escapeAndWrite(line, out);
               out.println("<br/>");      
             } %>            
          </div>
        <% } %>
        <% if (hasLinesBottom) { %>
          <div class="content-area__lines bottom">
          <% for (String line: guiParams.getContentLinesBottom()) {
               HtmlEscaper.escapeAndWrite(line, out);
               out.println("<br/>");
             } %>
          </div>
        <% } %>
      </div>
  
      
      <button 
          onclick="onResetAnchorButtonClick();" 
          class="control-button_reset-anchor control-button big-black-button hidden"
          title="Снять подсветку с новых записей"
          >ПРОЧИТАНО</button> <!-- NON-NLS --> <!-- NON-NLS -->
    
    </div>
        
    
    <script type="text/javascript">

      // constants for using in log-monitor.js
      var logmonitor_linesTop = document.querySelectorAll(".content-area__lines.top")[0];
      var logmonitor_linesBottom = document.querySelectorAll(".content-area__lines.bottom")[0];
      
      var logmonitor_loadMoreLinesUrl = "<%= guiParams.getLoadTopUrl() %>";
      var logmonitor_canResetAnchor = <%= canResetAnchor %>;
      var logmonitor_resetAnchorUrl = "<%= guiParams.getResetAnchorUrl() %>";
      
    </script>
    
  </body>
</html>