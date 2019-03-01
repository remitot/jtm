package org.jepria.tomcat.manager.web.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jepria.tomcat.manager.core.jdbc.Connection;
import org.jepria.tomcat.manager.core.jdbc.ResourceInitialParams;
import org.jepria.tomcat.manager.core.jdbc.TomcatConfJdbc;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ModRequestBodyDto;

public class JdbcApi {
  
  protected boolean isCreateContextResources(Environment environment) {
    return "true".equals(environment.getProperty("org.jepria.tomcat.manager.web.jdbc.createContextResources"));
  }
  
  public List<ConnectionDto> list(Environment environment) {
    
    final TomcatConfJdbc tomcatConf = new TomcatConfJdbc(
        () -> environment.getContextXmlInputStream(), 
        () -> environment.getServerXmlInputStream(),
        isCreateContextResources(environment));
    
    return getConnections(tomcatConf);
  }
  
  protected List<ConnectionDto> getConnections(TomcatConfJdbc tomcatConf) {
    Map<String, Connection> connections = tomcatConf.getConnections();

    // list all connections
    return connections.entrySet().stream().map(
        entry -> connectionToDto(entry.getKey(), entry.getValue()))
        .sorted(connectionSorter()).collect(Collectors.toList());
  }
  
  protected Comparator<ConnectionDto> connectionSorter() {
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

  protected ConnectionDto connectionToDto(String id, Connection connection) {
    Objects.requireNonNull(id);
    
    ConnectionDto dto = new ConnectionDto();
    
    dto.setDataModifiable(connection.isDataModifiable());
    dto.setActive(connection.isActive());
    dto.setDb(connection.getDb());
    dto.setId(id);
    dto.setName(connection.getName());
    dto.setPassword(connection.getPassword());
    dto.setServer(connection.getServer());
    dto.setUser(connection.getUser());
    
    return dto;
  }
  
  public static class ModResponse {
    /**
     * {@code Map<modRequestId, modStatus>}
     */
    public Map<String, ModStatus> modStatusMap;
    
    /**
     * all modRequests succeeded
     */
    public boolean allModSuccess;
    
    /**
     * Optional (only if all modRequests succeeded): list of connections after all modifications performed
     */
    public List<ConnectionDto> list;
  }
  
  public ModResponse mod(Environment environment, Map<String, ModRequestBodyDto> modRequestBodyMap) {
    
    final ModResponse ret = new ModResponse();
    
    final TomcatConfJdbc tomcatConf = new TomcatConfJdbc(
        () -> environment.getContextXmlInputStream(), 
        () -> environment.getServerXmlInputStream(),
        isCreateContextResources(environment));

    // collect processed modRequests
    final Set<String> processedModRequestIds = new HashSet<>();
    
    
    ret.allModSuccess = true; 
    ret.modStatusMap = new HashMap<>();
    
    // 1) perform all updates
    for (String modRequestId: modRequestBodyMap.keySet()) {
      ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);

      if ("update".equals(mreq.getAction())) {
        processedModRequestIds.add(modRequestId);

        ModStatus modStatus = updateConnection(mreq, tomcatConf);
        if (modStatus.code != ModStatus.SC_SUCCESS) {
          ret.allModSuccess = false;
        }

        ret.modStatusMap.put(modRequestId, modStatus);
      }
    }


    // 2) perform all deletions
    for (String modRequestId: modRequestBodyMap.keySet()) {
      ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);

      if ("delete".equals(mreq.getAction())) {
        processedModRequestIds.add(modRequestId);

        ModStatus modStatus = deleteConnection(mreq, tomcatConf);
        if (modStatus.code != ModStatus.SC_SUCCESS) {
          ret.allModSuccess = false;
        }
        
        ret.modStatusMap.put(modRequestId, modStatus);
      }
    }


    // 3) perform all creations
    for (String modRequestId: modRequestBodyMap.keySet()) {
      ModRequestBodyDto mreq = modRequestBodyMap.get(modRequestId);

      if ("create".equals(mreq.getAction())) {
        processedModRequestIds.add(modRequestId);

        ModStatus modStatus = createConnection(mreq, tomcatConf, 
            environment.getResourceInitialParams());
        if (modStatus.code != ModStatus.SC_SUCCESS) {
          ret.allModSuccess = false;
        }
        
        ret.modStatusMap.put(modRequestId, modStatus);
      }
    }


    // 4) ignore illegal actions

    
    // list after save
    final List<ConnectionDto> listAfterSave;
    
    if (ret.allModSuccess) {
      // save modifications and add a new _list to the response
      
      // fake save: to be sure, save the modified conf to a temporary storage and get after-save list from there
      ByteArrayOutputStream contextXmlBaos = new ByteArrayOutputStream();
      ByteArrayOutputStream serverXmlBaos = new ByteArrayOutputStream();
      
      tomcatConf.save(contextXmlBaos, serverXmlBaos);
      
      final TomcatConfJdbc tomcatConfAfterSave = new TomcatConfJdbc(
          () -> new ByteArrayInputStream(contextXmlBaos.toByteArray()),
          () -> new ByteArrayInputStream(serverXmlBaos.toByteArray()),
          isCreateContextResources(environment));
      
      listAfterSave = getConnections(tomcatConfAfterSave);
      
      
      // real save: it is safe to save modifications to context.xml file here (before servlet response), 
      // because although Tomcat is about to reload the context after such saving, 
      // it still fulfills the servlet requests currently under processing. 
      tomcatConf.save(environment.getContextXmlOutputStream(), 
          environment.getServerXmlOutputStream());
      
    } else {
      
      listAfterSave = null;
    }
    
    ret.list = listAfterSave;
    
    
    return ret;
  }
  
  protected ModStatus updateConnection(
      ModRequestBodyDto mreq, TomcatConfJdbc tomcatConf) {
    
    try {
      final String id = mreq.getId();

      if (id == null) {
        return ModStatus.errEmptyId();
      }

      final Map<String, Connection> connections = tomcatConf.getConnections();
      final Connection connection = connections.get(id);

      if (connection == null) {
        return ModStatus.errNoItemFoundById();
      }
        
      final ConnectionDto connectionDto = mreq.getData();
      
      
      // validate name
      final String name = connectionDto.getName();
      if (name != null) {
        int validateNameResult = tomcatConf.validateNewResourceName(connectionDto.getName());
        if (validateNameResult == 1) {
          return ModStatus.errInvalidFieldData("name", "DUPLICATE_NAME", null);
        } else if (validateNameResult == 2) {
          return ModStatus.errInvalidFieldData("name", "DUPLICATE_GLOBAL", null);
        }
      }
      
      
      return updateFields(connectionDto, connection);
      
    } catch (Throwable e) {
      e.printStackTrace();
      
      return ModStatus.errServerException();
    }
  }
  
  /**
   * Updates target's fields with source's values
   * @param sourceDto
   * @param target non null
   * @return
   */
  protected ModStatus updateFields(ConnectionDto sourceDto, Connection target) {
    
    // validate illegal action due to dataModifiable field
    if (!target.isDataModifiable() && (
        sourceDto.getActive() != null || sourceDto.getServer() != null 
        || sourceDto.getDb() != null || sourceDto.getUser() != null
        || sourceDto.getPassword() != null)) {
      
      return ModStatus.errDataNotModifiable();
    }
    
    
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
    
    return ModStatus.success();
  }
  
  protected ModStatus deleteConnection(
      ModRequestBodyDto mreq, TomcatConfJdbc tomcatConf) {

    try {
      String id = mreq.getId();

      if (id == null) {
        return ModStatus.errEmptyId();
      }


      Map<String, Connection> connections = tomcatConf.getConnections();
      Connection connection = connections.get(id);

      if (connection == null) {
        return ModStatus.errNoItemFoundById();
      }
        
      if (!connection.isDataModifiable()) {
        return ModStatus.errDataNotModifiable();
      }
      
      tomcatConf.delete(id);

      return ModStatus.success();
      
    } catch (Throwable e) {
      e.printStackTrace();
      
      return ModStatus.errServerException();
    }
  }
  
  protected ModStatus createConnection(
      ModRequestBodyDto mreq, TomcatConfJdbc tomcatConf,
      ResourceInitialParams initialParams) {

    try {
      ConnectionDto connectionDto = mreq.getData();

      
      // validate mandatory fields
      List<String> emptyMandatoryFields = validateMandatoryFields(connectionDto);
      if (!emptyMandatoryFields.isEmpty()) {
        String[] invalidFields = new String[emptyMandatoryFields.size() * 3];
        int i = 0;
        for (String fieldName: emptyMandatoryFields) {
          invalidFields[i++] = fieldName;
          invalidFields[i++] = "MANDATORY_EMPTY";
          invalidFields[i++] = null;
        }
        return ModStatus.errInvalidFieldData(invalidFields);
      }
      
          
      // validate name
      int validateNameResult = tomcatConf.validateNewResourceName(connectionDto.getName());
      if (validateNameResult == 1) {
        return ModStatus.errInvalidFieldData("name", "DUPLICATE_NAME");
      } else if (validateNameResult == 2) {
        return ModStatus.errInvalidFieldData("name", "DUPLICATE_GLOBAL");
      }
      
      
      final Connection newConnection = tomcatConf.create(connectionDto.getName(), initialParams);

      return updateFields(connectionDto, newConnection);
      
    } catch (Throwable e) {
      e.printStackTrace();
      
      return ModStatus.errServerException();
    }
  }
  
  /**
   * Validate mandatory fields
   * @param dto
   * @return list of field names whose values are empty (but must not be empty), or else empty list
   */
  protected List<String> validateMandatoryFields(ConnectionDto dto) {
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
  
  protected boolean empty(String string) {
    return string == null || "".equals(string);
  }
}
