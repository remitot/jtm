package org.jepria.tomcat.manager.web.jdbc;

import java.util.List;

public class EnsureConnectionResponseStatus {
  
  
  public static final int SUCCESS__EXISTED_THE_SAME = 0;
  public static final int SUCCESS__NO_EXIST_CREATED = 1;
  public static final int SUCCESS__EXISTED_CREATED = 2;
  public static final int ERR__MANDATORY_FIELDS_EMPTY = 3;
  public static final int ERR__INTERNAL_ERROR = 4;
  
  
  public final int code;
  public final String message;
  
  
  private EnsureConnectionResponseStatus(int code, String message) {
    this.code = code;
    this.message = message;
  }
  
  
  public static EnsureConnectionResponseStatus successExistedTheSame() {
    return new EnsureConnectionResponseStatus(SUCCESS__EXISTED_THE_SAME, "SUCCESS: there is an entirely same active connection, no modifications performed");
  }
  
  public static EnsureConnectionResponseStatus successNoExistCreated() {
    return new EnsureConnectionResponseStatus(SUCCESS__NO_EXIST_CREATED, "SUCCESS: no active connection with the same name existed, new connection created");
  }
  
  public static EnsureConnectionResponseStatus successExistedCreated() {
    return new EnsureConnectionResponseStatus(SUCCESS__EXISTED_CREATED, "SUCCESS: an active connection with the same name existed, it was made inactive, new connection created");
  }
  
  public static EnsureConnectionResponseStatus errMandatoryFieldsEmpty(List<String> fields) {
    String fieldsStr = fields == null || fields.isEmpty() ? null : fields.toString(); 
    return new EnsureConnectionResponseStatus(ERR__MANDATORY_FIELDS_EMPTY, "ERROR: mandatory fields are empty" + 
        (fieldsStr == null ? "" : (": " + fieldsStr)));
  }
  
  public static EnsureConnectionResponseStatus errInternalError() {
    return new EnsureConnectionResponseStatus(ERR__INTERNAL_ERROR, "ERROR: internal error, ask server admin for more info");
  }
}
