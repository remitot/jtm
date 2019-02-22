package org.jepria.httpd.apache.manager.web.jk;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing status of an arbitrary data modification request
 */
public class ModStatus {
  
  /**
   * Modification succeeded
   */
  public static final int SC_SUCCESS = 0;
  /**
   * Client field data is invalid (incorrect format, or value processing exception)
   */
  public static final int SC_INVALID_FIELD_DATA = 1;
  
  /**
   * Id is empty
   */
  public static final int SC_EMPTY_ID = 2;
  
  /**
   * No item found by id
   */
  public static final int SC_NO_ITEM_FOUND_BY_ID = 3;
  
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
  public final Map<String, Object> invalidFieldDataMap;
  
  private ModStatus(int code, Map<String, Object> invalidFieldDataMap) {
    this.code = code;
    this.invalidFieldDataMap = invalidFieldDataMap;
  }

  public static ModStatus success() {
    return new ModStatus(SC_SUCCESS, null); 
  }
  
  /**
   * 
   * @param invalidFields tuples each of length 3: [fieldName1, fieldErrorCode1, errorMessage1, fieldName2, fieldErrorCode2, errorMessage2, ...]
   */
  public static ModStatus errInvalidFieldData(String...invalidFields) {
    if (invalidFields != null) {
      if (invalidFields.length % 3 != 0) {
        throw new IllegalArgumentException("Expected tuples each of length 3");
      }
      Map<String, Object> invalidFieldDataMap = new HashMap<>();
      for (int i = 0; i < invalidFields.length; i += 3) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("errorCode", invalidFields[i + 1]);
        errorMap.put("errorMessage", invalidFields[i + 2]);
        invalidFieldDataMap.put(invalidFields[i], errorMap);
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
  
  public static ModStatus errNoItemFoundById() {
    return new ModStatus(SC_NO_ITEM_FOUND_BY_ID, null);
  }
}
