package org.jepria.tomcat.manager.web.jdbc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.core.jdbc.Configuration;
import org.jepria.tomcat.manager.core.jdbc.Configuration.TransactionException;
import org.jepria.tomcat.manager.core.jdbc.ConfigurationContext;
import org.jepria.tomcat.manager.core.jdbc.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JdbcServlet extends HttpServlet {

  private static final long serialVersionUID = -7724868882541481749L;

  private Configuration newConfiguration(HttpServletRequest req) throws TransactionException {
    Path confPath = Paths.get(req.getServletContext().getRealPath("")).getParent().getParent().resolve("conf");
    return new Configuration(new ConfigurationContext.Default(confPath)); 
  }


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String path = req.getPathInfo();
    
    if ("/list".equals(path)) {
      resp.setContentType("application/json; charset=UTF-8");
      
      try {
        List<ConnectionDto> connectionDtos = list(req);
        
        Map<String, Object> responseJsonMap = new HashMap<>();
        responseJsonMap.put("connections", connectionDtos);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(responseJsonMap, new PrintStream(resp.getOutputStream()));
        
      } catch (Throwable e) {
        e.printStackTrace();

        resp.getOutputStream().println("Oops! Something went wrong.");
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.flushBuffer();
        return;
      }

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.flushBuffer();
    }
  }
  
  private List<ConnectionDto> list(HttpServletRequest req) throws TransactionException {
    Configuration conf = newConfiguration(req);

    Map<String, Connection> connections = conf.getConnections();

    // list all connections
    return connections.entrySet().stream().map(
        entry -> connectionToDto(entry.getKey(), entry.getValue()))
        .sorted(connectionSorter()).collect(Collectors.toList());
  }
  
  private static Comparator<ConnectionDto> connectionSorter() {
    return new Comparator<ConnectionDto>() {
      @Override
      public int compare(ConnectionDto o1, ConnectionDto o2) {
        int nameCmp = o1.getName().compareTo(o2.getName());
        if (nameCmp == 0) {
          // the active is the first
          if (o1.getActive() && !o2.getActive()) {
            return -1;
          } else if (o2.getActive() && !o1.getActive()) {
            return 1;
          } else {
            return 0;
          }
        } else {
          return nameCmp;
        }
      }
    };
  }

  private static ConnectionDto connectionToDto(String location, Connection connection) {
    ConnectionDto dto = new ConnectionDto();
    dto.setActive(connection.isActive());
    dto.setDb(connection.getDb());
    dto.setLocation(location);
    dto.setName(connection.getName());
    dto.setPassword(connection.getPassword());
    dto.setServer(connection.getServer());
    dto.setUser(connection.getUser());
    return dto;
  }



  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String path = req.getPathInfo();
    
    if (!"/mod".equals(path)) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
    
    resp.setContentType("application/json; charset=UTF-8");

    try {

      List<ConnectionModificationRequestDto> connectionModificationRequests;

      try {
        Type listType = new TypeToken<ArrayList<ConnectionModificationRequestDto>>(){}.getType();
        connectionModificationRequests = new Gson().fromJson(new InputStreamReader(req.getInputStream()), listType);
      } catch (Throwable e) {
        e.printStackTrace();

        resp.getOutputStream().println("Error parsing JSON request body: expected list of connection modification request objects");
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.flushBuffer();
        return;
      }

      Configuration conf = newConfiguration(req);

      final Map<String, Connection> connections = conf.getConnections();

      // responses correspond to requests
      int[] connectionModificationResponseStatuses = new int[connectionModificationRequests.size()];
      Set<Integer> processedRequestIndexes = new HashSet<>();

      // 1) updates
      for (int i = 0; i < connectionModificationRequests.size(); i++) {
        ConnectionModificationRequestDto cmRequest = connectionModificationRequests.get(i);

        if ("update".equals(cmRequest.getAction())) {
          processedRequestIndexes.add(i);

          int cmResponse;

          try {
            String location = cmRequest.getLocation();

            if (location == null) {

              cmResponse = ConnectionModificationResponseStatus.ERR__LOCATION_IS_EMPTY;

            } else {

              Connection connection = connections.get(location);

              if (connection == null) {

                cmResponse = ConnectionModificationResponseStatus.ERR__CONNECTION_NOT_FOUND_BY_LOCATION;

              } else {
                ConnectionDto connectionDto = cmRequest.getData();

                if (connectionDto.getActive() != null) {
                  connection.setActive(connectionDto.getActive());
                }
                if (connectionDto.getDb() != null) {
                  connection.setDb(connectionDto.getDb());
                }
                if (connectionDto.getName() != null) {
                  connection.setName(connectionDto.getName());
                }
                if (connectionDto.getPassword() != null) {
                  connection.setPassword(connectionDto.getPassword());
                }
                if (connectionDto.getServer() != null) {
                  connection.setServer(connectionDto.getServer());
                }
                if (connectionDto.getUser() != null) {
                  connection.setUser(connectionDto.getUser());
                }

                cmResponse = ConnectionModificationResponseStatus.SUCCESS;
              }
            }
          } catch (Throwable e) {
            e.printStackTrace();
            cmResponse = ConnectionModificationResponseStatus.ERR__INTERNAL_ERROR;
          }

          connectionModificationResponseStatuses[i] = cmResponse;
        }
      }


      // 2) deletions
      for (int i = 0; i < connectionModificationRequests.size(); i++) {
        ConnectionModificationRequestDto cmRequest = connectionModificationRequests.get(i);

        if ("delete".equals(cmRequest.getAction())) {
          processedRequestIndexes.add(i);

          int cmResponse;

          try {
            String location = cmRequest.getLocation();

            if (location == null) {

              cmResponse = ConnectionModificationResponseStatus.ERR__LOCATION_IS_EMPTY;

            } else {

              Connection connection = connections.get(location);

              if (connection == null) {

                cmResponse = ConnectionModificationResponseStatus.ERR__CONNECTION_NOT_FOUND_BY_LOCATION;

              } else {
                conf.delete(location);

                cmResponse = ConnectionModificationResponseStatus.SUCCESS;
              }
            }
          } catch (Throwable e) {
            e.printStackTrace();
            cmResponse = ConnectionModificationResponseStatus.ERR__INTERNAL_ERROR;
          }

          connectionModificationResponseStatuses[i] = cmResponse;
        }
      }


      // 3) creations
      for (int i = 0; i < connectionModificationRequests.size(); i++) {
        ConnectionModificationRequestDto cmRequest = connectionModificationRequests.get(i);

        if ("create".equals(cmRequest.getAction())) {
          processedRequestIndexes.add(i);

          int cmResponse;

          try {
            ConnectionDto connectionDto = cmRequest.getData();

            // check mandatory fields of a new connection
            List<String> emptyFields = getEmptyMandatoryFields(connectionDto);

            if (!emptyFields.isEmpty()) {

              cmResponse = ConnectionModificationResponseStatus.ERR__MANDATORY_FIELDS_EMPTY;

            } else {

              Connection newConnection = conf.create();

              newConnection.setDb(connectionDto.getDb());
              newConnection.setName(connectionDto.getName());
              newConnection.setPassword(connectionDto.getPassword());
              newConnection.setServer(connectionDto.getServer());
              newConnection.setUser(connectionDto.getUser());

              cmResponse = ConnectionModificationResponseStatus.SUCCESS;
            }
          } catch (Throwable e) {
            e.printStackTrace();
            cmResponse = ConnectionModificationResponseStatus.ERR__INTERNAL_ERROR;
          }

          connectionModificationResponseStatuses[i] = cmResponse;
        }
      }


      // 4) save results
      conf.save();


      // 5) process illegal actions
      for (int i = 0; i < connectionModificationRequests.size(); i++) {
        if (!processedRequestIndexes.contains(i)) {
          int cmResponse = ConnectionModificationResponseStatus.ERR__ILLEGAL_ACTION;
          connectionModificationResponseStatuses[i] = cmResponse;
        }
      }


      // 6) write response
      Map<String, Object> responseJsonMap = new HashMap<>();
      responseJsonMap.put("modStatuses", connectionModificationResponseStatuses);
      
      //TODO 
      /*
        try {
          // save to temp strings, but not to real files
          conf.save(tempContextString, tempServerString);
          List<ConnectionDto> connectionDtos = list(new Configuration(tempContextString, tempServerString));
          writeResponse(connectionDtos);
        } finally {
          // after response sent, save to real files
          conf.save();
        }
      */
      List<ConnectionDto> connectionDtos = list(req);
      responseJsonMap.put("connections", connectionDtos);
      
      try (OutputStreamWriter osw = new OutputStreamWriter(resp.getOutputStream(), "UTF-8")) {
        new Gson().toJson(connectionModificationResponseStatuses, osw);
      }
      
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.flushBuffer();
      return;

    } catch (Throwable e) {
      e.printStackTrace();

      resp.getOutputStream().println("Oops! Something went wrong.");
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }

  }

  /**
   * 
   * @param connection
   * @return or empty list
   */
  private static List<String> getEmptyMandatoryFields(ConnectionDto connectionDto) {
    List<String> emptyFields = new ArrayList<>();

    if (connectionDto.getDb() == null) {
      emptyFields.add("db");
    }
    if (connectionDto.getName() == null) {
      emptyFields.add("name");
    }
    if (connectionDto.getPassword() == null) {
      emptyFields.add("password");
    }
    if (connectionDto.getServer() == null) {
      emptyFields.add("server");
    }
    if (connectionDto.getUser() == null) {
      emptyFields.add("user");
    }

    return emptyFields;
  }

}
