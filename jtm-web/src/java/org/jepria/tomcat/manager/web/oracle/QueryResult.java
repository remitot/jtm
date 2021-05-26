package org.jepria.tomcat.manager.web.oracle;

import java.util.List;

public class QueryResult {
  private final boolean successful;
  
  private final List<String> columnNames;
  private final List<List<String>> values;
  /**
   * If paging is used, true if there are any more result records behind the last record of this page, 
   * false if the last record of this page is the last record of the entire result set of the query.   
   */
  private final Boolean hasMoreResults;
  
  private final Throwable exception;

  /**
   * 
   * @param columnNames
   * @param values
   * @param hasMoreResults
   */
  public QueryResult(List<String> columnNames, List<List<String>> values, boolean hasMoreResults) {
    this.successful = true;
    this.columnNames = columnNames;
    this.values = values;
    this.hasMoreResults = hasMoreResults;
    this.exception = null;
  }

  public QueryResult(Throwable exception) {
    this.successful = false;
    this.columnNames = null;
    this.values = null;
    this.hasMoreResults = null;
    this.exception = exception;
  }

  public List<String> getColumnNames() {
    return columnNames;
  }

  public List<List<String>> getValues() {
    return values;
  }

  public Boolean hasMoreResults() {
    return hasMoreResults;
  }
  
  public Throwable getException() {
    return exception;
  }

  public boolean isSuccessful() {
    return successful;
  }
}


