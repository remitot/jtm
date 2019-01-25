<%@ page language="java" contentType="text/html; charset=utf-8"%>
<%
  String requestUri = (String)request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
  if (requestUri != null) {
    String contextPath = request.getContextPath();
    if (contextPath != null) {
      int index = requestUri.indexOf(contextPath);
      if (index != -1) {
        String path = requestUri.substring(index + contextPath.length());
      
        if ("/log-monitor".equals(path)) {
          %>
            <html>
              <head>
              </head>
              <body>
                <h1>Unauthorized</h1>
                <h2>Only authorized users can monitor logs.<br/>
                <a href="log" target="_blank">Login</a> then reload this page to continue monitoring.</h2>
              </body>
            </html>  
          <%
        }
      }
    }
  }
  
  response.setStatus(401);
%>