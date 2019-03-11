package org.jepria.tomcat.manager.web.jdbc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jepria.tomcat.manager.core.jdbc.Connection;
import org.jepria.tomcat.manager.core.jdbc.ResourceInitialParams;
import org.jepria.tomcat.manager.core.jdbc.TomcatConfJdbc;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;

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
  
  protected Comparator<Map<String, String>> connectionSorter() {
    return new Comparator<Map<String, String>>() {
      @Override
      public int compare(Map<String, String> o1, Map<String, String> o2) {
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
    public Map<String, ItemModStatus> itemModStatusMap;
    
    /**
     * all modRequests succeeded
     */
    public boolean allModSuccess;
  }
  
  protected ItemModStatus updateConnection(String id,
      Map<String, String> fields, TomcatConfJdbc tomcatConf) {
    
    try {
      if (id == null) {
        return ItemModStatus.errEmptyId();
      }

      final Map<String, Connection> connections = tomcatConf.getConnections();
      final Connection connection = connections.get(id);

      if (connection == null) {
        return ItemModStatus.errNoItemFoundById();
      }
        
      // validate name
      final String name = fields.get("name");
      if (name != null) {
        int validateNameResult = tomcatConf.validateNewResourceName(fields.get("name"));
        if (validateNameResult == 1) {
          final Map<String, ItemModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
          invalidFieldDataMap.put("name", ItemModStatus.InvalidFieldDataCode.DUPLICATE_NAME);
          return ItemModStatus.errInvalidFieldData(invalidFieldDataMap);
        } else if (validateNameResult == 2) {
          final Map<String, ItemModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
          invalidFieldDataMap.put("name", ItemModStatus.InvalidFieldDataCode.DUPLICATE_GLOBAL);
          return ItemModStatus.errInvalidFieldData(invalidFieldDataMap);
        }
      }
      
      return updateFields(fields, connection);
      
    } catch (Throwable e) {
      e.printStackTrace();
      
      return ItemModStatus.errServerException();
    }
  }
  
  /**
   * Updates target's fields with source's values
   * @param fields
   * @param target non null
   * @return
   */
  protected ItemModStatus updateFields(Map<String, String> fields, Connection target) {
    
    // validate illegal action due to dataModifiable field
    if (!target.isDataModifiable() && (
        fields.get("active") != null || fields.get("server") != null 
        || fields.get("db") != null || fields.get("user") != null
        || fields.get("password") != null)) {
      
      return ItemModStatus.errDataNotModifiable();
    }
    
    
    if (fields.get("active") != null) {
      target.setActive(!"false".equals(fields.get("active")));
    }
    if (fields.get("db") != null) {
      target.setDb(fields.get("db"));
    }
    if (fields.get("name") != null) {
      target.setName(fields.get("name"));
    }
    if (fields.get("password") != null) {
      target.setPassword(fields.get("password"));
    }
    if (fields.get("server") != null) {
      target.setServer(fields.get("server"));
    }
    if (fields.get("user") != null) {
      target.setUser(fields.get("user"));
    }
    
    return ItemModStatus.success();
  }
  
  protected ItemModStatus deleteConnection(String id, TomcatConfJdbc tomcatConf) {

    try {
      if (id == null) {
        return ItemModStatus.errEmptyId();
      }


      Map<String, Connection> connections = tomcatConf.getConnections();
      Connection connection = connections.get(id);

      if (connection == null) {
        return ItemModStatus.errNoItemFoundById();
      }
        
      if (!connection.isDataModifiable()) {
        return ItemModStatus.errDataNotModifiable();
      }
      
      tomcatConf.delete(id);

      return ItemModStatus.success();
      
    } catch (Throwable e) {
      e.printStackTrace();
      
      return ItemModStatus.errServerException();
    }
  }
  
  protected ItemModStatus createConnection(
      Map<String, String> fields, TomcatConfJdbc tomcatConf,
      ResourceInitialParams initialParams) {

    try {
      // validate mandatory fields
      List<String> emptyMandatoryFields = validateMandatoryFields(fields);
      if (!emptyMandatoryFields.isEmpty()) {
        Map<String, ItemModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
        for (String fieldName: emptyMandatoryFields) {
          invalidFieldDataMap.put(fieldName, ItemModStatus.InvalidFieldDataCode.MANDATORY_EMPTY);
        }
        return ItemModStatus.errInvalidFieldData(invalidFieldDataMap);
      }
      
          
      // validate name
      int validateNameResult = tomcatConf.validateNewResourceName(fields.get("name"));
      if (validateNameResult == 1) {
        final Map<String, ItemModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
        invalidFieldDataMap.put("name", ItemModStatus.InvalidFieldDataCode.DUPLICATE_NAME);
        return ItemModStatus.errInvalidFieldData(invalidFieldDataMap);
      } else if (validateNameResult == 2) {
        final Map<String, ItemModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
        invalidFieldDataMap.put("name", ItemModStatus.InvalidFieldDataCode.DUPLICATE_GLOBAL);
        return ItemModStatus.errInvalidFieldData(invalidFieldDataMap);
      }
      
      
      final Connection newConnection = tomcatConf.create(fields.get("name"), initialParams);

      return updateFields(fields, newConnection);
      
    } catch (Throwable e) {
      e.printStackTrace();
      
      return ItemModStatus.errServerException();
    }
  }
  
  /**
   * Validate mandatory fields
   * @param dto
   * @return list of field names whose values are empty (but must not be empty), or else empty list
   */
  protected List<String> validateMandatoryFields(Map<String, String> fields) {
    List<String> emptyFields = new ArrayList<>();

    if (empty(fields.get("db"))) {
      emptyFields.add("db");
    }
    if (empty(fields.get("name"))) {
      emptyFields.add("name");
    }
    if (empty(fields.get("password"))) {
      emptyFields.add("password");
    }
    if (empty(fields.get("server"))) {
      emptyFields.add("server");
    }
    if (empty(fields.get("user"))) {
      emptyFields.add("user");
    }
    
    return emptyFields;
  }
  
  protected boolean empty(String string) {
    return string == null || "".equals(string);
  }
}
