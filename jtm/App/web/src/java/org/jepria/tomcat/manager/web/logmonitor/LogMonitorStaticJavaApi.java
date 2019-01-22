package org.jepria.tomcat.manager.web.logmonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;

/**
 * Class contains static java API aimed for using by log-monitor.jsp only
 */
public class LogMonitorStaticJavaApi {

  //TODO this value is assumed. But how to determine it? 
  private static final String LOG_FILE_READ_ENCODING = "UTF-8";

  public static InitMonitorResultDto initMonitor(HttpServletRequest request,
      String filename, int lines) throws FileNotFoundException {

    try {

      Environment environment = EnvironmentFactory.get(request);

      File logsDirectory = environment.getLogsDirectory();

      Path logFile = logsDirectory.toPath().resolve(filename);

      LinkedList<String> contentLinesBeforeAnchor = new LinkedList<>();

      int lineCount = 0;

      try (Scanner sc = new Scanner(logFile.toFile(), LOG_FILE_READ_ENCODING)) {
        while (sc.hasNextLine()) {
          final String line = sc.nextLine();
          
          contentLinesBeforeAnchor.add(line);

          if (contentLinesBeforeAnchor.size() > lines) {
            contentLinesBeforeAnchor.removeFirst();
          }

          lineCount++;
        }
      } catch (FileNotFoundException e) {
        throw e;
      }// TODO catch also non-readable file excepiton

      
      final int anchorLine = lineCount > 0 ? lineCount - 1 : 0;
      
      InitMonitorResultDto ret = new InitMonitorResultDto();
      ret.setAnchorLine(anchorLine);
      ret.setContentLinesBeforeAnchor(contentLinesBeforeAnchor);

      return ret;

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
      ret.setContentLinesBeforeAnchor(contentLinesBeforeAnchor);
      ret.setContentLinesAfterAnchor(contentLinesAfterAnchor);
      
      return ret;

    } catch (Throwable e) {
      e.printStackTrace();

      throw new RuntimeException(e);
    }
  }
}
