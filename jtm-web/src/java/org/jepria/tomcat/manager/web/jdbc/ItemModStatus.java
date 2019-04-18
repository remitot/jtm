package org.jepria.tomcat.manager.web.jdbc;

import java.util.Map;

/**
 * Class representing status of a single data item modification request
 */
public class ItemModStatus {
  
  public static enum Code {
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
   * Only in case of {@link #code} == {@link InvalidFieldDataCode#INVALID_FIELD_DATA}: invalid field names mapped to error codes
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
    return new ItemModStatus(Code.INVALID_FIELD_DATA, invalidFieldDataMap);
  }
}
