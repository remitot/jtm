package org.jepria.jdbsee.web;

public class ConnectionModificationResponseStatus {
  public static final int SUCCESS = 0;
  public static final int ERR__CONNECTION_NOT_FOUND_BY_LOCATION = 1;
  public static final int ERR__LOCATION_IS_EMPTY = 2;
  public static final int ERR__MANDATORY_FIELDS_EMPTY = 3;
  public static final int ERR__ILLEGAL_ACTION = 4;
  public static final int ERR__INTERNAL_ERROR = 5;
}
