<%@ page language="java" pageEncoding="utf-8"%>
<%@ page import="org.jepria.tomcat.manager.web.jdbc.JdbcHtmlPageUnauthorized"%>
<%@ page import="org.jepria.tomcat.manager.web.HtmlPage"%>
<%@ page import="org.jepria.web.ssr.Status"%>
<%@ page import="org.jepria.web.ssr.StatusBar"%>
<%
  String requestUri = (String)request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
  if (requestUri != null) {
    String contextPath = request.getContextPath();
    if (contextPath != null) {
      int index = requestUri.indexOf(contextPath);
      if (index != -1) {
        final String path = requestUri.substring(index + contextPath.length());
        if (path.equals("/log-monitor") || path.startsWith("/log-monitor/")) {
          request.getRequestDispatcher("/gui/log-monitor/log-monitor-error-401.jsp").forward(request, response);
        } else 
        if (path.equals("/jdbc") || path.startsWith("/jdbc/")) {
          
          HtmlPage htmlPage = new JdbcHtmlPageUnauthorized(request);
          
          final Status pageStatus = (Status)request.getSession().getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.pageStatus");
          if (pageStatus != null) {
            final StatusBar statusBar = new StatusBar(pageStatus.type, pageStatus.statusHTML);
            htmlPage.setStatusBar(statusBar);
          }
          
          // reset the page status after the first request
          request.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.pageStatus");
          
          htmlPage.respond(response);
          return;
        }
      }
    }
  }
%>