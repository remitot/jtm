package org.jepria.tomcat.manager.web.jdbc;

import java.util.HashMap;
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
   * if {@link #code} == {@link #SC_INVALID_FIELD_DATA} only: invalid field name mapped to error in format {errorCode: ..., errorMessage: ...}
   */
  public final Map<String, String> invalidFieldDataMap;
  
  private ModStatus(int code, Map<String, String> invalidFieldDataMap) {
    this.code = code;
    this.invalidFieldDataMap = invalidFieldDataMap;
  }

  public static ModStatus success() {
    return new ModStatus(SC_SUCCESS, null); 
  }
  
  /**
   * 
   * @param invalidFields tuples each of length 2: [fieldName1, fieldErrorCode1, fieldName2, fieldErrorCode2, ...]
   */
  public static ModStatus errInvalidFieldData(String...invalidFields) {
    if (invalidFields != null) {
      if (invalidFields.length % 2 != 0) {
        throw new IllegalArgumentException("Expected tuples each of length 2");
      }
      Map<String, String> invalidFieldDataMap = new HashMap<>();
      for (int i = 0; i < invalidFields.length; i += 3) {
        invalidFieldDataMap.put(invalidFields[i], invalidFields[i + 1]);
      }
      return new ModStatus(SC_INVALID_FIELD_DATA, invalidFieldDataMap);
    } else {
      return new ModStatus(SC_INVALID_FIELD_DATA, null);
    }
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
