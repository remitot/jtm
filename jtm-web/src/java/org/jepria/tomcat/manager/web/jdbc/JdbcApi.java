package org.jepria.tomcat.manager.web.jdbc;

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
        int nameCmp = o1.get("name").toLowerCase().compareTo(o2.get("name").toLowerCase());
        if (nameCmp == 0) {
          // the active is the first
          if ("true".equals(o1.get("active")) && "false".equals(o2.get("active"))) {
            return -1;
          } else if ("true".equals(o2.get("active")) && "false".equals(o1.get("active"))) {
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
    dto.put("active", Boolean.FALSE.equals(connection.isActive()) ? "false" : "true");
    dto.put("id", id);
    dto.put("name", connection.getName());
    dto.put("server", connection.getServer());
    dto.put("db", connection.getDb());
    dto.put("user", connection.getUser());
    dto.put("password", connection.getPassword());
    
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

    
    if (ret.allModSuccess) {
      // save modifications and add a new _list to the response
      
      // Note: it is safe to save modifications to context.xml file here (before servlet response), 
      // because although Tomcat reloads the context after context.xml modification, 
      // it still fulfills the servlet requests currently under processing. 
      tomcatConf.save(environment.getContextXmlOutputStream(), 
          environment.getServerXmlOutputStream());
    }
    
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
      final String name = connectionDto.get("name");
      if (name != null) {
        int validateNameResult = tomcatConf.validateNewResourceName(connectionDto.get("name"));
        if (validateNameResult == 1) {
          return ModStatus.errInvalidFieldData("name", "DUPLICATE_NAME");
        } else if (validateNameResult == 2) {
          return ModStatus.errInvalidFieldData("name", "DUPLICATE_GLOBAL");
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
        sourceDto.get("active") != null || sourceDto.get("server") != null 
        || sourceDto.get("db") != null || sourceDto.get("user") != null
        || sourceDto.get("password") != null)) {
      
      return ModStatus.errDataNotModifiable();
    }
    
    
    if (sourceDto.get("active") != null) {
      target.setActive(!"false".equals(sourceDto.get("active")));
    }
    if (sourceDto.get("db") != null) {
      target.setDb(sourceDto.get("db"));
    }
    if (sourceDto.get("name") != null) {
      target.setName(sourceDto.get("name"));
    }
    if (sourceDto.get("password") != null) {
      target.setPassword(sourceDto.get("password"));
    }
    if (sourceDto.get("server") != null) {
      target.setServer(sourceDto.get("server"));
    }
    if (sourceDto.get("user") != null) {
      target.setUser(sourceDto.get("user"));
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
        String[] invalidFields = new String[emptyMandatoryFields.size() * 2];
        int i = 0;
        for (String fieldName: emptyMandatoryFields) {
          invalidFields[i++] = fieldName;
          invalidFields[i++] = "MANDATORY_EMPTY";
        }
        return ModStatus.errInvalidFieldData(invalidFields);
      }
      
          
      // validate name
      int validateNameResult = tomcatConf.validateNewResourceName(connectionDto.get("name"));
      if (validateNameResult == 1) {
        return ModStatus.errInvalidFieldData("name", "DUPLICATE_NAME");
      } else if (validateNameResult == 2) {
        return ModStatus.errInvalidFieldData("name", "DUPLICATE_GLOBAL");
      }
      
      
      final Connection newConnection = tomcatConf.create(connectionDto.get("name"), initialParams);

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

    if (empty(dto.get("db"))) {
      emptyFields.add("db");
    }
    if (empty(dto.get("name"))) {
      emptyFields.add("name");
    }
    if (empty(dto.get("password"))) {
      emptyFields.add("password");
    }
    if (empty(dto.get("server"))) {
      emptyFields.add("server");
    }
    if (empty(dto.get("user"))) {
      emptyFields.add("user");
    }
    
    return emptyFields;
  }
  
  protected boolean empty(String string) {
    return string == null || "".equals(string);
  }
}
