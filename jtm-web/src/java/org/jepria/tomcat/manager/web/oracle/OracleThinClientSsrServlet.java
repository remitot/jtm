package org.jepria.tomcat.manager.web.oracle;

import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.JtmPageHeader;
import org.jepria.tomcat.manager.web.jdbc.JdbcApi;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.web.ssr.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.*;
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

  public static final String SESSION_ATTR_KEY__CLOB_PREFIX = "org.jepria.tomcat.manager.web.oracle.QueryResult.CLOB_";
  public static final String SESSION_ATTR_KEY__BLOB_PREFIX = "org.jepria.tomcat.manager.web.oracle.QueryResult.BLOB_";
  
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
          .stream().filter(connection -> "true".equals(connection.getActive())).collect(Collectors.toList());
      return new OracleRootPageContent(context, connections);
    }

    m = Pattern.compile("/(jdbc/[^/]+)/packages/?").matcher(path);
    if (m.matches()) {
      final String dataSourceName = m.group(1);
      List<String> packageNames;
      try {
        packageNames = getPackageNames(dataSourceName);
      } catch (NamingException | SQLException e) {
        throw new RuntimeException(e);
      }
      return new PackageListPageContent(context, packageNames);
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

    m = Pattern.compile("/(jdbc/[^/]+)/?").matcher(path);
    if (m.matches()) {
      // query
      final String datasourceName = m.group(1);
      return getQueryPageContent(context, datasourceName, request);
    }
    
    return null; // TODO
  }
  
  protected static QueryPageContent getQueryPageContent(Context context, String datasourceName, HttpServletRequest request) {

    final String formAction = context.getAppContextPath() + context.getServletContextPath() + "/" + datasourceName;

    // query to execute immediately to show results for
    final String query = request.getParameter("query");
    if (query != null && !"".equals(query)) {
      // set the query as an input into the field and execute

      String queryRefined;
      {
        // trim trailing ' ; '
        Matcher m1 = Pattern.compile("(.+)\\s*;\\s*").matcher(query);
        if (m1.matches()) {
          queryRefined = m1.group(1);
        } else {
          queryRefined = query;
        }
        
        // refine whitespaces
        queryRefined = queryRefined.replaceAll("\\s", " ");
        queryRefined = queryRefined.replaceAll("\\u00A0", " "); // non-break space
      }


      QueryResult queryResult = executeQuery(datasourceName, queryRefined);

      { // clear existing lob values stored in the session
        Set<String> sessionAttrNamesToRemove = new HashSet<>();
        Enumeration<String> sessionAttrNames = request.getSession().getAttributeNames();
        while (sessionAttrNames.hasMoreElements()) {
          String sessionAttrName = sessionAttrNames.nextElement();
          if (sessionAttrName.startsWith(SESSION_ATTR_KEY__CLOB_PREFIX)
              || sessionAttrName.startsWith(SESSION_ATTR_KEY__BLOB_PREFIX)) {
            sessionAttrNamesToRemove.add(sessionAttrName);
          }
        }
        for (String sessionAttrNameToRemove: sessionAttrNamesToRemove) {
          request.getSession().removeAttribute(sessionAttrNameToRemove);
        }
      }


      { // store new unstubbed lob values into the session
        Set<QueryResult.LobValue> unstubbedLobValues = queryResult.getUnstubbedLobValues();
        if (unstubbedLobValues != null) {
          for (QueryResult.LobValue lobValue : unstubbedLobValues) {
            String downloadId = String.valueOf((int) (Math.random() * 1000000));
            if (lobValue instanceof QueryResult.ClobValue) {
              QueryResult.ClobValue clobValue = (QueryResult.ClobValue) lobValue;
              clobValue.downloadId = downloadId;
              String sessionAttrKey = SESSION_ATTR_KEY__CLOB_PREFIX + clobValue.downloadId;
              request.getSession().setAttribute(sessionAttrKey, (Reader) clobValue.content);
            } else if (lobValue instanceof QueryResult.BlobValue) {
              QueryResult.BlobValue blobValue = (QueryResult.BlobValue) lobValue;
              blobValue.downloadId = downloadId;
              String sessionAttrKey = SESSION_ATTR_KEY__BLOB_PREFIX + blobValue.downloadId;
              request.getSession().setAttribute(sessionAttrKey, (InputStream) blobValue.content);
            } else {
              throw new IllegalArgumentException(lobValue.getClass().getCanonicalName());
            }
          }
        }
      }

      return new QueryPageContent(context, formAction, datasourceName, query, queryResult);

    } else {

      final String queryInput = request.getParameter("query-input");
      return new QueryPageContent(context, formAction, datasourceName, queryInput, null);

    }
  }
  
  // TODO remove this method (refactor)
  protected static List<String> getPackageNames(String dataSourceName) throws NamingException, SQLException {
    
    final List<String> ret = new ArrayList<>();
    
    InitialContext ic = new InitialContext();
    DataSource dataSource = (DataSource) ic.lookup("java:/comp/env/" + dataSourceName);
    try (Connection con = dataSource.getConnection()) {
      con.setAutoCommit(false);
      Statement stm = con.createStatement();

      {
        String queryText =
            "SELECT OBJECT_NAME, OWNER FROM ALL_OBJECTS WHERE OBJECT_TYPE = 'PACKAGE'" +
                " AND OBJECT_NAME LIKE 'PKG_%'" +
                " ORDER BY OBJECT_NAME ASC";

        stm.execute(queryText);

        ResultSet rs = stm.getResultSet();
        if (rs != null) {
          while (rs.next()) {
            ret.add(rs.getString("OBJECT_NAME"));
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
        if (rs != null) {
          while (rs.next()) {
            ret.add(rs.getString("OBJECT_NAME"));
          }
        }
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

  protected static QueryResult executeQuery(String connectionName, String queryText) {
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
  protected static QueryResult executeQuery(String connectionName, String queryText, int page, int pageSize) {

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

        boolean loop = true;
        
        // skip pages before
        for (int p = 0; p < page && loop; p++) {
          for (int i = 0; i < pageSize && loop; i++) {
            if (!rs.next()) {
              loop = false;
            }
          }
        }

        final List<List<QueryResult.Value>> values = new ArrayList<>();
        
        boolean hasMoreResults = false;

        boolean firstRow = true;
        
        // any Clob or Blob values (regardless rows or columns) which are unstubbed and may further have to be stubbed
        Set<QueryResult.LobValue> unstubbedLobValues = new HashSet<>();
        // whether or not all unstubbedLobValues have to be stubbed at the end of the result fetching loop
        boolean stubUnstubbedLobValues = false;
        
        while (loop) {
          if (hasMoreResults = rs.next()) {
            if (values.size() < pageSize) {
              
              List<QueryResult.Value> row = new ArrayList<>();
              for (int i = 1; i <= n; i++) {
                
                if (rsmeta.getColumnType(i) == 2005) {
                  // CLOB
                  Reader reader = rs.getCharacterStream(i);
                  if (reader == null) {
                    QueryResult.StringValue nullValue = new QueryResult.StringValue(null);
                    row.add(nullValue);
                  } else {
                    QueryResult.ClobValue clobValue = new QueryResult.ClobValue();
                    if (firstRow) {
                      clobValue.content = reader;
                      clobValue.isStubbed = false;
                      unstubbedLobValues.add(clobValue);
                    } else {
                      stubUnstubbedLobValues = true;
                      clobValue.isStubbed = true;
                    }
                    row.add(clobValue);
                  }
                  
                } else if (rsmeta.getColumnType(i) == 2004) {
                  // BLOB
                  InputStream in = rs.getBinaryStream(i);
                  if (in == null) {
                    QueryResult.StringValue nullValue = new QueryResult.StringValue(null);
                    row.add(nullValue);
                  } else {
                    QueryResult.BlobValue blobValue = new QueryResult.BlobValue();
                    if (firstRow) {
                      blobValue.content = in;
                      blobValue.isStubbed = false;
                      unstubbedLobValues.add(blobValue);
                    } else {
                      stubUnstubbedLobValues = true;
                      blobValue.isStubbed = true;
                    }
                    row.add(blobValue);
                  }
                  
                } else {
                  QueryResult.StringValue stringValue = new QueryResult.StringValue(rs.getString(i));
                  row.add(stringValue);
                }
              }
              values.add(row);
            } else {
              loop = false;
            }
            
            firstRow = false;
          } else {
            loop = false;
          }
        }
        
        if (stubUnstubbedLobValues) {
          for (QueryResult.Value lobValue: unstubbedLobValues) {
            if (lobValue instanceof QueryResult.ClobValue) {
              QueryResult.ClobValue clobValue = (QueryResult.ClobValue) lobValue;
              if (!clobValue.isStubbed) {
                clobValue.isStubbed = true;
                clobValue.content = null;
              }
            } else if (lobValue instanceof QueryResult.BlobValue) {
              QueryResult.BlobValue blobValue = (QueryResult.BlobValue) lobValue;
              if (!blobValue.isStubbed) {
                blobValue.isStubbed = true;
                blobValue.content = null;
              }
            }
          }
          unstubbedLobValues.clear();
        }
        
        return new QueryResult(columnNames, values, unstubbedLobValues, hasMoreResults);
      }

    } catch (Throwable e) {
      e.printStackTrace();

      return new QueryResult(e);
    }
  }
}

