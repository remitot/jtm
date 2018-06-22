package org.jepria.tomcat.manager.web.jdbc;

public class EnsureConnectionResponseStatus {
  public static final int SUCCESS__EXISTED_THE_SAME = 0;
  public static final int SUCCESS__NO_EXIST_CREATED = 1;
  public static final int SUCCESS__EXISTED_CREATED = 2;
  public static final int ERR__MANDATORY_FIELDS_EMPTY = 3;
  public static final int ERR__INTERNAL_ERROR = 4;
  
  public static String getMessage(int status) {
    switch (status) {
    case SUCCESS__EXISTED_THE_SAME: {
      return "SUCCESS: there is an entirely same active connection, no modifications performed";
    }
    case SUCCESS__NO_EXIST_CREATED: {
      return "SUCCESS: no active connection with the same name existed, new connection created";
    }
    case SUCCESS__EXISTED_CREATED: {
      return "SUCCESS: an active connection with the same name existed, it was made inactive, new connection created";
    }
    case ERR__MANDATORY_FIELDS_EMPTY: {
      return "ERROR: some mandatory fields are empty";
    }
    case ERR__INTERNAL_ERROR: {
      return "ERROR: internal error";
    }
    default: return null;
    }
  }
}
