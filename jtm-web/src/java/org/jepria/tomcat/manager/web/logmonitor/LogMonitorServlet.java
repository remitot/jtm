package org.jepria.tomcat.manager.web.logmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
  
  // TODO extract?
  private static final long LOAD_LIMIT = 1000000;
  //TODO extract?
  private static final boolean RESET_LINES_ON_ANCHOR_RESET = true;
  //TODO extract?
  private static final int FRAME_SIZE = 200; //TODO extract?
  //TODO this value is assumed. But how to determine it? 
  private static final String LOG_FILE_READ_ENCODING = "UTF-8";
  
  
  private static Reader readFile(HttpServletRequest request, String filename) 
      throws FileNotFoundException {
    
    Environment environment = EnvironmentFactory.get(request);

    File logsDirectory = environment.getLogsDirectory();

    Path logFile = logsDirectory.toPath().resolve(filename);
    
    try {
      return new InputStreamReader(new FileInputStream(logFile.toFile()), LOG_FILE_READ_ENCODING);
    } catch (UnsupportedEncodingException e) {
      // impossible
      throw new RuntimeException(e);
    }// TODO catch also non-readable file excepiton (e.g. permission denied)
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
      
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      response.flushBuffer();
      return;
      
    } else if (!filename.matches("[^/\\\\]+")) {
      // invalid 'filename' value
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid 'filename' parameter value");
      response.flushBuffer();
      return;
      
    }
    
    //////////////////////////////  
    
    // 'lines' request parameter
    final int lines;
    String linesStr = request.getParameter("lines");
    if (linesStr != null) {
      try {
        lines = Integer.parseInt(linesStr);
      } catch (NumberFormatException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid 'lines' parameter value");
        response.flushBuffer();
        return;
      }
      
      if (lines < 1) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid 'lines' parameter value");
        response.flushBuffer();
        return;
      }
    } else {
      lines = FRAME_SIZE;
    }
    
    
    //////////////////////////////

    final String anchorStr = request.getParameter("anchor");
    final int anchor;  
    if (anchorStr == null) {
      // anchor-undefined (initial) monitor request
    
      try (Reader fileReader = readFile(request, filename)){
        anchor = getAnchorLine(fileReader);
        
      } catch (FileNotFoundException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no file found by such 'filename' parameter value");
        response.flushBuffer();
        return;
      }

      
      
      
      response.sendRedirect("?filename=" + filename 
          + "&anchor=" + anchor 
          + "&lines=" + lines);
      response.flushBuffer();
      return;
      
    } else {
      // anchor-defined (repetitive) monitor request
      
      try {
        anchor = Integer.parseInt(anchorStr);//TODO validate anchor value (int range)
      } catch (NumberFormatException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid 'anchor' parameter value");
        response.flushBuffer();
        return;
      }
      
      
      
      final URL url = new URL(request.getRequestURL().toString());
      final String host = url.getHost() + (url.getPort() == 80 ? "" : (":" + url.getPort()));
         
      
      final MonitorResultDto monitor;
      
      try (Reader fileReader = readFile(request, filename)) {
        monitor = monitor(fileReader, anchor, lines);
        
      } catch (FileNotFoundException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no file found by such 'filename' parameter value");
        response.flushBuffer();
        return;
      }
      
      
      final String loadMoreLinesUrl = request.getRequestURL().toString()
          + "?filename=" + filename
          + "&anchor=" + anchor
          + "&lines=" + (lines + FRAME_SIZE);

      final String resetAnchorUrl;
      if (monitor.contentLinesBottom != null && monitor.contentLinesBottom.size() > 0) {
        
        // increase by number of bottom lines
        final int newAnchor = anchor + monitor.contentLinesBottom.size();
        final int newLines = RESET_LINES_ON_ANCHOR_RESET ? FRAME_SIZE : (lines + monitor.contentLinesBottom.size()); 
        
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
      
      request.getRequestDispatcher("/gui/log-monitor/log-monitor.jsp").include(request, response);
      
      return;
    } 
  }
  
  private static int getAnchorLine(Reader fileReader) throws IOException {
    int lineCount = 0;

    try (BufferedReader reader = new BufferedReader(fileReader)) {
      while (reader.readLine() != null) {
        lineCount++;
      }
    }
    
    return lineCount > 0 ? lineCount - 1 : 0;
  }
  
  /**
   * @param log file reader
   * @param anchor index of the anchor line in the file 
   * (index of the last line loaded on the {@link #initMonitor} request), from 0 
   * @param lines > 0, total number of lines to load (counting back from the anchor, including it)
   * @return
   */
  public static MonitorResultDto monitor(Reader fileReader, int anchor, int lines) {

    if (lines < 1) {
      throw new IllegalArgumentException();
    }
    
    try {

      boolean fileBeginReached = true;
      LinkedList<String> contentLinesTop = new LinkedList<>();
      List<String> contentLinesBottom = new LinkedList<>();
      
      // total char count
      long charCount = 0;
      
      int lineIndex = 0;
      
      try (BufferedReader reader = new BufferedReader(fileReader)) {
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
      }

      
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
