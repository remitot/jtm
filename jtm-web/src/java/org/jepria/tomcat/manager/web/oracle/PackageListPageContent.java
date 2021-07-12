package org.jepria.tomcat.manager.web.oracle;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;

import java.util.ArrayList;
import java.util.List;

public class PackageListPageContent extends ArrayList<El> {
  public PackageListPageContent(Context context, List<String> packageNames) {
    
    El div = new El("div", context);
    
    String innerHtml = "";
    for (String packageName: packageNames) {
      innerHtml += "<div>" + packageName + "</div><br/>\n";
    }
    
    div.setInnerHTML(innerHtml);
    
    add(div);
  }
}
