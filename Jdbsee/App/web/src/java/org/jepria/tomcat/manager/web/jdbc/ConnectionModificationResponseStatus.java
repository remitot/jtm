package org.jepria.tomcat.manager.web.jdbc;

import java.util.List;

public class ConnectionModificationResponseStatus {
  
  
  public static final int SUCCESS = 0;
  public static final int ERR__CONNECTION_NOT_FOUND_BY_LOCATION = 1;
  public static final int ERR__LOCATION_IS_EMPTY = 2;
  public static final int ERR__MANDATORY_FIELDS_EMPTY = 3;
  public static final int ERR__ILLEGAL_ACTION = 4;
  public static final int ERR__INTERNAL_ERROR = 5;
  
  
  public final int code;
  public final String message;


  private ConnectionModificationResponseStatus(int code, String message) {
    this.code = code;
    this.message = message;
  }

  
  public static ConnectionModificationResponseStatus success() {
    return new ConnectionModificationResponseStatus(SUCCESS, "SUCCESS"); 
  }
  
  public static ConnectionModificationResponseStatus errConnectionNotFoundByLocation(String location) {
    return new ConnectionModificationResponseStatus(ERR__CONNECTION_NOT_FOUND_BY_LOCATION, "ERROR: no connection found by location '" + location + "'"); 
  }
  
  public static ConnectionModificationResponseStatus errLocationIsEmpty() {
    return new ConnectionModificationResponseStatus(ERR__LOCATION_IS_EMPTY, "ERROR: location is empty");
  }
  
  public static ConnectionModificationResponseStatus errMandatoryFieldsEmpty(List<String> fields) {
    String fieldsStr = fields == null || fields.isEmpty() ? null : fields.toString(); 
    return new ConnectionModificationResponseStatus(ERR__MANDATORY_FIELDS_EMPTY, "ERROR: mandatory fields in 'data' are empty" + 
        (fieldsStr == null ? "" : (": " + fieldsStr)));
  }

  public static ConnectionModificationResponseStatus errIllegalAction(String action) {
    return new ConnectionModificationResponseStatus(ERR__ILLEGAL_ACTION, "ERROR: illegal action" + 
        (action == null ? "" : (": " + action)));
  }
  
  public static ConnectionModificationResponseStatus errInternalError() {
    return new ConnectionModificationResponseStatus(ERR__INTERNAL_ERROR, "ERROR: internal error, ask server admin for more info");
  }
}
