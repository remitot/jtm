package org.jepria.tomcat.manager.web.jdbc;

import java.util.Map;

/**
 * Class representing status of an arbitrary data modification request
 */
public class ModStatus {
  
  /**
   * Modification succeeded
   */
  public static final int SC_SUCCESS = 0;// the constant value referred from table.js
  /**
   * Client field data is invalid (incorrect format, or value processing exception)
   */
  public static final int SC_INVALID_FIELD_DATA = 1;// the constant value referred from table.js
  
  /**
   * Empty id.
   */
  public static final int SC_EMPTY_ID = 2;// the constant value referred from table.js
  
  /**
   * No item found by id
   */
  public static final int SC_NO_ITEM_FOUND_BY_ID = 3;// the constant value referred from table.js
  
  /**
   * Data not modifiable
   */
  public static final int SC_DATA_NOT_MODIFIABLE = 4;// the constant value referred from table.js
  /**
   * Any server data processing exception
   */
  public static final int SC_SERVER_EXCEPTION = 500;
  
  /**
   * SC_* constant value
   */
  public final int code;
  /**
   * if {@link #code} == {@link #SC_INVALID_FIELD_DATA} only: invalid field names mapped to error codes
   */
  public final Map<String, InvalidFieldDataCode> invalidFieldDataMap;
  
  private ModStatus(int code, Map<String, InvalidFieldDataCode> invalidFieldDataMap) {
    this.code = code;
    this.invalidFieldDataMap = invalidFieldDataMap;
  }

  public static ModStatus success() {
    return new ModStatus(SC_SUCCESS, null); 
  }
  
  public static enum InvalidFieldDataCode {
    MANDATORY_EMPTY,
    DUPLICATE_NAME,
    DUPLICATE_GLOBAL,
  }
  
  /**
   * 
   * @param invalidFieldDataMap {@code Map<fieldName, errorCode>}
   */
  public static ModStatus errInvalidFieldData(Map<String, InvalidFieldDataCode> invalidFieldDataMap) {
    return new ModStatus(SC_INVALID_FIELD_DATA, invalidFieldDataMap);
  }
  
  public static ModStatus errServerException() {
    return new ModStatus(SC_SERVER_EXCEPTION, null); 
  }
  
  
  public static ModStatus errEmptyId() {
    return new ModStatus(SC_EMPTY_ID, null);
  }
  
  public static ModStatus errDataNotModifiable() {
    return new ModStatus(SC_DATA_NOT_MODIFIABLE, null);
  }
  
  public static ModStatus errNoItemFoundById() {
    return new ModStatus(SC_NO_ITEM_FOUND_BY_ID, null);
  }
}
