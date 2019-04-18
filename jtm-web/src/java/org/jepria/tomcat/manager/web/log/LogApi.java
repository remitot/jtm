package org.jepria.tomcat.manager.web.log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.log.dto.LogDto;

public class LogApi {

  protected <T> Comparator<T> subsequentComparator(List<Comparator<T>> sequence) {
    return new Comparator<T>() {
      @Override
      public int compare(T o1, T o2) {
        int cmpResult = 0;
        for (Comparator<T> cmp: sequence) {
          cmpResult = cmp.compare(o1, o2);
          if (cmpResult != 0) {
            break;
          }
        }
        return cmpResult;
      }
    };
  }
  
  protected Comparator<LogDto> filenameComparator(int order) {
    return new Comparator<LogDto>() {
      @Override
      public int compare(LogDto o1, LogDto o2) {
        final int cmpResult = o1.getName().compareTo(o2.getName());
        return order < 0 ? -cmpResult : cmpResult;
      }
    };
  }
  
  protected Comparator<LogDto> lastModifiedComparator(int order) {
    return new Comparator<LogDto>() {
      @Override
      public int compare(LogDto o1, LogDto o2) {
        final int cmpResult = o1.getLastModified().compareTo(o2.getLastModified());
        return order < 0 ? -cmpResult : cmpResult;
      }
    };
  }
  
  /**
   * 
   * @param environment
   * @param sortConfig list of sort configurations where each value is one of:
   * <ul>
   *   <li><code>+filename</code> or <code>filename</code>: sort ascending by file name</li>
   *   <li><code>-filename</code>: sort descending by file name</li>
   *   <li><code>+lastModified</code> or <code>lastModified</code>: sort ascending by the last file modification timestamp</li>
   *   <li><code>-lastModified</code>: sort descending by the last file modification timestamp</li>
   * </ul>
   * If the parameter is null or empty, the default sort configuration is applied: <code>["-lastModified", "+filename"]</code>.
   * @return
   */
  public List<LogDto> list(Environment environment, List<String> sortConfig) {
    
    final List<Comparator<LogDto>> comparatorSequence = new ArrayList<>();
    
    if (sortConfig == null || sortConfig.size() == 0) {
      // default value
      comparatorSequence.add(lastModifiedComparator(-1));
      comparatorSequence.add(filenameComparator(1));
    } else {
      for (String column: sortConfig) {
        if ("+filename".equals(column) || "filename".equals(column)) {
          comparatorSequence.add(filenameComparator(1));
        } else if ("-filename".equals(column)) {
          comparatorSequence.add(filenameComparator(-1));
        } else if ("+lastModified".equals(column) || "lastModified".equals(column)) {
          comparatorSequence.add(lastModifiedComparator(1));
        } else if ("-lastModified".equals(column)) {
          comparatorSequence.add(lastModifiedComparator(-1));
        } else {
          throw new IllegalArgumentException(column);
        }
      }
    }
    final Comparator<LogDto> sortComparator = subsequentComparator(comparatorSequence);
    
    
      
    File logsDirectory = environment.getLogsDirectory();
    
    File[] logFiles = logsDirectory.listFiles();
    
    List<LogDto> logs = new ArrayList<>();
    
    if (logFiles != null) {
      for (File logFile: logFiles) {
        LogDto log = new LogDto();
        
        final long lastModified = logFile.lastModified();
        
        log.setName(logFile.getName());
        log.setLastModified(lastModified);
        log.setSize(logFile.length());
        
        logs.add(log);
      }
      
    }
    
    
    // sort the list
    Collections.sort(logs, sortComparator);
    
    return logs;
  }
  
  protected static class ClientDateFormat {
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat timeFormat;
    
    public ClientDateFormat(TimeZone clientTimeZone) {
      dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      dateFormat.setTimeZone(clientTimeZone);
      timeFormat = new SimpleDateFormat("HH:mm:ss");
      timeFormat.setTimeZone(clientTimeZone);
    }
    public String formatDate(Date date) {
      return dateFormat.format(date);
    }
    public String formatTime(Date date) {
      return timeFormat.format(date);
    }
  }
}
