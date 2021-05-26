package org.jepria.tomcat.manager.web.oracle;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.JtmPageHeader;
import org.jepria.tomcat.manager.web.jdbc.JdbcApi;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.web.HttpDataEncoding;
import org.jepria.web.auth.RedirectBuilder;
import org.jepria.web.ssr.*;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OracleThinClientSsrServlet extends SsrServletBase {
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    Context context = Context.get(request, "text/org_jepria_tomcat_manager_web_Text");
    Text text = context.getText();

    final Environment env = EnvironmentFactory.get(request);

    final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(context);
    pageBuilder.setTitle(text.getString("org.jepria.tomcat.manager.web.oracle.title"));

    String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");

    final PageHeader pageHeader = new JtmPageHeader(context, managerApacheHref, JtmPageHeader.CurrentMenuItem.ORACLE);
    pageBuilder.setHeader(pageHeader);
    
    
    if (checkAuth(request)) {
      pageHeader.setButtonLogout(request);
      pageHeader.setSources();
      
      List<El> content = getPageContent(request, context, env);

      pageBuilder.setContent(content);
  
    } else {
      requireAuth(request, pageBuilder);
    }

    pageBuilder.build().respond(response);
  }

  protected List<El> getPageContent(HttpServletRequest request, Context context, Environment env) {
    String path = request.getPathInfo();
    
    Matcher m;

    if (path == null) {
      path = "";
    }
    
    m = Pattern.compile("/?").matcher(path);
    if (m.matches()) {
      // oracle root
      final List<ConnectionDto> connections = new JdbcApi().list(env)
          .stream().filter(connection -> "true".equals(connection.get("active"))).collect(Collectors.toList());
      return new OracleRootPageContent(context, connections);
    }

    m = Pattern.compile("/(jdbc/[^/]+)/?").matcher(path);
    if (m.matches()) {
      // datasource info
      final String datasourceName = m.group(1);
      // TODO this is a stub El
      return new DataSourceInfoPageContent(context, datasourceName);
    }

    m = Pattern.compile("/(jdbc/[^/]+)/packages/?").matcher(path);
    if (m.matches()) {
      final String dataSourceName = m.group(1);
      // TODO this is a stub El
      return Arrays.asList(new El("div", context).setInnerHTML(createPackageListInnerHtml(dataSourceName)));
    }

    m = Pattern.compile("/(jdbc/[^/]+)/packages/([^/]+)/?").matcher(path);
    if (m.matches()) {
      final String dataSourceName = m.group(1);
      final String packageName = m.group(2);
      // TODO this is a stub El
      return Arrays.asList(new El("div", context).setInnerHTML("This is package " + dataSourceName + "." + packageName + "!"));
    }

    m = Pattern.compile("/(jdbc/[^/]+)/packages/([^/]+)/procedures/?").matcher(path);
    if (m.matches()) {
      final String dataSourceName = m.group(1);
      final String packageName = m.group(2);
      return Arrays.asList(new El("div", context).setInnerHTML(createProcedureListInnerHtml(dataSourceName, packageName)));
    }

    m = Pattern.compile("/(jdbc/[^/]+)/query/?").matcher(path);
    if (m.matches()) {
      // query
      final String datasourceName = m.group(1);

      final String queryText;
      final QueryResult queryResult;
          
      if (request.getSession().getAttribute(QUERY_EXEC_STATUS_SESSION_ATTR_KEY) != null) {
        queryText = (String) request.getSession().getAttribute(QUERY_SESSION_ATTR_KEY);
        queryResult = (QueryResult) request.getSession().getAttribute(QUERY_RESULT_SESSION_ATTR_KEY);
      } else {
        // if the query has not been executed, show query from parameter
        queryText = request.getParameter("q");
        queryResult = null;
      }

      request.getSession().removeAttribute(QUERY_SESSION_ATTR_KEY);
      request.getSession().removeAttribute(QUERY_RESULT_SESSION_ATTR_KEY);
      request.getSession().removeAttribute(QUERY_EXEC_STATUS_SESSION_ATTR_KEY);
      
      String formAction = context.getAppContextPath() + context.getServletContextPath() + "/" + datasourceName + "/query";
      return new QueryPageContent(context, formAction, datasourceName, queryText, queryResult);
    }
    
    return null;
  }
  
  protected static class OracleRootPageContent extends ArrayList<El> {
    public OracleRootPageContent(Context context, List<ConnectionDto> connections) {
      add(new El("div", context).setInnerHTML("Welcome to the Oracle thin client! Select a datasource from the list below.", false));

      if (connections == null || connections.isEmpty()) {
        add(new El("div", context).setInnerHTML("No datasources available."));
      } else {
        for (ConnectionDto conn: connections) {
          String datasourceName = conn.get("name");
          add(new El("div", context).setInnerHTML("<a href=\"" + context.getAppContextPath() + context.getServletContextPath() + "/" + datasourceName + "\">" + datasourceName + "</a>", false));
        }
      }
    }
  }
  
  protected static class DataSourceInfoPageContent extends ArrayList<El> {
    public DataSourceInfoPageContent(Context context, String datasourceName) {
      add(new El("div", context).setInnerHTML("This is datasource " + datasourceName  + "." +
          " Execute a <a href=\"" + context.getAppContextPath() + context.getServletContextPath() + "/" + datasourceName + "/query" + "\">query</a>" +
          " or select a <a href=\"" + context.getAppContextPath() + context.getServletContextPath() + "/" + datasourceName + "/packages" + "\">package</a> to work with.", false));
    }
  }
  
  // TODO remove this method (refactor)
  protected String createPackageListInnerHtml(String dataSourceName) {
    
    try {

      InitialContext ic = new InitialContext();
      DataSource dataSource = (DataSource) ic.lookup("java:/comp/env/" + dataSourceName);
      try (Connection con = dataSource.getConnection()) {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        String ret = "";
        
        {
          String queryText =
              "SELECT OBJECT_NAME, OWNER FROM ALL_OBJECTS WHERE OBJECT_TYPE = 'PACKAGE'" +
                  " AND OBJECT_NAME LIKE 'PKG_%'" +
                  " ORDER BY OBJECT_NAME ASC";
  
          stm.execute(queryText);
  
          ResultSet rs = stm.getResultSet();
          if (rs == null) {
            ret = "<div>rs=null</div>";
          } else {
            while (rs.next()) {
              ret += "<div>" + rs.getString("OBJECT_NAME") + "</div>"
                  + "\n";
            }
          }
        }

        {
          String queryText =
              "SELECT OBJECT_NAME, OWNER FROM ALL_OBJECTS WHERE OBJECT_TYPE = 'PACKAGE'" +
                  " AND OBJECT_NAME NOT LIKE 'PKG_%'" +
                  " ORDER BY OBJECT_NAME ASC";

          stm.execute(queryText);

          ResultSet rs = stm.getResultSet();
          if (rs == null) {
            ret = "<div>rs=null</div>";
          } else {
            while (rs.next()) {
              ret += "<div>" + rs.getString("OBJECT_NAME") + "</div>"
                  + "\n";
            }
          }
        }
        
        return ret;
      }

    } catch (Throwable e) {
      e.printStackTrace();

      final String ret;
      {
        // print exception to string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        ret = "<div>" + baos.toString() + "</div>";
      }

      return ret;
    }
  }

  // TODO remove this method (refactor)
  protected String createProcedureListInnerHtml(String dataSourceName, String packageName) {
    
    try {

      InitialContext ic = new InitialContext();
      DataSource dataSource = (DataSource) ic.lookup("java:/comp/env/" + dataSourceName);
      try (Connection con = dataSource.getConnection()) {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        String ret = "";

        {
          String queryText =
              "SELECT PROCEDURE_NAME FROM ALL_PROCEDURES WHERE upper(OBJECT_NAME) = upper('" + packageName + "')" +
                  " ORDER BY PROCEDURE_NAME ASC";

          stm.execute(queryText);

          ResultSet rs = stm.getResultSet();
          if (rs == null) {
            ret = "<div>rs=null</div>";
          } else {
            while (rs.next()) {
              ret += "<div>" + rs.getString("PROCEDURE_NAME") + "</div>"
                  + "\n";
            }
          }
        }

        return ret;
      }

    } catch (Throwable e) {
      e.printStackTrace();

      final String ret;
      {
        // print exception to string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        ret = "<div>" + baos.toString() + "</div>";
      }

      return ret;
    }
  }

  /**
   * Query text
   */
  private static final String QUERY_SESSION_ATTR_KEY = "org.jepria.tomcat.manager.web.oracle.SessionAttributes.query";
  /**
   * Query result
   */
  private static final String QUERY_RESULT_SESSION_ATTR_KEY = "org.jepria.tomcat.manager.web.oracle.SessionAttributes.queryResult";
  /**
   * Whether or not the query has been executed at all
   */
  private static final String QUERY_EXEC_STATUS_SESSION_ATTR_KEY = "org.jepria.tomcat.manager.web.oracle.SessionAttributes.queryExecStatus";
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    if (checkAuth(request)) {

      // extract connection name from path
      final String connectionName;
      {
        final String path = request.getPathInfo();
        Matcher m = Pattern.compile("/(.+)/query/?").matcher(path);
        if (m.matches()) {
          connectionName = m.group(1);
        } else {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Expected request path: '/jdbc/DataSourceName/query', actual: '" + path + "'");
          response.flushBuffer();
          return;
        }
      }
      
      final String query = HttpDataEncoding.getParameterUtf8(request, "query-text");
      
      request.getSession().setAttribute(QUERY_SESSION_ATTR_KEY, query);

      if (query != null && !"".equals(query)) {
        QueryResult queryResult = executeQuery(connectionName, query);
        request.getSession().setAttribute(QUERY_EXEC_STATUS_SESSION_ATTR_KEY, true);
        request.getSession().setAttribute(QUERY_RESULT_SESSION_ATTR_KEY, queryResult);
      }
    }

    response.sendRedirect(RedirectBuilder.self(request));
    
  }

  protected QueryResult executeQuery(String connectionName, String queryText) {
    return executeQuery(connectionName, queryText, 0, 25);
  }
  /**
   *
   * @param connectionName non null
   * @param queryText non null
   * @param page from 0
   * @param pageSize 
   * @return table
   */
  protected QueryResult executeQuery(String connectionName, String queryText, int page, int pageSize) {

    try {

      InitialContext ic = new InitialContext();
      DataSource dataSource = (DataSource) ic.lookup("java:/comp/env/" + connectionName);
      try (Connection con = dataSource.getConnection()) {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(queryText);
        ResultSetMetaData rsmeta = rs.getMetaData(); 
        
        int n = rsmeta.getColumnCount();
        
        final List<String> columnNames = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
          columnNames.add(rsmeta.getColumnName(i));
        }

        final List<List<String>> values = new ArrayList<>();
        
        boolean loop = true;
        
        // skip pages before
        for (int p = 0; p < page && loop; p++) {
          for (int i = 0; i < pageSize && loop; i++) {
            if (!rs.next()) {
              loop = false;
            }
          }
        }
        
        boolean hasMoreResults = false;
        
        while (loop) {
          if (hasMoreResults = rs.next()) {
            if (values.size() < pageSize) {
              List<String> row = new ArrayList<>();
              for (int i = 1; i <= n; i++) {
                if (rsmeta.getColumnType(i) == 2005) {
                  row.add("<CLOB>");
                  // TODO CLOB
                } else if (rsmeta.getColumnType(i) == 2004) {
                  row.add("<BLOB>");
                  // TODO BLOB
                } else {
                  row.add(rs.getString(i));
                }
              }
              values.add(row);
            } else {
              loop = false;
            }
          } else {
            loop = false;
          }
        }
        
        return new QueryResult(columnNames, values, hasMoreResults);
      }

    } catch (Throwable e) {
      e.printStackTrace();

      return new QueryResult(e);
    }
  }
}

