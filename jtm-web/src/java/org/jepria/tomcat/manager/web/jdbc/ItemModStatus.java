package org.jepria.tomcat.manager.web.jdbc;

import java.util.Map;

/**
 * Class representing status of a single data item modification request
 */
public class ItemModStatus {
  
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
   * SC_* constant value
   */
  public final int code;
  /**
   * if {@link #code} == {@link #SC_INVALID_FIELD_DATA} only: invalid field names mapped to error codes
   */
  public final Map<String, InvalidFieldDataCode> invalidFieldDataMap;
  
  private ItemModStatus(int code, Map<String, InvalidFieldDataCode> invalidFieldDataMap) {
    this.code = code;
    this.invalidFieldDataMap = invalidFieldDataMap;
  }

  public static ItemModStatus success() {
    return new ItemModStatus(SC_SUCCESS, null); 
  }
  
  public static enum InvalidFieldDataCode {
    EMPTY,
    MANDATORY_EMPTY,
    DUPLICATE_NAME,
    DUPLICATE_GLOBAL,
  }
  
  /**
   * 
   * @param invalidFieldDataMap {@code Map<fieldName, errorCode>}
   */
  public static ItemModStatus errInvalidFieldData(Map<String, InvalidFieldDataCode> invalidFieldDataMap) {
    return new ItemModStatus(SC_INVALID_FIELD_DATA, invalidFieldDataMap);
  }
  
  
  public static ItemModStatus errEmptyId() {
    return new ItemModStatus(SC_EMPTY_ID, null);
  }
  
  public static ItemModStatus errDataNotModifiable() {
    return new ItemModStatus(SC_DATA_NOT_MODIFIABLE, null);
  }
  
  public static ItemModStatus errNoItemFoundById() {
    return new ItemModStatus(SC_NO_ITEM_FOUND_BY_ID, null);
  }
}
