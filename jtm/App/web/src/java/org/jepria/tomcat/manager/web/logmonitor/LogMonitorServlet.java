package org.jepria.tomcat.manager.web.logmonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;

public class LogMonitorServlet extends HttpServlet {

  private static final long serialVersionUID = -4404438014956108762L;
  
  private static class MonitorResultDto {
    public List<String> contentLinesBeforeAnchor;
    public List<String> contentLinesAfterAnchor;
  }
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
 // the content type is defined for the entire method
    response.setContentType("text/html; charset=UTF-8");
    
    
    // 'filename' request parameter
    final String filename = request.getParameter("filename");
    if (filename == null) {
      // no log file specified for monitoring
      out(response, "no log file specified for monitoring");// TODO gui
      return;
      
    } else if (!filename.matches("[^/\\\\]+")) {
      // invalid 'filename' value
      out(response, "400: invalid 'filename' value");// TODO gui
      return;
      
    } else {
      final String anchorStr = request.getParameter("anchor");
      
      //////////////////////////////  
      
      // 'lines' request parameter
      final int lines;
      String linesStr = request.getParameter("lines");
      if (linesStr != null) {
        try {
          lines = Integer.parseInt(linesStr);
        } catch (java.lang.NumberFormatException e) {
          response.sendError(400); return; // TODO GUI
        }
      } else {
        lines = FRAME_SIZE;
      }
      
      
      //////////////////////////////

      final int anchor;  
      if (anchorStr == null) {
        // anchor-undefined (initial) monitor request
      
        anchor = getAnchorLine(request, filename);
        // TODO handle Exceptions

        response.sendRedirect("?filename=" + filename 
            + "&anchor=" + anchor + "&lines=" + lines);
        response.flushBuffer();
        return;
        
      } else {
        // anchor-defined (repetitive) monitor request
        
        try {
          anchor = Integer.parseInt(anchorStr);//TODO validate anchor value (int range)
        } catch (NumberFormatException e) {
          // invalid 'anchor' value
          out(response, "400: invalid 'anchor' value");// TODO gui
          return;
        }
        
        MonitorResultDto monitor = monitor(request, filename, anchor, lines);
        // TODO handle Exceptions
        
        List<String> contentLinesBeforeAnchor = monitor.contentLinesBeforeAnchor;
        List<String> contentLinesAfterAnchor = monitor.contentLinesAfterAnchor;
        
        String loadMoreLinesUrl = request.getRequestURL().toString()
            + "?filename=" + filename
            + "&anchor=" + anchor
            + "&lines=" + (lines + FRAME_SIZE);

        request.setAttribute("contentLinesBeforeAnchor", contentLinesBeforeAnchor);
        request.setAttribute("contentLinesAfterAnchor", contentLinesAfterAnchor);
        request.setAttribute("loadMoreLinesUrl", loadMoreLinesUrl);
        
        request.getRequestDispatcher("log-monitor/log-monitor.jsp").include(request, response);
        
        return;
      } 
    }
  }
  
  //TODO this value is assumed. But how to determine it? 
  
  private static final int FRAME_SIZE = 100; //TODO extract?
  
  
  //TODO this value is assumed. But how to determine it? 
  private static final String LOG_FILE_READ_ENCODING = "UTF-8";
  
  public static int getAnchorLine(HttpServletRequest request,
      String filename) throws FileNotFoundException {
    
    try {

      Environment environment = EnvironmentFactory.get(request);

      File logsDirectory = environment.getLogsDirectory();

      Path logFile = logsDirectory.toPath().resolve(filename);

      int lineCount = 0;

      try (Scanner sc = new Scanner(logFile.toFile(), LOG_FILE_READ_ENCODING)) {
        while (sc.hasNextLine()) {
          sc.nextLine();
          lineCount++;
        }
      } catch (FileNotFoundException e) {
        throw e;
      }// TODO catch also non-readable file excepiton

      
      return lineCount > 0 ? lineCount - 1 : 0;

    } catch (Throwable e) {
      e.printStackTrace();

      throw new RuntimeException(e);
    }
  }
  
  private static void out(HttpServletResponse response, String str) 
      throws IOException {
    response.getWriter().println(str);
  }
  
  /**
   * @param filename
   * @param anchor index of the anchor line in the file 
   * (index of the last line loaded on the {@link #initMonitor} request), from 0 
   * @param lines total number of lines to load (counting back from the anchor, including it)
   * @return
   */
  public static MonitorResultDto monitor(HttpServletRequest request,
      String filename, int anchor, int lines) 
          throws FileNotFoundException {

    try {

      Environment environment = EnvironmentFactory.get(request);

      File logsDirectory = environment.getLogsDirectory();

      Path logFile = logsDirectory.toPath().resolve(filename);

      LinkedList<String> contentLinesBeforeAnchor = new LinkedList<>();
      List<String> contentLinesAfterAnchor = new LinkedList<>();

      int lineIndex = 0;

      try (Scanner sc = new Scanner(logFile.toFile(), LOG_FILE_READ_ENCODING)) {
        while (sc.hasNextLine()) {
          final String line = sc.nextLine();
          
          if (lineIndex <= anchor) {
            contentLinesBeforeAnchor.add(line);
            
            if (contentLinesBeforeAnchor.size() > lines) {
              contentLinesBeforeAnchor.removeFirst();
            }
          } else {
            contentLinesAfterAnchor.add(line);
          }

          lineIndex++;
        }
      } catch (FileNotFoundException e) {
        throw e;
      }// TODO catch also non-readable file excepiton


      MonitorResultDto ret = new MonitorResultDto();
      ret.contentLinesBeforeAnchor = contentLinesBeforeAnchor;
      ret.contentLinesAfterAnchor = contentLinesAfterAnchor;
      
      return ret;

    } catch (Throwable e) {
      e.printStackTrace();

      throw new RuntimeException(e);
    }
  }
  
}
