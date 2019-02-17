package org.jepria.tomcat.manager.web.jdbc;

import java.util.List;

public class ModStatus {
  
  
  public static final int CODE_SUCCESS = 0;
  public static final int CODE_ERR__ITEM_NOT_FOUND_BY_LOCATION = 1;
  public static final int CODE_ERR__LOCATION_IS_EMPTY = 2;
  public static final int CODE_ERR__MANDATORY_FIELDS_EMPTY = 3;
  public static final int CODE_ERR__ILLEGAL_ACTION = 4;
  public static final int CODE_ERR__DATA_NOT_MODIFIABLE = 5;
  public static final int CODE_ERR__INTERNAL_ERROR = 500;
  
  
  public final int code;
  public final String message;


  private ModStatus(int code, String message) {
    this.code = code;
    this.message = message;
  }

  
  public static ModStatus success() {
    return new ModStatus(CODE_SUCCESS, "SUCCESS"); 
  }
  
  public static ModStatus errItemNotFoundByLocation(String location) {
    return new ModStatus(CODE_ERR__ITEM_NOT_FOUND_BY_LOCATION, "ERROR: no item found by location '" + location + "'"); 
  }
  
  public static ModStatus errLocationIsEmpty() {
    return new ModStatus(CODE_ERR__LOCATION_IS_EMPTY, "ERROR: location is empty");
  }
  
  public static ModStatus errMandatoryFieldsEmpty(List<String> fields) {
    String fieldsStr = fields == null || fields.isEmpty() ? null : fields.toString(); 
    return new ModStatus(CODE_ERR__MANDATORY_FIELDS_EMPTY, "ERROR: mandatory fields in 'data' are empty" + 
        (fieldsStr == null ? "" : (": " + fieldsStr)));
  }

  public static ModStatus errIllegalAction(String action) {
    return new ModStatus(CODE_ERR__ILLEGAL_ACTION, "ERROR: illegal action" + 
        (action == null ? "" : (": " + action)));
  }
  
  public static ModStatus errDataNotModifiable() {
    return new ModStatus(CODE_ERR__DATA_NOT_MODIFIABLE, "ERROR: data is not modifiable");
  }
  
  public static ModStatus errInternalError() {
    return new ModStatus(CODE_ERR__INTERNAL_ERROR, "ERROR: internal error, ask server admin for more info");
  }
}
