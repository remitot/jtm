package org.jepria.tomcat.manager.web.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.jepria.tomcat.manager.core.TransactionException;
import org.jepria.tomcat.manager.core.jdbc.Connection;
import org.jepria.tomcat.manager.core.jdbc.TomcatConfJdbc;
import org.jepria.tomcat.manager.web.BasicEnvironment;
import org.jepria.tomcat.manager.web.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JdbcApiServlet extends HttpServlet {

  private static final long serialVersionUID = -7724868882541481749L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    resp.setContentType("application/json; charset=UTF-8");
    
    String path = req.getPathInfo();
    
    if ("/list".equals(path)) {
      
      try {
        
        Environment environment = new BasicEnvironment(req);
        
        TomcatConfJdbc tomcatConf = new TomcatConfJdbc(environment.getContextXmlInputStream(), 
            environment.getServerXmlInputStream());
        
        List<ConnectionDto> connectionDtos = getConnections(tomcatConf);
        
        Map<String, Object> responseJsonMap = new HashMap<>();
        responseJsonMap.put("connections", connectionDtos);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(responseJsonMap, new PrintStream(resp.getOutputStream()));
        
      } catch (Throwable e) {
        e.printStackTrace();

        resp.getOutputStream().println("Oops! Something went wrong.");//TODO
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.flushBuffer();
        return;
      }

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.flushBuffer();
      return;
      
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
  
  private static List<ConnectionDto> getConnections(TomcatConfJdbc tomcatConf) {
    Map<String, Connection> connections = tomcatConf.getConnections();

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


  private void mod(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json; charset=UTF-8");

    try {

      List<ModRequestDto> modRequests;

      try {
        Type mapType = new TypeToken<HashMap<String, List<ModRequestDto>>>(){}.getType();
        Map<String, List<ModRequestDto>> requestJsonMap = new Gson().fromJson(new InputStreamReader(req.getInputStream()), mapType);
        modRequests = requestJsonMap.get("mod_requests");
        
        if (modRequests == null) {
          throw new IllegalStateException("mod_requests is null");
        }
      } catch (Throwable e) {
        e.printStackTrace();

        resp.getOutputStream().println("Error parsing JSON request body");
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.flushBuffer();
        return;
      }

      Environment environment = new BasicEnvironment(req);
      
      TomcatConfJdbc tomcatConf = new TomcatConfJdbc(environment.getContextXmlInputStream(), 
          environment.getServerXmlInputStream());

      final Map<String, Connection> connections = tomcatConf.getConnections();

      // response statuses and messages, corresponding to requests
      final ConnectionModificationResponseStatus[] respStatuses = new ConnectionModificationResponseStatus[modRequests.size()];
      
      Set<Integer> processedRequestIndexes = new HashSet<>();
      
      boolean confModified = false; 
      
      // 1) updates
      for (int i = 0; i < modRequests.size(); i++) {
        ModRequestDto cmRequest = modRequests.get(i);

        if ("update".equals(cmRequest.getAction())) {
          processedRequestIndexes.add(i);

          ConnectionModificationResponseStatus respStatus;
          
          try {
            String location = cmRequest.getLocation();

            if (location == null) {

              respStatus = ConnectionModificationResponseStatus.errLocationIsEmpty();
                  
            } else {

              Connection connection = connections.get(location);

              if (connection == null) {

                respStatus = ConnectionModificationResponseStatus.errConnectionNotFoundByLocation(location);

              } else {
                ConnectionDto connectionDto = cmRequest.getData();

                if (connectionDto.getActive() != null) {
                  if (!connection.isActive() && connectionDto.getActive()) {
                    connection.onActivate();
                  } else if (connection.isActive() && !connectionDto.getActive()) {
                    connection.onDeactivate();
                  }
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

                respStatus = ConnectionModificationResponseStatus.success();
                confModified = true;
              }
            }
          } catch (Throwable e) {
            e.printStackTrace();
            respStatus = ConnectionModificationResponseStatus.errInternalError();
          }

          respStatuses[i] = respStatus;
        }
      }


      // 2) deletions
      for (int i = 0; i < modRequests.size(); i++) {
        ModRequestDto cmRequest = modRequests.get(i);

        if ("delete".equals(cmRequest.getAction())) {
          processedRequestIndexes.add(i);

          ConnectionModificationResponseStatus respStatus;

          try {
            String location = cmRequest.getLocation();

            if (location == null) {

              respStatus = ConnectionModificationResponseStatus.errLocationIsEmpty();

            } else {

              Connection connection = connections.get(location);

              if (connection == null) {

                respStatus = ConnectionModificationResponseStatus.errConnectionNotFoundByLocation(location);

              } else {
                tomcatConf.delete(location);

                respStatus = ConnectionModificationResponseStatus.success();
                confModified = true;
              }
            }
          } catch (Throwable e) {
            e.printStackTrace();
            respStatus = ConnectionModificationResponseStatus.errInternalError();
          }

          respStatuses[i] = respStatus;
        }
      }


      // 3) creations
      for (int i = 0; i < modRequests.size(); i++) {
        ModRequestDto cmRequest = modRequests.get(i);

        if ("create".equals(cmRequest.getAction())) {
          processedRequestIndexes.add(i);

          ConnectionModificationResponseStatus respStatus;

          try {
            ConnectionDto connectionDto = cmRequest.getData();

            // check mandatory fields of a new connection
            List<String> emptyFields = getEmptyMandatoryFields(connectionDto);

            if (!emptyFields.isEmpty()) {

              respStatus = ConnectionModificationResponseStatus.errMandatoryFieldsEmpty(emptyFields);

            } else {

              Connection newConnection = tomcatConf.create(environment.getJdbcConnectionInitialParams());

              newConnection.setDb(connectionDto.getDb());
              newConnection.setName(connectionDto.getName());
              newConnection.setPassword(connectionDto.getPassword());
              newConnection.setServer(connectionDto.getServer());
              newConnection.setUser(connectionDto.getUser());

              respStatus = ConnectionModificationResponseStatus.success();
              confModified = true;
            }
          } catch (Throwable e) {
            e.printStackTrace();
            respStatus = ConnectionModificationResponseStatus.errInternalError();
          }

          respStatuses[i] = respStatus;
        }
      }



      // 4) process illegal actions
      for (int i = 0; i < modRequests.size(); i++) {
        if (!processedRequestIndexes.contains(i)) {
          respStatuses[i] = ConnectionModificationResponseStatus.errIllegalAction(modRequests.get(i).getAction());
        }
      }


      
      Map<String, Object> responseJsonMap = new HashMap<>();
      responseJsonMap.put("statuses", Arrays.stream(respStatuses).map(status -> status.code).collect(Collectors.toList()));
      // TODO maybe to check some URL parameter (such as 'verbose=1') and to put or not to put status_messages into a response?
      responseJsonMap.put("status_messages", Arrays.stream(respStatuses).map(status -> status.message).collect(Collectors.toList()));
      
      if (confModified) {
        
        // because the new connection list needed in response, do a fake save (to a temporary storage) and get after-save connections from there
        ByteArrayOutputStream contextXmlBaos = new ByteArrayOutputStream();
        ByteArrayOutputStream serverXmlBaos = new ByteArrayOutputStream();
        
        tomcatConf.save(contextXmlBaos, serverXmlBaos);
        
        TomcatConfJdbc tomcatConfAfterSave = new TomcatConfJdbc(
            new ByteArrayInputStream(contextXmlBaos.toByteArray()),
            new ByteArrayInputStream(serverXmlBaos.toByteArray()));
        
        List<ConnectionDto> connectionDtos = getConnections(tomcatConfAfterSave);
        responseJsonMap.put("connections", connectionDtos);
        
        saveAndWriteResponse(tomcatConf, environment, responseJsonMap, resp);
        return;
        
      } else {
        // no conf save, just write response
        List<ConnectionDto> connectionDtos = getConnections(tomcatConf);
        responseJsonMap.put("connections", connectionDtos);
        
        try (OutputStreamWriter osw = new OutputStreamWriter(resp.getOutputStream(), "UTF-8")) {
          new Gson().toJson(responseJsonMap, osw);
        }
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
        
        return;
      }
      
    } catch (Throwable e) {
      e.printStackTrace();

      resp.getOutputStream().println("Oops! Something went wrong.");//TODO
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }
  }
  
  private static void saveAndWriteResponse(TomcatConfJdbc tomcatConf, Environment environment,
      Map<String, Object> responseJsonMap, HttpServletResponse resp)
          throws TransactionException, IOException {
    // prepare response to write as fast as possible, after save completes
    ByteArrayOutputStream preparedResponse = new ByteArrayOutputStream();
    
    try (OutputStreamWriter osw = new OutputStreamWriter(preparedResponse, "UTF-8")) {
      new Gson().toJson(responseJsonMap, osw);
    }
    
    // do save
    tomcatConf.save(environment.getContextXmlOutputStream(), 
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
  }
  
  private void ensure(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json; charset=UTF-8");

    try {
      ConnectionDto connectionDto;

      try {
        connectionDto = new Gson().fromJson(new InputStreamReader(req.getInputStream()), ConnectionDto.class);
        
        if (connectionDto == null) {
          throw new IllegalStateException("connectionDto is null");
        }
      } catch (Throwable e) {
        e.printStackTrace();

        resp.getOutputStream().println("Error parsing JSON request body");
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        resp.flushBuffer();
        return;
      }

      Environment environment = new BasicEnvironment(req);
      
      TomcatConfJdbc tomcatConf = new TomcatConfJdbc(environment.getContextXmlInputStream(), 
          environment.getServerXmlInputStream());

      final Map<String, Connection> connections = tomcatConf.getConnections();

      EnsureConnectionResponseStatus respStatus;
      
      boolean confModified = false;
      
      try {
        
        // check mandatory fields of a new connection
        List<String> emptyFields = getEmptyMandatoryFields(connectionDto);

        if (!emptyFields.isEmpty()) {

          respStatus = EnsureConnectionResponseStatus.errMandatoryFieldsEmpty(emptyFields);

        } else {

          // deactivate all active connections with the same name
          List<Connection> existingConnections = connections.values().stream()
              .filter(connection -> connectionDto.getName().equals(connection.getName()) && connection.isActive())
              .collect(Collectors.toList());
          
          
          // check the same existing connection
          boolean hasExistingSameConnection = existingConnections.stream()
              .filter(connection -> connectionsEqual(connectionDto, connection)).findAny().isPresent(); 
              
          if (hasExistingSameConnection) {
            respStatus = EnsureConnectionResponseStatus.successExistedTheSame();
            
          } else {

            for (Connection connection: existingConnections) {
              connection.onDeactivate();
            }
            
            
            // create a new active connection
            Connection newConnection = tomcatConf.create(environment.getJdbcConnectionInitialParams());
  
            newConnection.setDb(connectionDto.getDb());
            newConnection.setName(connectionDto.getName());
            newConnection.setPassword(connectionDto.getPassword());
            newConnection.setServer(connectionDto.getServer());
            newConnection.setUser(connectionDto.getUser());
  
            
            if (existingConnections.isEmpty()) {
              respStatus = EnsureConnectionResponseStatus.successNoExistCreated();
            } else {
              respStatus = EnsureConnectionResponseStatus.successExistedCreated();
            }
            
            confModified = true;
          }
        }
        
      } catch (Throwable e) {
        e.printStackTrace();
        respStatus = EnsureConnectionResponseStatus.errInternalError();
      }
      
      
      Map<String, Object> responseJsonMap = new HashMap<>();
      responseJsonMap.put("status", respStatus.code);
      // TODO maybe to check some URL parameter (such as 'verbose=1') and to put or not to put status_message into a response?
      responseJsonMap.put("status_message", respStatus.message);
      
      if (confModified) {
        saveAndWriteResponse(tomcatConf, environment, responseJsonMap, resp);
        return;
        
      } else {
        // no conf save, just write response
        try (OutputStreamWriter osw = new OutputStreamWriter(resp.getOutputStream(), "UTF-8")) {
          new Gson().toJson(responseJsonMap, osw);
        }
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
        
        return;
      }

    } catch (Throwable e) {
      e.printStackTrace();

      resp.getOutputStream().println("Oops! Something went wrong.");//TODO
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }
  }
  
  private static boolean connectionsEqual(ConnectionDto connectionDto, Connection connection) {
    return connectionDto.getName().equals(connection.getName()) &&
        connectionDto.getServer().equals(connection.getServer()) &&
        connectionDto.getDb().equals(connection.getDb()) &&
        connectionDto.getUser().equals(connection.getUser()) &&
        connectionDto.getPassword().equals(connection.getPassword());
  }
  
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String path = req.getPathInfo();
    
    if ("/mod".equals(path)) {
      mod(req, resp);
      return;
      
    } else if ("/ensure".equals(path)) {
      ensure(req, resp);
      return;
      
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
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
