package org.jepria.httpd.apache.manager.web.files;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.core.ApacheConfBase;
import org.jepria.httpd.apache.manager.core.jk.TextLineReference;
import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;

public class FilesApiServlet extends HttpServlet{
  
  private static final long serialVersionUID = -3405808908421946460L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
    
    response.setContentType("text/plain; charset=UTF-8");
    
    String path = req.getPathInfo();
    
    if ("/conf/jk/mod_jk.conf".equals(path)) {
      
      try {
        
        Environment environment = EnvironmentFactory.get(req);
        
        ApacheConfBase apacheConf = new ApacheConfBase(
            () -> environment.getMod_jk_confInputStream(), 
            () -> environment.getWorkers_propertiesInputStream());
        
        
        List<TextLineReference> lines = apacheConf.getMod_jk_confLines();
        
        if (lines != null) {
          for (TextLineReference line: lines) {
            response.getWriter().println(line.getContent());
          }
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
        return;
        
      } catch (Throwable e) {
        e.printStackTrace();

        response.getOutputStream().println("Oops! Something went wrong.");//TODO
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.flushBuffer();
        return;
      }

    } else if ("/conf/jk/workers.properties".equals(path)) {
      
      try {
        
        Environment environment = EnvironmentFactory.get(req);
        
        ApacheConfBase apacheConf = new ApacheConfBase(
            () -> environment.getMod_jk_confInputStream(), 
            () -> environment.getWorkers_propertiesInputStream());
        
        
        List<TextLineReference> lines = apacheConf.getWorkers_propertiesLines();
        
        if (lines != null) {
          for (TextLineReference line: lines) {
            response.getWriter().println(line.getContent());
          }
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
        return;
        
      } catch (Throwable e) {
        e.printStackTrace();

        response.getOutputStream().println("Oops! Something went wrong.");//TODO
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.flushBuffer();
        return;
      }

    } else {
      
      // TODO set content type for the error case?
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      response.flushBuffer();
      return;
    }
  }
}
