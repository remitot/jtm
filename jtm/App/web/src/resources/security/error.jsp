<%@ page language="java"%>
<%
  String requestUri = (String)request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
  if (requestUri != null) {
    String contextPath = request.getContextPath();
    if (contextPath != null) {
      int index = requestUri.indexOf(contextPath);
      if (index != -1) {
        String path = requestUri.substring(index + contextPath.length());
      
        if ("/log-monitor-entry".equals(path)) {
          request.getRequestDispatcher("/log-monitor/log-monitor-error.jsp").include(request, response);
        }
      }
    }
  }
%>