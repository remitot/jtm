<% 
  request.getRequestDispatcher("login.jsp?error").include(request, response);
  response.setStatus(401); 
%>