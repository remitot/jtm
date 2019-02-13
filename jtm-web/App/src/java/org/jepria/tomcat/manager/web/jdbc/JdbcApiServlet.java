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
import org.jepria.tomcat.manager.core.jdbc.ConnectionInitialParams;
import org.jepria.tomcat.manager.core.jdbc.TomcatConfJdbc;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ModRequestBodyDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ModRequestDto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JdbcApiServlet extends HttpServlet {

  private static final long serialVersionUID = -7724868882541481749L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    String path = req.getPathInfo();
    
    if ("/list".equals(path)) {
      
      // the content type is defined for the entire method
      resp.setContentType("application/json; charset=UTF-8");
      
      try {
        
        Environment environment = EnvironmentFactory.get(req);
        
        TomcatConfJdbc tomcatConf = new TomcatConfJdbc(
            () -> environment.getContextXmlInputStream(), 
            () -> environment.getServerXmlInputStream());
        
        List<ConnectionDto> connections = getConnections(tomcatConf);

        Map<String, Object> responseJsonMap = new HashMap<>();
        responseJsonMap.put("_list", connections);
        
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

    } else {
      
      // TODO set content type for the error case?
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
        int nameCmp = o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
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


  private static void mod(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("application/json; charset=UTF-8");

    try {

      // read list from request body
      final List<ModRequestDto> modRequests;
      
      try {
        Type type = new TypeToken<ArrayList<ModRequestDto>>(){}.getType();
        modRequests = new Gson().fromJson(new InputStreamReader(req.getInputStream()), type);
        
      } catch (Throwable e) {
        e.printStackTrace();

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        resp.flushBuffer();
        return;
      } 
      
      
      // convert list to map
      final Map<String, ModRequestBodyDto> modRequestBodyMap = new HashMap<>();
      
      if (modRequests != null) {
        for (ModRequestDto modRequest: modRequests) {
          final String modRequestId = modRequest.getModRequestId();
          
          if (modRequestId == null || "".equals(modRequestId)
              || modRequestBodyMap.put(modRequestId, modRequest.getModRequestBody()) != null) {
            // duplicate or empty modRequestId values
            
            // TODO log?
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            resp.flushBuffer();
            return;
          }
        }
      }
      

      
      final Environment environment = EnvironmentFactory.get(req);
      
      final TomcatConfJdbc tomcatConf = new TomcatConfJdbc(
          () -> environment.getContextXmlInputStream(), 
          () -> environment.getServerXmlInputStream());

      // collect processed modRequests
      final Set<String> processedModRequestIds = new HashSet<>();
      
      
      // response map
      final Map<String, ModStatus> modStatusMap = new HashMap<>();
      
      
      boolean confModified = false; 
      
      // 1) perform all updates
      for (String modRequestId: modRequestBodyMap.keySet()) {
        ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);

        if ("update".equals(mreq.getAction())) {
          processedModRequestIds.add(modRequestId);

          ModStatus modStatus = updateConnection(mreq, tomcatConf);
          if (modStatus.code == ModStatus.CODE_SUCCESS) {
            confModified = true;
          }

          modStatusMap.put(modRequestId, modStatus);
        }
      }


      // 2) perform all deletions
      for (String modRequestId: modRequestBodyMap.keySet()) {
        ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);

        if ("delete".equals(mreq.getAction())) {
          processedModRequestIds.add(modRequestId);

          ModStatus modStatus = deleteConnection(mreq, tomcatConf);
          if (modStatus.code == ModStatus.CODE_SUCCESS) {
            confModified = true;
          }
          
          modStatusMap.put(modRequestId, modStatus);
        }
      }


      // 3) perform all creations
      for (String modRequestId: modRequestBodyMap.keySet()) {
        ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);

        if ("create".equals(mreq.getAction())) {
          processedModRequestIds.add(modRequestId);

          ModStatus modStatus = createConnection(mreq, tomcatConf, 
              environment.getJdbcConnectionInitialParams());
          if (modStatus.code == ModStatus.CODE_SUCCESS) {
            confModified = true;
          }
          
          modStatusMap.put(modRequestId, modStatus);
        }
      }



      // 4) process illegal actions
      for (String modRequestId: modRequestBodyMap.keySet()) {
        if (!processedModRequestIds.contains(modRequestId)) {
          
          String action = modRequestBodyMap.get(modRequestId).getAction();
          ModStatus modStatus = ModStatus.errIllegalAction(action);
          
          modStatusMap.put(modRequestId, modStatus);
        }
      }


      // prepare response map
      final Map<String, Object> responseJsonMap = new HashMap<>();
      
      // convert map to list
      final List<Map<String, Object>> modStatusList = modStatusMap.entrySet().stream().map(
          entry -> {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("modRequestId", entry.getKey());
            jsonMap.put("modStatusCode", entry.getValue().code);
            // TODO maybe to check some URL parameter (such as 'verbose=1') and to put or not to put modStatusMessages into a response?
            jsonMap.put("modStatusMessage", entry.getValue().message);
            return jsonMap;
          }).collect(Collectors.toList());
      
      responseJsonMap.put("modStatusList", modStatusList);
      
      
      if (confModified) {
        
        // because the new connection list needed in response, do a fake save (to a temporary storage) and get after-save connections from there
        ByteArrayOutputStream contextXmlBaos = new ByteArrayOutputStream();
        ByteArrayOutputStream serverXmlBaos = new ByteArrayOutputStream();
        
        tomcatConf.save(contextXmlBaos, serverXmlBaos);
        
        TomcatConfJdbc tomcatConfAfterSave = new TomcatConfJdbc(
            () -> new ByteArrayInputStream(contextXmlBaos.toByteArray()),
            () -> new ByteArrayInputStream(serverXmlBaos.toByteArray()));
        
        List<ConnectionDto> connectionDtos = getConnections(tomcatConfAfterSave);
        responseJsonMap.put("_list", connectionDtos);
        
        saveAndWriteResponse(tomcatConf, environment, responseJsonMap, resp);
        
      } else {
        // no conf save, just write response
        List<ConnectionDto> connectionDtos = getConnections(tomcatConf);
        responseJsonMap.put("_list", connectionDtos);
        
        try (OutputStreamWriter osw = new OutputStreamWriter(resp.getOutputStream(), "UTF-8")) {
          new Gson().toJson(responseJsonMap, osw);
        }
      }
      
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
  
  /**
   * Validate mandatory fields
   * @param dto
   * @return list of field names whose values are empty (but must not be empty), or else empty list
   */
  private static List<String> validateMandatoryFields(ConnectionDto dto) {
    List<String> emptyFields = new ArrayList<>();

    if (empty(dto.getDb())) {
      emptyFields.add("db");
    }
    if (empty(dto.getName())) {
      emptyFields.add("name");
    }
    if (empty(dto.getPassword())) {
      emptyFields.add("password");
    }
    if (empty(dto.getServer())) {
      emptyFields.add("server");
    }
    if (empty(dto.getUser())) {
      emptyFields.add("user");
    }
    
    return emptyFields;
  }
  
  private static boolean empty(String string) {
    return string == null || "".equals(string);
  }
  
  private static ModStatus updateConnection(
      ModRequestBodyDto mreq, TomcatConfJdbc tomcatConf) {
    
    ModStatus ret;
    
    try {
      String location = mreq.getLocation();

      if (location == null) {

        ret = ModStatus.errLocationIsEmpty();
            
      } else {

        Map<String, Connection> connections = tomcatConf.getConnections();
        Connection connection = connections.get(location);

        if (connection == null) {

          ret = ModStatus.errItemNotFoundByLocation(location);

        } else {
          ConnectionDto connectionDto = mreq.getData();

          updateFields(connectionDto, connection);
          
          ret = ModStatus.success();
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      ret = ModStatus.errInternalError();
    }
    
    return ret;
  }
  
  /**
   * Updates target's fields with source's values
   * @param sourceDto
   * @param target non null
   * @return
   */
  private static void updateFields(ConnectionDto sourceDto, Connection target) {
    if (sourceDto.getActive() != null) {
      target.setActive(sourceDto.getActive());
    }
    if (sourceDto.getDb() != null) {
      target.setDb(sourceDto.getDb());
    }
    if (sourceDto.getName() != null) {
      target.setName(sourceDto.getName());
    }
    if (sourceDto.getPassword() != null) {
      target.setPassword(sourceDto.getPassword());
    }
    if (sourceDto.getServer() != null) {
      target.setServer(sourceDto.getServer());
    }
    if (sourceDto.getUser() != null) {
      target.setUser(sourceDto.getUser());
    }
  }
  
  private static ModStatus deleteConnection(
      ModRequestBodyDto mreq, TomcatConfJdbc tomcatConf) {
    
    ModStatus ret;

    try {
      String location = mreq.getLocation();

      if (location == null) {

        ret = ModStatus.errLocationIsEmpty();

      } else {

        Map<String, Connection> connections = tomcatConf.getConnections();
        Connection connection = connections.get(location);

        if (connection == null) {

          ret = ModStatus.errItemNotFoundByLocation(location);

        } else {
          tomcatConf.delete(location);

          ret = ModStatus.success();
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      ret = ModStatus.errInternalError();
    }
    
    return ret;
  }
  
  private static ModStatus createConnection(
      ModRequestBodyDto mreq, TomcatConfJdbc tomcatConf,
      ConnectionInitialParams connectionInitialParams) {

    try {
      ConnectionDto connectionDto = mreq.getData();

      
      // validate mandatory fields
      List<String> emptyMandatoryFields = validateMandatoryFields(connectionDto);
      if (!emptyMandatoryFields.isEmpty()) {
        return ModStatus.errMandatoryFieldsEmpty(emptyMandatoryFields);
      }
      

      Connection newConnection = tomcatConf.create(connectionInitialParams);

      updateFields(connectionDto, newConnection);

      return ModStatus.success();
      
    } catch (Throwable e) {
      e.printStackTrace();
      
      return ModStatus.errInternalError();
    }
  }
  
  /**
   * Assumes the resp has the {@code Content-Type=application/json;charset=UTF-8}
   * header already set
   * @param tomcatConf
   * @param environment
   * @param responseJsonMap
   * @param resp
   * @throws TransactionException
   * @throws IOException
   */
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
  }
  
  private static void ensure(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    // the content type is defined for the entire method
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

      
      Environment environment = EnvironmentFactory.get(req);
      
      TomcatConfJdbc tomcatConf = new TomcatConfJdbc(
          () -> environment.getContextXmlInputStream(), 
          () -> environment.getServerXmlInputStream());

      final Map<String, Connection> connections = tomcatConf.getConnections();

      EnsureConnectionResponseStatus respStatus;
      
      boolean confModified = false;
      
      try {
        

        // validate mandatory fields
        List<String> emptyMandatoryFields = validateMandatoryFields(connectionDto);
        if (!emptyMandatoryFields.isEmpty()) {
          respStatus = EnsureConnectionResponseStatus.errMandatoryFieldsEmpty(emptyMandatoryFields);

        } else {

          // deactivate all active connections with the same name
          List<Connection> existingActiveConnections = connections.values().stream()
              .filter(connection -> connectionDto.getName().equals(connection.getName()) && connection.isActive())
              .collect(Collectors.toList());
          
          
          // check the same existing connection
          boolean hasExistingSameConnection = existingActiveConnections.stream()
              .filter(connection -> connectionsEqual(connectionDto, connection)).findAny().isPresent(); 
              
          if (hasExistingSameConnection) {
            respStatus = EnsureConnectionResponseStatus.successExistedTheSame();
            
          } else {

            for (Connection connection: existingActiveConnections) {
              connection.setActive(false);
            }
            
            
            // create a new connection
            Connection newConnection = tomcatConf.create(environment.getJdbcConnectionInitialParams());
  
            updateFields(connectionDto, newConnection);
            
            if (existingActiveConnections.isEmpty()) {
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
        
      } else {
        // no conf save, just write response
        try (OutputStreamWriter osw = new OutputStreamWriter(resp.getOutputStream(), "UTF-8")) {
          new Gson().toJson(responseJsonMap, osw);
        }
      }
      
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
      
      // TODO set content type for the error case?
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
}
