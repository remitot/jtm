package org.jepria.httpd.apache.manager.web.jk;

import java.util.HashMap;
import java.util.Map;

public class ModStatus {
  
  /**
   * Modification succeeded
   */
  public static final int SC_SUCCESS = 0;
  /**
   * Client field data is invalid (incorrect format or value processing exception)
   */
  public static final int SC_INVALID_FIELD_DATA = 1;
  /**
   * Any server data processing exception
   */
  public static final int SC_SERVER_EXCEPTION = 2;
  
  /**
   * SC_* constant value
   */
  public final int code;
  /**
   * if {@link #code} == {@link #SC_INVALID_FIELD_DATA} only: invalid field name mapped to any error message or a meta tag
   */
  public final Map<String, String> invalidFieldData;
  
  private ModStatus(int code, Map<String, String> invalidFieldData) {
    this.code = code;
    this.invalidFieldData = invalidFieldData;
  }

  public static ModStatus success() {
    return new ModStatus(SC_SUCCESS, null); 
  }
  
  public static ModStatus errInvalidFieldData(Map<String, String> invalidFieldData) {
    return new ModStatus(SC_INVALID_FIELD_DATA, invalidFieldData); 
  }
  
  public static ModStatus errServerException() {
    return new ModStatus(SC_SERVER_EXCEPTION, null); 
  }
  
  /////////////////////////////////
  
  public static ModStatus errLocationIsEmpty() {
    Map<String, String> invalidFieldData = new HashMap<>();
    invalidFieldData.put("location", "EMPTY");
    return ModStatus.errInvalidFieldData(invalidFieldData);
  }
  
  public static ModStatus errItemNotFoundByLocation() {
    Map<String, String> invalidFieldData = new HashMap<>();
    invalidFieldData.put("location", "NO_ITEM_FOUND_BY_LOCATION");
    return ModStatus.errInvalidFieldData(invalidFieldData);
  }
}
