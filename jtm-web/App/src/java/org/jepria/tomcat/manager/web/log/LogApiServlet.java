package org.jepria.tomcat.manager.web.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.log.dto.LogDto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LogApiServlet extends HttpServlet {

  private static final long serialVersionUID = 5891799566737744116L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String path = req.getPathInfo();
    
    if ("/list".equals(path)) {
      list(req, resp);
      return;
      
    } else if (path == null || "/".equals(path)) {
      fileContents(req, resp);
      return;
      
    } else {
      
      // TODO set content type for the error case?
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
  
  private static <T> Comparator<T> subsequentComparator(List<Comparator<T>> sequence) {
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
  
  private static Comparator<LogDto> filenameComparator(int order) {
    return new Comparator<LogDto>() {
      @Override
      public int compare(LogDto o1, LogDto o2) {
        final int cmpResult = o1.getName().compareTo(o2.getName());
        return order < 0 ? -cmpResult : cmpResult;
      }
    };
  }
  
  private static Comparator<LogDto> lastModifiedComparator(int order) {
    return new Comparator<LogDto>() {
      @Override
      public int compare(LogDto o1, LogDto o2) {
        final int cmpResult = o1.getLastModified().compareTo(o2.getLastModified());
        return order < 0 ? -cmpResult : cmpResult;
      }
    };
  }
  
  private static void list(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("application/json; charset=UTF-8");
    
    
    // 'sort' request parameter
    final String sort = req.getParameter("sort");
    final List<String> sortColumns = new ArrayList<>();
    if (sort != null) {
      for (String column: sort.split(",")) {
        if (!"".equals(column)) {
          while(sortColumns.remove(column));// remove same values added before
          sortColumns.add(column);
        }
      }
    }
    final List<Comparator<LogDto>> comparatorSequence = new ArrayList<>();
    if (sortColumns.isEmpty()) {
      // default value
      comparatorSequence.add(lastModifiedComparator(-1));
      comparatorSequence.add(filenameComparator(1));
    } else {
      for (String column: sortColumns) {
        if ("+filename".equals(column) || "filename".equals(column)) {
          comparatorSequence.add(filenameComparator(1));
        } else if ("-filename".equals(column)) {
          comparatorSequence.add(filenameComparator(-1));
        } else if ("+lastModified".equals(column) || "lastModified".equals(column)) {
          comparatorSequence.add(lastModifiedComparator(1));
        } else if ("-lastModified".equals(column)) {
          comparatorSequence.add(lastModifiedComparator(-1));
        } else {
          // invalid value
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
          resp.flushBuffer();
          return;
        }
      }
    }
    final Comparator<LogDto> sortComparator = subsequentComparator(comparatorSequence);
    
    
    // 'tzoffset' request parameter
    final Integer tzOffsetMins;
    final String tzoffset = req.getParameter("tzoffset");
    if (tzoffset != null) {
      try {
        tzOffsetMins = Integer.parseInt(tzoffset);
      } catch (NumberFormatException e) {
        // invalid value
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        resp.flushBuffer();
        return;
      }
    } else {
      tzOffsetMins = null;
    }
    
    
    try {
      
      Environment environment = EnvironmentFactory.get(req);

      File logsDirectory = environment.getLogsDirectory();
      
      List<LogDto> logs = getLogs(logsDirectory);
      
      // fill local date/time fields
      if (tzOffsetMins != null) {
        if (logs != null) {
          
          final LocalDateFormat localDateFormat = new LocalDateFormat(tzOffsetMins);
          
          for (LogDto log: logs) {
            
            // fill local date/time fields
            Date lastModified = new Date(log.getLastModified());
            log.setLastModifiedDateLocal(localDateFormat.formatDate(lastModified));
            log.setLastModifiedTimeLocal(localDateFormat.formatTime(lastModified));
            
            // fill last modified ago verb
            fillLastModifiedAgoVerbGlobal(log); // global (short ago)
            if (log.getLastModifiedAgoVerb() == null) { // local (long ago)
              // TODO stopped here fill with LOCAL 'today' of 'yesterday'
            }
          }
        }
      }
      
      // sort the list
      Collections.sort(logs, sortComparator);
      
      
      Map<String, Object> responseJsonMap = new HashMap<>();
      responseJsonMap.put("_list", logs);
      
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(responseJsonMap, new PrintStream(resp.getOutputStream()));
      
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.flushBuffer();
      return;
      
    } catch (Throwable e) {
      e.printStackTrace();

      // response body must either be empty or match the declared content type
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }
  }
  
  private static void fillLastModifiedAgoVerbGlobal(LogDto log) {
    final long lastModifiedAgo = log.getLastModifiedAgo();
    final Integer verb;
    if (lastModifiedAgo < 10) {
      verb = 1;
    } else if (lastModifiedAgo < 90) {
      verb = 2;
    } else if (lastModifiedAgo < 150) {
      verb = 3;
    } else if (lastModifiedAgo < 210) {
      verb = 4;
    } else if (lastModifiedAgo < 450) {
      verb = 5;
    } else if (lastModifiedAgo < 900) {
      verb = 6;
    } else if (lastModifiedAgo < 2700) {
      verb = 7;
    } else if (lastModifiedAgo < 5400) {
      verb = 8;
    } else if (lastModifiedAgo < 9000) {
      verb = 9;
    } else if (lastModifiedAgo < 12600) {
      verb = 10;
    } else {
      verb = null;
    }
    log.setLastModifiedAgoVerb(verb);
  }
  
  private static class LocalDateFormat {
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat timeFormat;
    private final TimeZone tz;
    
    public LocalDateFormat(int tzOffsetMins) {
      dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      timeFormat = new SimpleDateFormat("HH:mm:ss");
      
      String[] availableIds = TimeZone.getAvailableIDs(tzOffsetMins * 60 * 1000);
      if (availableIds != null && availableIds.length > 0) {
        tz = TimeZone.getTimeZone(availableIds[0]);
      } else {
        throw new IllegalStateException("No TimeZone IDs found for offset " + tzOffsetMins + " minutes");
      }
      
      dateFormat.setTimeZone(tz);
      timeFormat.setTimeZone(tz);
    }
    public String formatDate(Date date) {
      return dateFormat.format(date);
    }
    public String formatTime(Date date) {
      return timeFormat.format(date);
    }
    public TimeZone getTimeZone() {
      return tz;
    }
  }
  
  // TODO this value is assumed. But how to determine it? 
  private static final String LOG_FILE_READ_ENCODING = "UTF-8";
  
  /**
   * @param request
   * @param response
   * @param filename
   * @param inline whether to set "Content-Disposition" response header "inline" or "attachment"
   * @throws IOException
   */
  private static void fileContents(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    
    // the content type is defined for the entire method
    response.setContentType("text/plain; charset=UTF-8");
    
    
    // 'filename' request parameter
    final String filename = request.getParameter("filename");
    if (filename == null || !filename.matches("[^/\\\\]+")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
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

      File logsDirectory = environment.getLogsDirectory();
      
      Path logFile = logsDirectory.toPath().resolve(filename);

      try (Scanner sc = new Scanner(logFile.toFile(), LOG_FILE_READ_ENCODING)) {
        while (sc.hasNextLine()) {
          response.getWriter().println(sc.nextLine());
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        response.flushBuffer();
        return;
      }// TODO catch also non-readable file excepiton

      
      /*
      // XXX consider the simple solution
      //(but it copies byte-by-byte and hence does not consider encoding):
      try (OutputStream out = resp.getOutputStream()) {
        Files.copy(logFile, out);
      }
       */
      
      response.setStatus(HttpServletResponse.SC_OK);
      response.flushBuffer();
      return;
      
    } catch (Throwable e) {
      e.printStackTrace();

      // response body must either be empty or match the declared content type
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.flushBuffer();
      return;
    }
  }

  /**
   * Does not fill local date fields of the Dtos returned
   * @param logsDirectory
   * @return
   */
  private static List<LogDto> getLogs(File logsDirectory) {
    File[] logFiles = logsDirectory.listFiles();
    
    List<LogDto> logs = new ArrayList<>();
    
    if (logFiles != null) {
      for (File logFile: logFiles) {
        LogDto log = new LogDto();
        
        final long lastModified = logFile.lastModified();
        final long lastModifiedAgo = (System.currentTimeMillis() - lastModified) / 1000;
        
        log.setName(logFile.getName());
        log.setLastModified(lastModified);
        log.setLastModifiedAgo(lastModifiedAgo);
        log.setSize(logFile.length());
        
        logs.add(log);
      }
    }
    
    return logs;
  }
}
