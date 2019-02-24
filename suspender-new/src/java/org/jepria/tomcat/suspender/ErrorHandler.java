package org.jepria.tomcat.suspender;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorHandler {
  
  public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // validate the status (deal with 404 only)
    if (response.getStatus() != HttpServletResponse.SC_NOT_FOUND) {
      return;
    }
    
    
    final String requestUri = (String)request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
    final String requestQs = (String)request.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING);
    
    if (requestUri == null) {
      // TODO what it means?
      throw new IllegalStateException();
    }
    
    
    final String appUnsus = mapToAppUnsuspended(requestUri);
    final String appSus = mapToAppSuspended(requestUri);

    // priority the appContext
    final String app;
    if (appSus != null) {
      if (appUnsus != null) {
        if (appSus.length() > appUnsus.length()) {
          // found both apps: suspended and unsuspended, 
          // prefer suspended because of the stricter (longer) match
          app = appSus;
        } else {
          // found both apps: suspended and unsuspended,
          // prefer unsuspended because of the stricter (longer) match
          app = appUnsus;
        }
      } else {
        // no unsuspended app found, but found suspended app
        app = appSus;
      }
    } else {
      if (appUnsus != null) {
        // no suspended app found, but found unsuspended app
        app = appUnsus;
      } else {
        // neither suspended nor unsuspended app found
        app = null;
      }
    }
    
    
    if (app != null && app == appSus) {// refs equals
      // unsuspend the suspended app
      
      if (!unsuspend(app)) {
        // TODO do not throw but log and confirm not found
        throw new RuntimeException("Failed to unsuspend app: " + app);
      }
      
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      // the app successfully unsuspended, redirect
      response.sendRedirect(requestUri + (requestQs == null ? "" : ("?" + requestQs)));
    } else {
      
      confirmNotFound(response);
    }
  }
  
  protected String mapToAppUnsuspended(String uri) {
    return WebappsFactory.get().mapToAppUnsuspended(uri);
  }
  
  protected String mapToAppSuspended(String uri) {
    return WebappsFactory.get().mapToAppSuspended(uri);
  }

  protected boolean unsuspend(String application) {
    return SuspenderFactory.get().unsuspend(application);
  }
  
  protected void confirmNotFound(HttpServletResponse response) throws IOException {
    // TODO print the default error page 
    // @see default error page at: org.apache.catalina.valves.ErrorReportValve#report(Request, Response, Throwable)
    response.getWriter().println("<!DOCTYPE><html><head><title>Not found</title></head><body><h1>Still 404</h1></body></html>");
    response.flushBuffer();
    // TODO the following does not work on the client (receives empty response)
//    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
  
}
