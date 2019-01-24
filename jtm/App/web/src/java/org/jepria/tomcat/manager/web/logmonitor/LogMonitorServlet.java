package org.jepria.tomcat.manager.web.logmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;

public class LogMonitorServlet extends HttpServlet {

  private static final long serialVersionUID = -4404438014956108762L;
  
  private static class MonitorResultDto {
    public final List<String> contentLinesTop;
    public final List<String> contentLinesBottom;
    public final boolean fileBeginReached;
    
    public MonitorResultDto(List<String> contentLinesTop, List<String> contentLinesBottom,
        boolean fileBeginReached) {
      this.contentLinesTop = contentLinesTop;
      this.contentLinesBottom = contentLinesBottom;
      this.fileBeginReached = fileBeginReached;
    }
  }
  
  private static final long LOAD_LIMIT = 1000000;
  
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
        
        if (lines < 1) {
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
        
        
        
        final URL url = new URL(request.getRequestURL().toString());
        final String host = url.getHost() + (url.getPort() == 80 ? "" : (":" + url.getPort()));
            
        final MonitorResultDto monitor = monitor(request, filename, anchor, lines);
        // TODO handle Exceptions
        
        final String loadMoreLinesUrl = request.getRequestURL().toString()
            + "?filename=" + filename
            + "&anchor=" + anchor
            + "&lines=" + (lines + FRAME_SIZE);

        final String resetAnchorUrl;
        if (monitor.contentLinesBottom != null && monitor.contentLinesBottom.size() > 0) {
          
          // increase by number of bottom lines
          final int newAnchor = anchor + monitor.contentLinesBottom.size();
          final int newLines = lines + monitor.contentLinesBottom.size(); 
          
          resetAnchorUrl = request.getRequestURL().toString()
              + "?filename=" + filename
              + "&anchor=" + newAnchor
              + "&lines=" + newLines;
        } else {
          resetAnchorUrl = null;
        }
        

        // set gui params for including jsp
        MonitorGuiParams monitorGuiParams = new MonitorGuiParams(
            filename, 
            host, 
            monitor.contentLinesTop, 
            monitor.contentLinesBottom,
            monitor.fileBeginReached, 
            loadMoreLinesUrl, 
            resetAnchorUrl);
        
        request.setAttribute("org.jepria.tomcat.manager.web.logmonitor.LogMonitorServlet.monitorGuiParams", 
            monitorGuiParams);
        
        request.getRequestDispatcher("log-monitor/log-monitor.jsp").include(request, response);
        
        return;
      } 
    }
  }
  
  //TODO this value is assumed. But how to determine it? 
  
  private static final int FRAME_SIZE = 200; //TODO extract?
  
  
  //TODO this value is assumed. But how to determine it? 
  private static final String LOG_FILE_READ_ENCODING = "UTF-8";
  
  public static int getAnchorLine(HttpServletRequest request,
      String filename) throws FileNotFoundException {
    
    try {

      Environment environment = EnvironmentFactory.get(request);

      File logsDirectory = environment.getLogsDirectory();

      Path logFile = logsDirectory.toPath().resolve(filename);

      int lineCount = 0;

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile.toFile()), LOG_FILE_READ_ENCODING))) {
        while (reader.readLine() != null) {
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
   * @param lines > 0, total number of lines to load (counting back from the anchor, including it)
   * @return
   */
  public static MonitorResultDto monitor(HttpServletRequest request,
      String filename, int anchor, int lines) 
          throws FileNotFoundException {

    if (lines < 1) {
      throw new IllegalArgumentException();
    }
    
    try {

      Environment environment = EnvironmentFactory.get(request);

      File logsDirectory = environment.getLogsDirectory();

      Path logFile = logsDirectory.toPath().resolve(filename);

      boolean fileBeginReached = true;
      LinkedList<String> contentLinesTop = new LinkedList<>();
      List<String> contentLinesBottom = new LinkedList<>();
      
      // total char count
      long charCount = 0;
      
      int lineIndex = 0;
      
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile.toFile()), LOG_FILE_READ_ENCODING))) {
        String line;
        while ((line = reader.readLine()) != null) {
          
          if (lineIndex <= anchor) {
            contentLinesTop.add(line);
            charCount += line.length();
            
            if (contentLinesTop.size() > lines) {
              String removed = contentLinesTop.removeFirst();
              charCount -= removed.length();
              fileBeginReached = false;
            }
          } else {
            contentLinesBottom.add(line);
            charCount += line.length();
          }

          lineIndex++;
        }
        
        
        // count all newlines as single chars
        if (contentLinesTop.size() > 1) {
          charCount += contentLinesTop.size() - 1;
        }
        if (!contentLinesTop.isEmpty() && !contentLinesBottom.isEmpty()) {
          charCount++;
        }
        if (contentLinesBottom.size() > 1) {
          charCount += contentLinesBottom.size() - 1;
        }
        
        
      } catch (FileNotFoundException e) {
        throw e;
      }// TODO catch also non-readable file excepiton

      
      // check load limit
      if (charCount > LOAD_LIMIT) {
        // TODO return error of crop the load against the limit?
        throw new RuntimeException("Load limit overflow");
      }
      
      
      final MonitorResultDto ret = new MonitorResultDto(
          contentLinesTop,
          contentLinesBottom,
          fileBeginReached);
      
      return ret;

    } catch (Throwable e) {
      e.printStackTrace();

      throw new RuntimeException(e);
    }
  }
  
}
