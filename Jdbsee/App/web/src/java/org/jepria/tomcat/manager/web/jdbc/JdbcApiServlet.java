package org.jepria.tomcat.manager.web.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.core.jdbc.Configuration;
import org.jepria.tomcat.manager.core.jdbc.Connection;
import org.jepria.tomcat.manager.web.JtmSecureServlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JdbcApiServlet extends JtmSecureServlet {

  private static final long serialVersionUID = -7724868882541481749L;

  private ConfigurationEnvironment newConfigurationEnvironment(HttpServletRequest req) {
    Path confPath = Paths.get(req.getServletContext().getRealPath("")).getParent().getParent().resolve("conf");
    return new ConfigurationEnvironment(confPath); 
  }


  @Override
  protected void doGetAuth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    resp.setContentType("application/json; charset=UTF-8");
    
    String path = req.getPathInfo();
    
    if ("/list".equals(path)) {
      
      try {
        
        ConfigurationEnvironment environment = newConfigurationEnvironment(req);
        
        Configuration conf = new Configuration(environment.getContextXmlInputStream(), 
            environment.getServerXmlInputStream());
        
        List<ConnectionDto> connectionDtos = getConnections(conf);
        
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
  
  private List<ConnectionDto> getConnections(Configuration configuration) {
    Map<String, Connection> connections = configuration.getConnections();

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
  protected void doPostAuth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String path = req.getPathInfo();
    
    if (!"/mod".equals(path)) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
    
    resp.setContentType("application/json; charset=UTF-8");

    try {

      List<ModRequestDto> modRequests;

      try {
        Type mapType = new TypeToken<HashMap<String, List<ModRequestDto>>>(){}.getType();
        Map<String, List<ModRequestDto>> requestJsonMap = new Gson().fromJson(new InputStreamReader(req.getInputStream()), mapType);
        modRequests = requestJsonMap.get("mod_requests");
        if (modRequests == null) {
          throw new NoSuchElementException("mod_requests");
        }
      } catch (Throwable e) {
        e.printStackTrace();

        resp.getOutputStream().println("Error parsing JSON request body");
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.flushBuffer();
        return;
      }

      ConfigurationEnvironment environment = newConfigurationEnvironment(req);
      
      Configuration conf = new Configuration(environment.getContextXmlInputStream(), 
          environment.getServerXmlInputStream());

      final Map<String, Connection> connections = conf.getConnections();

      // responses correspond to requests
      int[] connectionModificationResponseStatuses = new int[modRequests.size()];
      Set<Integer> processedRequestIndexes = new HashSet<>();

      // 1) updates
      for (int i = 0; i < modRequests.size(); i++) {
        ModRequestDto cmRequest = modRequests.get(i);

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
      for (int i = 0; i < modRequests.size(); i++) {
        ModRequestDto cmRequest = modRequests.get(i);

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
      for (int i = 0; i < modRequests.size(); i++) {
        ModRequestDto cmRequest = modRequests.get(i);

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



      // 4) process illegal actions
      for (int i = 0; i < modRequests.size(); i++) {
        if (!processedRequestIndexes.contains(i)) {
          int cmResponse = ConnectionModificationResponseStatus.ERR__ILLEGAL_ACTION;
          connectionModificationResponseStatuses[i] = cmResponse;
        }
      }


      // 5) do a fake save (to a temporary storage) and get after-save connections from there
      ByteArrayOutputStream contextXmlBaos = new ByteArrayOutputStream();
      ByteArrayOutputStream serverXmlBaos = new ByteArrayOutputStream();
      
      conf.save(contextXmlBaos, serverXmlBaos);
      
      Configuration confAfterSave = new Configuration(
          new ByteArrayInputStream(contextXmlBaos.toByteArray()),
          new ByteArrayInputStream(serverXmlBaos.toByteArray()));
      
      List<ConnectionDto> connectionDtos = getConnections(confAfterSave);
      
      // prepare repsonse to write as fast as possible, after a real save
      Map<String, Object> responseJsonMap = new HashMap<>();
      responseJsonMap.put("mod_states", connectionModificationResponseStatuses);
      responseJsonMap.put("connections", connectionDtos);
      
      ByteArrayOutputStream preparedResponse = new ByteArrayOutputStream();
      
      try (OutputStreamWriter osw = new OutputStreamWriter(preparedResponse, "UTF-8")) {
        new Gson().toJson(responseJsonMap, osw);
      }
      
      // 6) do a real save
      conf.save(environment.getContextXmlOutputStream(), 
          environment.getServerXmlOutputStream());
      
      // XXX potential vulnerability here!
      // If tomcat configuration has autodeploy=true option,
      // then it will reload the server by context.xml change event. 
      // Although we try to write response as fast as possible further,
      // the server may have already started reloading. 
      // In this case, the servlet may behave unexpectedly:
      // respond 500, or wait to respond after the server reloading finishes, whatever.
      
      // write response as fast as possible
      OutputStream os = resp.getOutputStream();
      for (byte b: preparedResponse.toByteArray()) {
        os.write(b);
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
