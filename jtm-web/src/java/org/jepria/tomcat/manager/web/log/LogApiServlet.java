package org.jepria.tomcat.manager.web.log;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;

public class LogApiServlet extends HttpServlet {

  private static final long serialVersionUID = 5891799566737744116L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String path = req.getPathInfo();
    
    if (path == null || "/".equals(path) || "".equals(path)) {
      fileContents(req, resp);
      return;
      
    } else {
      // unknown request
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported request path [" + path + "]");
      resp.flushBuffer();
      return;
    }
  }
  
  /**
   * @param request
   * @param response
   * @param filename
   * @throws IOException
   */
  protected void fileContents(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    
    // the content type is defined for the entire method
    response.setContentType("text/plain; charset=UTF-8");
    
    
    // 'filename' request parameter
    final String filename = request.getParameter("filename");
    if (filename == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The 'filename' request parameter is mandatory");
      response.flushBuffer();
      return;
    }
    if (filename.contains("/") || filename.contains("\\")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The 'filename' request parameter value [" + filename + "] must not contain '/' or '\\' character");
      response.flushBuffer();
      return;
    }

    
    // 'inline' request parameter
    if (request.getParameter("inline") != null) {
      response.setHeader("Content-Disposition", "inline") ;
    } else {
      response.setHeader("Content-Disposition", "attachment; filename=" + filename) ;
    }

    
    try {
      
      Environment environment = EnvironmentFactory.get(request);

      final List<String> fileContents;
      
      try {
        fileContents = new LogApi().readFileLines(environment, filename);
        
      } catch (NoSuchFileException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No such file found [" + filename + "]");
        response.flushBuffer();
        return;
      }
      
      if (fileContents != null) {
        for (String line: fileContents) {
          response.getWriter().println(line);
        }
      }
      
      response.setStatus(HttpServletResponse.SC_OK);
      response.flushBuffer();
      return;
      
    } catch (Throwable e) {
      final String errorId = String.valueOf(System.currentTimeMillis());

      synchronized (System.err) {
        System.err.println("Error ID [" + errorId + "]:");
        e.printStackTrace();
      }

      // response body must either be empty or match the declared content type
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error ID [" + errorId + "]");
      response.flushBuffer();
      return;
    }
  }
}
