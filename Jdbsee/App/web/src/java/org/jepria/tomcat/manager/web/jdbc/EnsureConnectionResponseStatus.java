package org.jepria.tomcat.manager.web.jdbc;

public class EnsureConnectionResponseStatus {
  public static final int SUCCESS__EXISTED_THE_SAME = 0;
  public static final int SUCCESS__NO_EXIST_CREATED = 1;
  public static final int SUCCESS__EXISTED_CREATED = 2;
  public static final int ERR__MANDATORY_FIELDS_EMPTY = 3;
  public static final int ERR__INTERNAL_ERROR = 4;
}
