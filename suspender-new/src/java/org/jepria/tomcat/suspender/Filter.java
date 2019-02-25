package org.jepria.tomcat.suspender;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.suspender.core.SuspendProcessor;
import org.jepria.tomcat.suspender.core.SuspenderFactory;
import org.jepria.tomcat.suspender.core.SuspendProcessor.Result;

public class Filter implements javax.servlet.Filter {

  @Override
  public void init(FilterConfig config) throws ServletException {
  }
  
  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    
    final HttpServletRequest request0 = (HttpServletRequest)request;
    
    final String uri = request0.getRequestURI();
//    final String qs = request0.getQueryString();

    
    process(uri);
    
    
    final HttpServletRequest request1 = new HttpServletRequestWrapper(request0) {
      @Override
      public ServletContext getServletContext() {
        final ServletContext servletContext0 = super.getServletContext();
        
        return new ServletContextWrapper(servletContext0) {
          @Override
          public ServletContext getContext(String uripath) {
            
            process(uripath);
            
            return super.getContext(uripath);
          }
        };
      }
    };
    
    chain.doFilter(request1, response);
  }

  //TODO can the query string be passed here?
  protected void process(String uri) {
    SuspendProcessor sp = new SuspendProcessor(uri);
    
    if (sp.getResult() == Result.FOUND_SUSPENDED || sp.getResult() == Result.FOUND_BOTH_PRIOR_SUSPENDED) {
      unsuspend(sp.getApp());
    }
  }
  
  protected void unsuspend(String app) {
    if (!SuspenderFactory.get().unsuspend(app)) {
      // TODO do not throw but log and confirm not found
      throw new RuntimeException("Failed to unsuspend app: " + app);
    }
  }
  
//    public void onNotFound() {
//      try {
//        // TODO print the default error page 
//        // @see default error page at: org.apache.catalina.valves.ErrorReportValve#report(Request, Response, Throwable)
//        response.getWriter().println("<!DOCTYPE><html><head><title>Not found</title></head><body><h1>Still 404</h1></body></html>");
//        response.flushBuffer();
//        
//        // TODO the following does not work on the client (receives empty response)
//        response.sendError(HttpServletResponse.SC_NOT_FOUND);
//        
//      } catch (IOException e) {
//        // TODO
//        throw new RuntimeException(e);
//      }
//    }
}
