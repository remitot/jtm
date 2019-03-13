<%@ page language="java" pageEncoding="utf-8"%>
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
        }
      }
    }
  }
%>