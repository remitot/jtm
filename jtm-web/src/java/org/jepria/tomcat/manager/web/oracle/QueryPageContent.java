package org.jepria.tomcat.manager.web.oracle;

import org.jepria.web.ssr.*;
import org.jepria.web.ssr.fields.Table;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QueryPageContent extends ArrayList<El>  {
  
  /**
   *
   * @param context
   * @param formAction {@code form action} attribute value to post the query for execution to
   * @param connectionName must not be {@code null}
   * @param queryText
   * @param queryResult
   */
  public QueryPageContent(Context context, String formAction, String connectionName, String queryText, QueryResult queryResult) {
    Text text = context.getText();

    El label = new El("div", context)
        .addClass("block")
        .setInnerHTML(String.format(text.getString("org.jepria.tomcat.manager.web.oracle.queryLabel"), "<b>" + connectionName + "</b>"));
    add(label);

    El form = new El("form", context)
        .addClass("block")
        .setAttribute("action", formAction)
        .setAttribute("method", "post");

    {
      El queryInput = new El("textarea", context)
          .addClass("query")
          .setAttribute("type", "text")
          .setAttribute("name", "query-text");
      if (queryText != null && !"".equals(queryText)) {
        queryInput.setInnerHTML(queryText);
      }

      form.appendChild(queryInput);

      El submit = new El("input", context)
          .addClass("submit")
          .setAttribute("type", "submit")
          .setAttribute("value", text.getString("org.jepria.tomcat.manager.web.oracle.query.buttonSubmit.text"));
      form.appendChild(submit);
    }

    add(form);

    if (queryResult != null) {
      {
        El status = new El("div", context)
            .addClass("block")
            .addClass("block-status");
        if (queryResult.isSuccessful()) {
          status.addClass("block-status_success");

          if (Boolean.TRUE.equals(queryResult.hasMoreResults())) {
            status.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.oracle.queryResult_success_hasMore"));
          } else if (Boolean.FALSE.equals(queryResult.hasMoreResults())) {
            status.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.oracle.queryResult_success_noMore"));
          } else {
            status.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.oracle.queryResult_success"));
          }

        } else {
          status.addClass("block-status_failure")
              .setInnerHTML(text.getString("org.jepria.tomcat.manager.web.oracle.queryResult_failure"));
        }
        add(status);
      }

      { // query results
        if (!queryResult.isSuccessful()) {
          Throwable e = queryResult.getException();

          if (e == null) {
            El unknownExceptionEl = new El("div", context)
                .addClass("block")
                .setInnerHTML("<i>Unknown exception</i>", false);
            add(unknownExceptionEl);

          } else {
            // print exception to string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            queryResult.getException().printStackTrace(new PrintStream(baos));
            String s = baos.toString();

            El exceptionStackTraceEl = new El("pre", context)
                .addClass("block")
                .setInnerHTML(s);
            add(exceptionStackTraceEl);
          }

        } else {
          // table html

          List<String> columnHeaders = queryResult.getColumnNames();
          List<List<String>> values = queryResult.getValues();
          { // test headers and each row of values to be of the same length 
            int len = columnHeaders.size();
            for (int i = 0; i < values.size(); i++) {
              if (values.get(i).size() != len) {
                throw new IllegalStateException("Headers and each row of values must be of the same length");
              }
            }
          }
          
          // calculate width for each column across header and all data rows
          List<String> columnWidths = calculateColumnWidths(columnHeaders, values);

          final El tableWrapper = new El("div", context).addClass("block");

          { // two separate tables: first for the column headers 
            final QueryResultTable tableHeader = new QueryResultTable(context, columnWidths);
            tableHeader.addClass("table_header");
            final List<Table.Row> rows = Collections.singletonList(dbrowToRow(columnHeaders));
            tableHeader.load(rows, null, null);

            El tableHeaderWrapper = new El("div", context).addClass("tableHeaderWrapper");
            tableHeaderWrapper.appendChild(tableHeader);
            tableWrapper.appendChild(tableHeaderWrapper);
          }

          { // two separate tables: second for the data rows
            if (values != null && values.size() > 0) {
              final QueryResultTable tableData = new QueryResultTable(context, columnWidths);
              tableData.addClass("table_data");
              final List<Table.Row> rows = values.stream()
                  .map(row -> dbrowToRow(row)).collect(Collectors.toList());
              tableData.load(rows, null, null);

              El tableDataWrapper = new El("div", context).addClass("tableDataWrapper");
              tableDataWrapper.setAttribute("onscroll", "onTableDataWrapperScroll();").addScript(new HasScripts.Script("js/oracle/query.js"));
              tableDataWrapper.appendChild(tableData);
              tableWrapper.appendChild(tableDataWrapper);
            }
          }

          add(tableWrapper);
        }
      }
    }

    if (size() > 0) {
      get(0).addStyle("css/oracle/query.css");
    }
  }
  
  protected static class QueryResultTable extends Table<Table.Row> {

    protected final List<String> columnWidthValues;

    /**
     * 
     * @param context
     * @param columnWidthValues css values of the {@code width} attributes in pixels, e.g. '100' or '123.4'
     */
    public QueryResultTable(Context context, List<String> columnWidthValues) {
      super(context, null);
      this.columnWidthValues = columnWidthValues;
    }

    @Override
    protected El createRow(Row row, TabIndex tabIndex) {
      El rowEl = super.createRow(row, tabIndex);
      
      // apply dynamic column widths to the cells
      if (rowEl.childs != null) {
        if (rowEl.childs.size() > 0) {
          Node divNode = rowEl.childs.get(0);
          if (divNode instanceof El) {
            El divEl = (El) divNode;
            if (divEl.childs != null) {
              for (int i = 0; i < divEl.childs.size(); i++) {
                Node cellNode = divEl.childs.get(i);
                if (cellNode instanceof El) {
                  El cellEl = (El) cellNode;
                  
                  if (cellEl.attributes.containsKey("style")) {
                    throw new IllegalStateException("Cannot set own 'style' attribute because the element already has it");
                  }
                  cellEl.setAttribute("style", "width: " + columnWidthValues.get(i) + "px;");
                } else {
                  throw new IllegalStateException();
                }
              }
            } else {
              throw new IllegalStateException();
            }
          } else {
            throw new IllegalStateException("" + divNode);
          }
        } else {
          throw new IllegalStateException();
        }
      } else {
        throw new IllegalStateException();
      }
      
      return rowEl;
    }
  }

  /**
   * for each column calculates approximate column width (in pixels) based on the longest string value 
   * @param headerNames
   * @param values
   * @return
   */
  protected static List<String> calculateColumnWidths(List<String> headerNames, List<List<String>> values) {
    int maxRowLen = headerNames.size();
    for (List<String> value : values) {
      if (value.size() > maxRowLen) {
        maxRowLen = value.size();
      }
    }

    final List<Integer> contentMaxLengths = new ArrayList<>();

    for (int i = 0; i < maxRowLen; i++) {
      int maxChars = 0;
      if (i < headerNames.size()) {
        String s = headerNames.get(i);
        if (s != null) {
          int chars = s.length();
          if (chars > maxChars) {
            maxChars = chars;
          }
        }
      }
      for (List<String> row : values) {
        if (i < row.size()) {
          String s = row.get(i);
          if (s != null) {
            int chars = s.length();
            if (chars > maxChars) {
              maxChars = chars;
            }
          }
        }
      }
      contentMaxLengths.add(maxChars);
    }

    List<String> columnWidths = new ArrayList<>();
    for (int maxLen: contentMaxLengths) {
      double cssFontSize = 13.5; // from .field-text css
      columnWidths.add(Integer.toString((int)(maxLen * cssFontSize * 0.6 + 20)));
    }

    return columnWidths;
  }

  protected static Table.Row dbrowToRow(List<String> dbrow) {
    Table.Row row = new Table.Row();
    for (int i = 0; i < dbrow.size(); i++) {
      Table.Cell cell = Table.Cells.withStaticValue(dbrow.get(i),null);
      row.add(cell);
    }
    return row;
  }
}
