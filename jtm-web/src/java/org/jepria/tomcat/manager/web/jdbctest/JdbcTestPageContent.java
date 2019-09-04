  package org.jepria.tomcat.manager.web.jdbctest;

  import org.jepria.web.ssr.Context;
  import org.jepria.web.ssr.El;
  import org.jepria.web.ssr.Text;

  import java.util.ArrayList;

public class JdbcTestPageContent extends ArrayList<El> {

  public static class QueryResult {
    public boolean success;
    public String message;
  }

  /**
   *
   * @param context
   * @param queryAction {@code form action} attribute value to post the query for execution to
   * @param connectionName must not be {@code null}
   * @param queryText
   * @param queryResult
   */
  public JdbcTestPageContent(Context context, String queryAction, String connectionName, String queryText, QueryResult queryResult) {
    Text text = context.getText();

    El label = new El("div", context)
        .addClass("block")
        .setInnerHTML(String.format(text.getString("org.jepria.tomcat.manager.web.jdbctest.queryLabel"), "<b>" + connectionName + "</b>"));
    add(label);

    El form = new El("form", context)
        .addClass("block")
        .setAttribute("action", queryAction)
        .setAttribute("method", "post");

    {
      El queryInput = new El("input", context)
          .addClass("query")
          .setAttribute("type", "text")
          .setAttribute("name", "query");
      if (queryText != null && !"".equals(queryText)) {
        queryInput.setAttribute("value", queryText);
      }

      form.appendChild(queryInput);

      El submit = new El("input", context)
          .setAttribute("type", "submit")
          .setAttribute("value", text.getString("org.jepria.tomcat.manager.web.jdbctest.query.buttonSubmit.text"));
      form.appendChild(submit);
    }

    add(form);

    if (queryResult != null) {

      {
        El status = new El("div", context)
            .addClass("block")
            .addClass("block-status");
        if (queryResult.success) {
          status.addClass("block-status_success")
              .setInnerHTML(text.getString("org.jepria.tomcat.manager.web.jdbctest.queryResult_success"));

        } else {
          status.addClass("block-status_failure")
              .setInnerHTML(text.getString("org.jepria.tomcat.manager.web.jdbctest.queryResult_failure"));
        }
        add(status);
      }

      {
        String queryResultMessage = queryResult.message;
        if (queryResultMessage != null && !"".equals(queryResultMessage)) {
          El queryResultMsg = new El("pre", context)
              .addClass("block")
              .setInnerHTML(queryResultMessage);
          add(queryResultMsg);
        }
      }

    }

    if (size() > 0) {
      get(0).addStyle("css/jdbc-test/jdbc-test.css");
    }
  }
}
