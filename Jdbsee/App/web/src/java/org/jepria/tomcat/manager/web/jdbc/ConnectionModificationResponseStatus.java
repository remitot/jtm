package org.jepria.tomcat.manager.web.jdbc;

public class ConnectionModificationResponseStatus {
  public static final int SUCCESS = 0;
  public static final int ERR__CONNECTION_NOT_FOUND_BY_LOCATION = 1;
  public static final int ERR__LOCATION_IS_EMPTY = 2;
  public static final int ERR__MANDATORY_FIELDS_EMPTY = 3;
  public static final int ERR__ILLEGAL_ACTION = 4;
  public static final int ERR__INTERNAL_ERROR = 5;
  
  public static String getMessage(int status) {
    switch (status) {
    case SUCCESS: {
      return "SUCCESS";
    }
    case ERR__CONNECTION_NOT_FOUND_BY_LOCATION: {
      return "ERROR: no connection found by \"location\"";
    }
    case ERR__LOCATION_IS_EMPTY: {
      return "ERROR: \"location\" is empty";
    }
    case ERR__MANDATORY_FIELDS_EMPTY: {
      return "ERROR: some mandatory fields in \"data\" are empty";
    }
    case ERR__ILLEGAL_ACTION: {
      return "ERROR: illegal \"action\" value";
    }
    case ERR__INTERNAL_ERROR: {
      return "ERROR: internal error";
    }
    default: return null;
    }
  }
}
