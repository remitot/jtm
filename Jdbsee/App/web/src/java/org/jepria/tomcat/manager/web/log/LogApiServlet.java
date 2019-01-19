package org.jepria.tomcat.manager.web.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;

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
      
      // get log by filename
      String queryString = req.getQueryString();
      if (queryString != null) {
        Matcher m = Pattern.compile("filename\\=([^&/\\\\]+)").matcher(queryString);
        if (m.matches()) {
          String filename = m.group(1);
          fileContents(req, resp, filename);
          return;
          
        }
      }
      
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      resp.flushBuffer();
      return;
      
    } else {
      
      // TODO set content type for the error case?
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
  
  private static void list(HttpServletRequest req, HttpServletResponse resp) 
      throws IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("application/json; charset=UTF-8");
    
    try {
      
      Environment environment = EnvironmentFactory.get(req);

      File logsDirectory = environment.getLogsDirectory();
      
      List<LogDto> logs = getLogs(logsDirectory);

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
  
  // TODO this value is assumed. But how to determine it? 
  private static final String LOG_FILE_READ_ENCODING = "UTF-8";
  
  private static final String LOG_FILE_RESPONSE_WRITE_ENCODING = "UTF-8";
  
  private static void fileContents(HttpServletRequest req, HttpServletResponse resp,
      String filename) throws IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("text/plain; charset=UTF-8");
    
    try {
      
      Environment environment = EnvironmentFactory.get(req);

      File logsDirectory = environment.getLogsDirectory();
      
      Path logFile = logsDirectory.toPath().resolve(filename);
      
      // output encoding
      resp.setCharacterEncoding(LOG_FILE_RESPONSE_WRITE_ENCODING);
      
      try (Scanner sc = new Scanner(logFile.toFile(), LOG_FILE_READ_ENCODING)) {
        while (sc.hasNextLine()) {
          resp.getWriter().println(sc.nextLine());
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        resp.flushBuffer();
        return;
      }
      
      /*
      // XXX this solution is for binary file download (no encoding specified):
      try (OutputStream out = resp.getOutputStream()) {
        Files.copy(logFile, out);
      }
       */
      
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
  
  private static List<LogDto> getLogs(File logsDirectory) {
    File[] logFiles = logsDirectory.listFiles();
    
    List<LogDto> logs = new ArrayList<>();
    
    if (logFiles != null) {
      
      final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss_Z");
      
      for (File logFile: logFiles) {
        LogDto log = new LogDto();
        
        log.setName(logFile.getName());
        
        String lastModified = simpleDateFormat.format(new Date(logFile.lastModified()));
        String[] lastModifiedParts = lastModified.split("_");
        
        log.setLastModifiedDate(lastModifiedParts[0]);
        log.setLastModifiedTime(lastModifiedParts[1]);
        log.setLastModifiedTimezone(lastModifiedParts[2]);
        
        logs.add(log);
      }
    }
    
    // sort by last modified date and time
    Collections.sort(logs, (log1, log2) -> 
        -(log1.getLastModifiedDate() + log1.getLastModifiedTime())
            .compareTo(log2.getLastModifiedDate() + log2.getLastModifiedTime()));
    
    return logs;
  }
}
