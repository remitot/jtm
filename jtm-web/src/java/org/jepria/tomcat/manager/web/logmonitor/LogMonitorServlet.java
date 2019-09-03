package org.jepria.tomcat.manager.web.logmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.log.LogApi;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.HtmlEscaper;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.SsrServletBase;

public class LogMonitorServlet extends SsrServletBase  {

  private static final long serialVersionUID = -4404438014956108762L;

  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  protected class MonitorResultDto {
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
  /**
   * Maximum number of chars for a log file fragment to load per a monitor request
   */
  // TODO measure in bytes instead of chars
  private static final long LOAD_LIMIT = 1000000;
  //TODO extract?
  private static final boolean RESET_LINES_ON_ANCHOR_RESET = true;
  //TODO extract?
  private static final int FRAME_SIZE = 500; //TODO extract?
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
    final Environment env = EnvironmentFactory.get(request);
    
    
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
    
    
    // host
    final URL url = new URL(request.getRequestURL().toString());
    final String host = url.getHost() + (url.getPort() == 80 ? "" : (":" + url.getPort()));
    
    
    
    Context context = Context.get(request, "text/org_jepria_tomcat_manager_web_Text");
    
    final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(context);
    pageBuilder.setTitle(HtmlEscaper.escape(filename) + " — " + host);
    
    
    
    
    if (checkAuth(request)) {
      
      // the content type is defined for the entire method
      response.setContentType("text/html; charset=UTF-8");
  
      
      // 'lines' request parameter
      final int lines;
      String linesStr = request.getParameter("lines");
      if (linesStr != null) {
        try {
          lines = Integer.parseInt(linesStr);
        } catch (NumberFormatException e) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid 'lines' request parameter value: could not parse as integer");
          response.flushBuffer();
          return;
        }
        
        if (lines < 1) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid 'lines' request parameter value: must be a positive integer");
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
      
        try (Reader fileReader = new LogApi().readFile(env, filename)){
          anchor = getAnchorLine(fileReader);
          
        } catch (NoSuchFileException e) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No such file found [" + filename + "]");
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
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid 'anchor' request parameter value: could not parse as integer");
          response.flushBuffer();
          return;
        }
        
        
        final MonitorResultDto monitor;
        
        try (Reader fileReader = new LogApi().readFile(env, filename)) {
          monitor = monitor(fileReader, anchor, lines);
          
        } catch (NoSuchFileException e) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No such file found [" + filename + "]");
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
        
  
        LogMonitorPageContent content = new LogMonitorPageContent(
            context,
            monitor.contentLinesTop,
            monitor.contentLinesBottom,
            (monitor.fileBeginReached ? null : loadMoreLinesUrl),
            FRAME_SIZE,
            resetAnchorUrl);
        
        pageBuilder.setContent(content);
        
      }
      
    } else {
      requireAuth(request, pageBuilder);
    }
    
    pageBuilder.build().respond(response);
  }
  
  private static int getAnchorLine(Reader fileReader) throws IOException {
    int lineCount = 0;

    try (BufferedReader reader = new BufferedReader(fileReader)) {
      while (reader.readLine() != null) {
        lineCount++;
      }
    }
    
    return lineCount > 0 ? lineCount : 1;
  }
  
  /**
   * @param log file reader
   * @param anchor index of the anchor line in the file, beginning with {@code 1}. 
   * Must be {@code 1} of greater
   * (equivalently of the value is number of lines in the file) 
   * @param lines > 0, total number of lines to load (counting back from the anchor, including it)
   * @return
   */
  protected MonitorResultDto monitor(Reader fileReader, int anchor, int lines) {

    if (lines < 1) {
      throw new IllegalArgumentException();
    }
    if (anchor < 1) {
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

        // load lines above the anchor
        while (true) {
          
          line = reader.readLine();
          if (line == null) {
            // file end reached
            break;
          }
          
          lineIndex++;
          
          if (lineIndex <= anchor) {
            contentLinesTop.add(line);
            charCount += line.length();
            
            if (contentLinesTop.size() > lines) {
              String removed = contentLinesTop.removeFirst();
              charCount -= removed.length();
              fileBeginReached = false;
            }
          } else {
            // enough lines loaded
            break;
          }
        }
        
        
        // count all newlines as single chars // TODO that's wrong!
        if (contentLinesTop.size() > 0) {
          charCount += contentLinesTop.size();
        }
        
        
        // check load limit
        if (charCount > LOAD_LIMIT) {
          // TODO return error of crop the load against the limit?
          throw new RuntimeException("Load limit overflow");
        }
        
        
        // load lines below the anchor
        while (true) {
          
          line = reader.readLine();
          if (line == null) {
            // file end reached
            break;
          }
          
          contentLinesBottom.add(line);
          charCount += line.length();
          
          
          // count newlines as single chars // TODO that's wrong!
          charCount++;
          
         
          // check load limit
          if (charCount > LOAD_LIMIT) {
            // TODO return error of crop the load against the limit?
            throw new RuntimeException("Load limit overflow");
          }
        }
        
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
