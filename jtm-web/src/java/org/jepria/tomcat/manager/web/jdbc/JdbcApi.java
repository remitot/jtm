package org.jepria.tomcat.manager.web.jdbc;

import org.jepria.tomcat.manager.core.jdbc.Connection;
import org.jepria.tomcat.manager.core.jdbc.ResourceInitialParams;
import org.jepria.tomcat.manager.core.jdbc.TomcatConfJdbc;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;

import java.util.*;
import java.util.stream.Collectors;

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
          if ("true".equals(o1.getActive()) && "false".equals(o2.getActive())) {
            return -1;
          } else if ("true".equals(o2.getActive()) && "false".equals(o1.getActive())) {
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
    dto.setActive(Boolean.FALSE.equals(connection.isActive()) ? "false" : "true");
    dto.setId(id);
    dto.setName(connection.getName());
    dto.setServer(connection.getServer());
    dto.setDb(connection.getDb());
    dto.setUser(connection.getUser());
    dto.setPassword(connection.getPassword());
    
    return dto;
  }
  
  /**
   * Class representing status of a single data item modification request
   */
  public static class ItemModStatus {
    
    public enum Code {
      /**
       * Modification succeeded
       */
      SUCCESS,
      /**
       * Client field data is invalid (incorrect format, or value processing exception)
       */
      INVALID_FIELD_DATA,
    }
    
    public final Code code;
    
    /**
     * Only in case of {@link #code} == {@link Code#INVALID_FIELD_DATA}: invalid field names mapped to error codes
     */
    public final Map<String, InvalidFieldDataCode> invalidFieldDataMap;
    
    private ItemModStatus(Code code, Map<String, InvalidFieldDataCode> invalidFieldDataMap) {
      this.code = code;
      this.invalidFieldDataMap = invalidFieldDataMap;
    }

    public static ItemModStatus success() {
      return new ItemModStatus(Code.SUCCESS, null); 
    }
    
    /**
     * Field invalidity description code
     */
    public static enum InvalidFieldDataCode {
      MANDATORY_EMPTY,
      DUPLICATE_NAME,
      DUPLICATE_GLOBAL,
    }
    
    /**
     * 
     * @param invalidFieldDataMap {@code Map<fieldName, errorCode>}
     */
    public static ItemModStatus errInvalidFieldData(Map<String, InvalidFieldDataCode> invalidFieldDataMap) {
      return new ItemModStatus(Code.INVALID_FIELD_DATA, invalidFieldDataMap);
    }
  }

  /**
   * 
   * @param id
   * @param updatedFields contains updated field values only; null for non-updated fields; "" for deleted values
   * @param tomcatConf
   * @return
   */
  public ItemModStatus updateConnection(String id,
      ConnectionDto updatedFields, TomcatConfJdbc tomcatConf) {
    
    Objects.requireNonNull(id, "id must not be null");
    
    final Map<String, ItemModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
    
    // validate empty but non-null fields
    List<String> emptyFields = validateEmptyFieldsForUpdate(updatedFields);
    if (!emptyFields.isEmpty()) {
      for (String fieldName: emptyFields) {
        invalidFieldDataMap.put(fieldName, ItemModStatus.InvalidFieldDataCode.MANDATORY_EMPTY);
      }
    }
    
    // validate name
    final String name = updatedFields.getName();
    if (name != null) {
      switch (tomcatConf.validateNewResourceName(updatedFields.getName())) {
      case DUPLICATE_NAME: {
        // putIfAbsent: do not overwrite previous invalid state
        invalidFieldDataMap.putIfAbsent("name", ItemModStatus.InvalidFieldDataCode.DUPLICATE_NAME);
        break;
      }
      case DUPLICATE_GLOBAL: {
        // do nothing: DUPLICATE_GLOBAL check is needed on connection create only
        break;
      }
      default:
      }
    }
    
    if (!invalidFieldDataMap.isEmpty()) {
      return ItemModStatus.errInvalidFieldData(invalidFieldDataMap);
    }
    
    
    

    final Map<String, Connection> connections = tomcatConf.getConnections();
    final Connection connection = connections.get(id);

    if (connection == null) {
      throw new IllegalStateException("No resource found by such id=[" + id + "]");
    }
    
    if (!validateDataModifiable(updatedFields, connection)) {
      throw new IllegalStateException("Cannot modify the unmodifiable fields of the resource by id=[" + id + "]");
    }
    
    return updateFields(updatedFields, connection);
  }

  /**
   * 
   * @param updatedFields contains updated field values only; null for non-updated fields; "" for deleted values
   * @param target
   * @return
   */
  protected boolean validateDataModifiable(ConnectionDto updatedFields, Connection target) {
    // validate illegal action due to dataModifiable field
    if (!target.isDataModifiable() && (
        updatedFields.getActive() != null || updatedFields.getServer() != null 
        || updatedFields.getDb() != null || updatedFields.getUser() != null
        || updatedFields.getPassword() != null)) {
      return false;
    }
    return true;
  }
  
  
  /**
   * Updates target's fields with source's values
   * @param updatedFields contains updated field values only; null for non-updated fields; "" for deleted values
   * @param target non null
   * @return
   */
  protected ItemModStatus updateFields(ConnectionDto updatedFields, Connection target) {
    
    if (updatedFields.getActive() != null) {
      target.setActive(!"false".equals(updatedFields.getActive()));
    }
    if (updatedFields.getDb() != null) {
      target.setDb(updatedFields.getDb());
    }
    if (updatedFields.getName() != null) {
      target.setName(updatedFields.getName());
    }
    if (updatedFields.getPassword() != null) {
      target.setPassword(updatedFields.getPassword());
    }
    if (updatedFields.getServer() != null) {
      target.setServer(updatedFields.getServer());
    }
    if (updatedFields.getUser() != null) {
      target.setUser(updatedFields.getUser());
    }
    
    return ItemModStatus.success();
  }
  
  /**
   * 
   * @param id non-null
   * @param tomcatConf
   * @return
   */
  public ItemModStatus deleteConnection(String id, TomcatConfJdbc tomcatConf) {

    Objects.requireNonNull(id, "id must not be null");

    Map<String, Connection> connections = tomcatConf.getConnections();
    Connection connection = connections.get(id);

    if (connection == null) {
      throw new IllegalStateException("No resource found by such id=[" + id + "]");
    }
      
    if (!connection.isDataModifiable()) {
      throw new IllegalStateException("Cannot delete the unmodifiable resource by id=[" + id + "]");
    }
    
    tomcatConf.delete(id);

    return ItemModStatus.success();
  }

  /**
   * 
   * @param createdFields contains created field values only; null for fields which are not about to be created
   * @param tomcatConf
   * @param initialParams
   * @return
   */
  public ItemModStatus createConnection(
      ConnectionDto createdFields, TomcatConfJdbc tomcatConf,
      ResourceInitialParams initialParams) {

    final Map<String, ItemModStatus.InvalidFieldDataCode> invalidFieldDataMap = new HashMap<>();
    
    // validate mandatory empty fields
    List<String> emptyMandatoryFields = validateEmptyFieldForCreate(createdFields);
    if (!emptyMandatoryFields.isEmpty()) {
      for (String fieldName: emptyMandatoryFields) {
        invalidFieldDataMap.put(fieldName, ItemModStatus.InvalidFieldDataCode.MANDATORY_EMPTY);
      }
    }
    
        
    // validate name
    switch(tomcatConf.validateNewResourceName(createdFields.getName())) {
    case DUPLICATE_NAME: {
      // putIfAbsent: do not overwrite previous invalid state
      invalidFieldDataMap.putIfAbsent("name", ItemModStatus.InvalidFieldDataCode.DUPLICATE_NAME);
      break;
    }
    case DUPLICATE_GLOBAL: {
      // putIfAbsent: do not overwrite previous invalid state
      invalidFieldDataMap.putIfAbsent("name", ItemModStatus.InvalidFieldDataCode.DUPLICATE_GLOBAL);
      break;
    }
    default:
    }

    if (!invalidFieldDataMap.isEmpty()) {
      return ItemModStatus.errInvalidFieldData(invalidFieldDataMap);
    }
    
    final Connection newConnection = tomcatConf.create(createdFields.getName(), initialParams);

    
    if (!validateDataModifiable(createdFields, newConnection)) {
      throw new IllegalStateException("Cannot set values for unmodifiable fields of the new resource");
    }
    
    return updateFields(createdFields, newConnection);
  }
  
  /**
   * Validate empty fields for create
   * @param createdFields contains created field values only; null for fields which are not about to be created
   * @return list of invalidly empty or missing mandatory fields, or else empty list, not null
   */
  protected List<String> validateEmptyFieldForCreate(ConnectionDto createdFields) {
    List<String> emptyFields = new ArrayList<>();

    // the fields must be neither null, nor empty
    String db = createdFields.getDb();
    if (db == null || "".equals(db)) {
      emptyFields.add("db");
    }
    String name = createdFields.getName(); 
    if (name == null || "".equals(name)) {
      emptyFields.add("name");
    }
    String password = createdFields.getPassword();
    if (password == null || "".equals(password)) {
      emptyFields.add("password");
    }
    String server = createdFields.getServer();
    if (server == null || "".equals(server)) {
      emptyFields.add("server");
    }
    String user = createdFields.getUser();
    if (user == null || "".equals(user)) {
      emptyFields.add("user");
    }
    
    return emptyFields;
  }
  
  /**
   * Validate empty fields for update
   * @param updatedFields contains updated field values only; null for non-updated fields; "" for deleted values
   * @return list of invalidly empty fields, or else empty list, not null
   */
  protected List<String> validateEmptyFieldsForUpdate(ConnectionDto updatedFields) {
    List<String> emptyFields = new ArrayList<>();

    // the fields may be null, but if not null then not empty
    if ("".equals(updatedFields.getDb())) {
      emptyFields.add("db");
    }
    if ("".equals(updatedFields.getName())) {
      emptyFields.add("name");
    }
    if ("".equals(updatedFields.getPassword())) {
      emptyFields.add("password");
    }
    if ("".equals(updatedFields.getServer())) {
      emptyFields.add("server");
    }
    if ("".equals(updatedFields.getUser())) {
      emptyFields.add("user");
    }
    
    return emptyFields;
  }
}
