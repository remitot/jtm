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

  public static List<String> initMonitor(HttpServletRequest request,
      String filename, int lines, int[] anchorRef) throws FileNotFoundException {

    try {

      Environment environment = EnvironmentFactory.get(request);

      File logsDirectory = environment.getLogsDirectory();

      Path logFile = logsDirectory.toPath().resolve(filename);

      LinkedList<String> contentLines = new LinkedList<>();

      int lineCount = 0;

      try (Scanner sc = new Scanner(logFile.toFile(), LOG_FILE_READ_ENCODING)) {
        while (sc.hasNextLine()) {
          contentLines.add(sc.nextLine());

          if (contentLines.size() > lines) {
            contentLines.removeFirst();
          }

          lineCount++;
        }
      } catch (FileNotFoundException e) {
        throw e;
      }// TODO catch also non-readable file excepiton


      if (anchorRef != null && anchorRef.length > 0) {
        if (lineCount > 0) {
          anchorRef[0] = lineCount - 1;
        } else {
          anchorRef[0] = 0;
        }
      }

      return contentLines;

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
  public static List<String> monitor(HttpServletRequest request,
      String filename, int anchor, int lines) 
          throws FileNotFoundException {

    try {

      Environment environment = EnvironmentFactory.get(request);

      File logsDirectory = environment.getLogsDirectory();

      Path logFile = logsDirectory.toPath().resolve(filename);

      LinkedList<String> contentLines = new LinkedList<>();

      int lineIndex = 0;

      try (Scanner sc = new Scanner(logFile.toFile(), LOG_FILE_READ_ENCODING)) {
        while (sc.hasNextLine()) {
          contentLines.add(sc.nextLine());

          if (lineIndex <= anchor) {
            if (contentLines.size() > lines) {
              contentLines.removeFirst();
            }
          }

          lineIndex++;
        }
      } catch (FileNotFoundException e) {
        throw e;
      }// TODO catch also non-readable file excepiton


      return contentLines;

    } catch (Throwable e) {
      e.printStackTrace();

      throw new RuntimeException(e);
    }
  }
}
