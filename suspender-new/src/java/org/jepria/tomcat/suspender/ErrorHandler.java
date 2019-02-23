package org.jepria.tomcat.suspender;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorHandler {
  
  public static void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    System.out.println("///req:"+req.getClass().getName());
    System.out.println("///fwd:"+req.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI));
    System.out.println("///fwdQs:"+req.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING));
    req.getServletContext().getContext("").getRequestDispatcher(arg0)
//    resp.sendRedirect("http://google.com"); // working
  }
  
}
