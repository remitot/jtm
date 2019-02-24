<%@ page import="org.jepria.tomcat.suspender.ErrorHandler" %>
<%
	new ErrorHandler().handle(request, response);
%>