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
        
        out(response, 
"            <!DOCTYPE html> " + 
"        <html> " + 
"          <head> " + 
"            <title>Tomcat manager: логи</title> <!-- NON-NLS --> " + 
"             " + 
"            <meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\" /> " + 
"            <meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">  " + 
"             " + 
"            <link rel=\"stylesheet\" href=\"log-monitor/log-monitor.css\"> " + 
"             " + 
"          </head> " + 
"           " + 
"          <body onload=\"logmonitor_onload();\">");
        
        out(response, 
            "<div id=\"content\">" +
              "<div class=\"lines lines_before-anchor\">");
        
        if (contentLinesBeforeAnchor != null) {
          for (String line: contentLinesBeforeAnchor) {
            out(response, "<div class=\"hf\">" + line + "</div><br/>");      
          }
        }
        
        out(response, 
              "</div>" +
              "<div class=\"lines lines_after-anchor\">");
        
        if (contentLinesAfterAnchor != null) {
          for (String line: contentLinesAfterAnchor) {
            out(response, "<div class=\"hf\">" + line + "</div><br/>");      
          }
        }
        
        out(response, 
            "</div>" +
          "</div>");
        
        String url = "http://localhost:8081/jtm/log-monitor?"
            + "filename=" + filename
            + "&anchor=" + anchor
            + "&lines=" + (lines + FRAME_SIZE);
        
        out(response,
"        <script type=\"text/javascript\"> " +
"          var linesBeforeAnchor = document.getElementsByClassName(\"lines_before-anchor\")[0]; " + 
"           " + 
"          /** " + 
"           * Returns scroll offset (the viewport position) from the bottom of the page, in pixels " + 
"           */ " + 
"          function getOffset() { " + 
"            var offset = window.location.hash.substring(1); " + 
"            if (offset) { " + 
"              return offset; " + 
"            } else { " + 
"              return null; " + 
"            } " + 
"          } " + 
"           " + 
"          function logmonitor_onload() { " + 
"            /* scroll to the offset */ " + 
"             " + 
"            var offset = getOffset(); " + 
"            if (offset) { " + 
"              scrTo(linesBeforeAnchor.clientHeight - offset); " + 
"            } else { " +
"              content = document.getElementById(\"content\"); " +
"              if (content.clientHeight <= window.innerHeight) { " +
"                if (content.clientHeight > 0) { " +
"                  scrTo(1); " + 
"                } " +
"              } else { " + 
"                scrTo(document.getElementById(\"content\").clientHeight - window.innerHeight); " + 
"              }         " + 
"            } " + 
"          } " + 
"           " + 
"           " + 
"          function scrTo(y) { " + 
"            if (document.body.scrollHeight < window.innerHeight + y) { " + 
"              /* adjust scrollHeight */ " + 
"              document.body.style.height = (window.innerHeight + y) + \"px\"; " + 
"            } " + 
"            window.scrollTo(0, y); " + 
"          } " + 
" " + 
"         " + 
"          window.onscroll = function() { " + 
"            var scrolled = window.pageYOffset || document.documentElement.scrollTop; " + 
" " + 
"            var offset = linesBeforeAnchor.clientHeight - scrolled;     " + 
"                  " + 
"            window.location.hash = \"#\" + offset;  " + 
"             " + 
"            if (scrolled == 0) { " + 
"              /* top reached */ " + 
"               " + 
"              /* because location.reload() not wotking in FF and Chrome */ " + 
"              window.location.href = \"" + url + "\" " + 
"                 + \"#\" + offset; " + 
"            } " + 
"          } " + 
"        </script> ");
        
        
        out(response, 
            "</body>" +
          "</html>");
      } 
    }
  }
  
  //TODO this value is assumed. But how to determine it? 
  
  private static final int FRAME_SIZE = 100; //TODO extract?
  
  private static void out(HttpServletResponse response, String str) 
      throws IOException {
    response.getWriter().println(str);
  }
  
  
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
