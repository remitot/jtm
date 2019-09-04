package org.jepria.tomcat.manager.web.jdbctest;

import org.jepria.web.HttpDataEncoding;
import org.jepria.web.auth.RedirectBuilder;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.HtmlEscaper;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.SsrServletBase;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;

public class JdbcTestSsrServlet extends SsrServletBase {

  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }


  /**
   * Query text
   */
  private static final String QUERY_SESSION_ATTR_KEY = "org.jepria.tomcat.manager.web.jdbctest.SessionAttributes.query";
  /**
   * Query result
   */
  private static final String QUERY_RESULT_SESSION_ATTR_KEY = "org.jepria.tomcat.manager.web.jdbctest.SessionAttributes.queryResult";
  /**
   * Whether or not the query has been executed at all
   */
  private static final String QUERY_EXEC_STATUS_SESSION_ATTR_KEY = "org.jepria.tomcat.manager.web.jdbctest.SessionAttributes.queryExecStatus";


  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // extract connection name from path
    final String path = request.getPathInfo();
    if (path == null || "".equals(path) || "/".equals(path)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The connection name must be specified in a request path");
      response.flushBuffer();
      return;
    }
    final String connectionName = path.substring(1); // crop leading '/'


    // host
    final URL url = new URL(request.getRequestURL().toString());
    final String host = url.getHost() + (url.getPort() == 80 ? "" : (":" + url.getPort()));


    Context context = Context.get(request, "text/org_jepria_tomcat_manager_web_Text");

    final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(context);
    pageBuilder.setTitle(context.getText().getString("org.jepria.tomcat.manager.web.jdbctest.title") + " " + HtmlEscaper.escape(connectionName) + " — " + host);

    if (checkAuth(request)) {

      response.setContentType("text/html; charset=UTF-8");

      String queryText;
      JdbcTestPageContent.QueryResult queryResult = null;
      if (request.getSession().getAttribute(QUERY_EXEC_STATUS_SESSION_ATTR_KEY) != null) {

        queryText = (String) request.getSession().getAttribute(QUERY_SESSION_ATTR_KEY);

        String queryResultText = (String) request.getSession().getAttribute(QUERY_RESULT_SESSION_ATTR_KEY);
        queryResult = new JdbcTestPageContent.QueryResult();
        queryResult.success = queryResultText == null;
        queryResult.message = queryResultText;

      } else {
        // if the query has not been executed, show sample query
        queryText = request.getParameter("sample-query");
      }

      request.getSession().removeAttribute(QUERY_SESSION_ATTR_KEY);
      request.getSession().removeAttribute(QUERY_RESULT_SESSION_ATTR_KEY);
      request.getSession().removeAttribute(QUERY_EXEC_STATUS_SESSION_ATTR_KEY);

      JdbcTestPageContent content = new JdbcTestPageContent(context, RedirectBuilder.self(request), connectionName, queryText, queryResult);

      pageBuilder.setContent(content);

    } else {
      requireAuth(request, pageBuilder);
    }

    pageBuilder.build().respond(response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    if (checkAuth(request)) {

      // extract connection name from path
      final String path = request.getPathInfo();
      if (path == null || "".equals(path) || "/".equals(path)) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The connection name must be specified in a request path");
        response.flushBuffer();
        return;
      }
      final String connectionName = path.substring(1); // crop leading '/'

      final String query = HttpDataEncoding.getParameterUtf8(request, "query");

      request.getSession().setAttribute(QUERY_SESSION_ATTR_KEY, query);

      if (query != null && !"".equals(query)) {
        String queryResult = testConnection(connectionName, query);
        request.getSession().setAttribute(QUERY_EXEC_STATUS_SESSION_ATTR_KEY, true);
        request.getSession().setAttribute(QUERY_RESULT_SESSION_ATTR_KEY, queryResult);
      }
    }

    response.sendRedirect(RedirectBuilder.self(request));
  }

  /**
   *
   * @param connectionName non null
   * @param queryText non null
   * @return null if OK, or else error message
   */
  protected String testConnection(String connectionName, String queryText) {

    try {

      InitialContext ic = new InitialContext();
      DataSource dataSource = (DataSource) ic.lookup("java:/comp/env/" + connectionName);
      try (Connection con = dataSource.getConnection()) {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();
        stm.execute(queryText);

        return null;
      }

    } catch (Throwable e) {
      e.printStackTrace();

      final String ret;
      {
        // print exception to string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        ret = baos.toString();
      }

      return ret;
    }
  }
}
