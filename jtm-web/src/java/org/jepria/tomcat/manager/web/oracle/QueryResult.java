package org.jepria.tomcat.manager.web.oracle;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Set;

public class QueryResult {
  private final boolean successful;
  
  private final List<String> columnNames;
  private final List<List<Value>> values;
  /**
   * If paging is used, true if there are any more result records behind the last record of this page, 
   * false if the last record of this page is the last record of the entire result set of the query.   
   */
  private final Boolean hasMoreResults;
  
  private final Throwable exception;

  public static class Value {
  }

  public static class StringValue extends Value {
    public String value;
    
    public StringValue(String value) {
      this.value = value;
    }
  }

  public static class LobValue extends Value {
    public boolean isStubbed;
  }
  
  public static class ClobValue extends LobValue {
    public String downloadId;
    public Reader content;
  }
  
  public static class BlobValue extends LobValue {
    public String downloadId;
    public InputStream content;
  }

  /**
   * Global access to all unsubbed (opened, having {@link LobValue#isStubbed} = false) LOB streams (CLOBs or BLOBs) containing across the {@link #values} table
   * regardless of rows or columns.
   */
  private Set<LobValue> unstubbedLobValues;
  
  /**
   * 
   * @param columnNames
   * @param values
   * @param hasMoreResults
   */
  public QueryResult(List<String> columnNames, List<List<Value>> values, Set<LobValue> unstubbedLobValues, boolean hasMoreResults) {
    this.successful = true;
    this.columnNames = columnNames;
    this.values = values;
    this.unstubbedLobValues = unstubbedLobValues;
    this.hasMoreResults = hasMoreResults;
    this.exception = null;
  }

  public QueryResult(Throwable exception) {
    this.successful = false;
    this.columnNames = null;
    this.values = null;
    this.unstubbedLobValues = null;
    this.hasMoreResults = null;
    this.exception = exception;
  }

  public List<String> getColumnNames() {
    return columnNames;
  }

  public List<List<Value>> getValues() {
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

  public Set<LobValue> getUnstubbedLobValues() {
    return unstubbedLobValues;
  }
}


