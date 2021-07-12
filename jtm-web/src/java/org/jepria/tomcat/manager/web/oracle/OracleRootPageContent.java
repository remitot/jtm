package org.jepria.tomcat.manager.web.oracle;

import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Node;
import org.jepria.web.ssr.fields.Table;

import java.util.ArrayList;
import java.util.List;

public class OracleRootPageContent extends ArrayList<El> {
  public OracleRootPageContent(Context context, List<ConnectionDto> connections) {
    add(new El("div", context).setInnerHTML("Welcome to the Oracle thin client! Select a datasource from the list below.", false));

    if (connections == null || connections.isEmpty()) {
      add(new El("div", context).setInnerHTML("No datasources available.")); // TODO
    } else {
      
      Table<Table.Row> datasourceTable = new Table<>(context, null);
      datasourceTable.addStyle("css/oracle/datasource-list.css");
      
      List<Table.Row> rows = new ArrayList<>();
      {
        final int columnCount = 5;
        int rowCount = connections.size() / columnCount + 1;
        
        for (int r = 0; r < rowCount; r++) {
          Table.Row row = new Table.Row();
          for (int c = 0; c < columnCount; c++) {
            Node node;
            {
              int connectionIndex = c * columnCount + r;
              if (connectionIndex < connections.size()) {
                String datasourceName = connections.get(connectionIndex).getName();
                String href = context.getAppContextPath() + context.getServletContextPath() + "/" + datasourceName;
                node = new El("a", context)
                    .setAttribute("href", href)
                    .setInnerHTML(datasourceName, true);
              } else {
                node = null;
              }
            }
            row.add(Table.Cells.withNode(node, null));
          }
          
          rows.add(row);
        }
      }
      
      datasourceTable.load(rows, null, null);
      
      add(datasourceTable);
    }
  }
}