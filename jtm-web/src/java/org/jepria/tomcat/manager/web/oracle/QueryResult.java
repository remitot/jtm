package org.jepria.tomcat.manager.web.oracle;

import java.util.List;

public class QueryResult {
  private final boolean successful;
  
  private final List<String> columnNames;
  private final List<List<String>> values;
  private final Integer maxRowCount;
  /**
   * 1: maxRowCount not reached and no more rows; 
   * 2: maxRowCount reached and there are no more rows; 
   * 3: maxRowCount reached but there are more rows; 
   */
  private final Integer maxRowCountReachType;
  
  private final Throwable exception;

  /**
   * 
   * @param columnNames
   * @param values
   * @param maxRowCountReachType 
   */
  public QueryResult(List<String> columnNames, List<List<String>> values, Integer maxRowCount, Integer maxRowCountReachType) {
    this.successful = true;
    this.columnNames = columnNames;
    this.values = values;
    this.maxRowCount = maxRowCount;
    this.maxRowCountReachType = maxRowCountReachType; 
    this.exception = null;
  }

  public QueryResult(Throwable exception) {
    this.successful = false;
    this.columnNames = null;
    this.values = null;
    this.maxRowCount = null;
    this.maxRowCountReachType = null;
    this.exception = exception;
  }

  public List<String> getColumnNames() {
    return columnNames;
  }

  public List<List<String>> getValues() {
    return values;
  }

  public Integer getMaxRowCountReachType() {
    return maxRowCountReachType;
  }

  public Integer getMaxRowCount() {
    return maxRowCount;
  }

  public Throwable getException() {
    return exception;
  }
  
  public boolean isSuccessful() {
    return successful;
  }
}


