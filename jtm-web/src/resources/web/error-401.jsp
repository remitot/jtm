<%@ page language="java" pageEncoding="utf-8"%>
<%@ page import="org.jepria.tomcat.manager.web.jdbc.JdbcHtmlPageUnauthorized"%>
<%@ page import="org.jepria.tomcat.manager.web.HtmlPage"%>
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
          
          final boolean loginAlreadyFailed;
          if (request.getSession().getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.servletLoginStatus.failure") != null) {
            loginAlreadyFailed = true;
          } else {
            loginAlreadyFailed = false;
          }
          
          // reset the servlet login status after the first request
          request.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.servletLoginStatus.failure");
          
          
          HtmlPage htmlPage = new JdbcHtmlPageUnauthorized(request, loginAlreadyFailed);
          
          response.setContentType("text/html; charset=UTF-8");
          htmlPage.render(response.getWriter());
          response.flushBuffer();
          return;
        }
      }
    }
  }
%>