package org.jepria.tomcat.manager.web.port;

import java.util.List;
import java.util.stream.Collectors;

import org.jepria.tomcat.manager.web.HtmlPage;
import org.jepria.tomcat.manager.web.port.dto.PortDto;
import org.jepria.web.ssr.table.Field;

public class PortHtmlPage extends HtmlPage {

  public static final String PAGE_TITLE = "Tomcat manager: порты"; // NON-NLS
  
  public final PortTable table;
  
  public PortHtmlPage(List<PortDto> ports) {
    
    setTitle(PAGE_TITLE);
    
    // table html
    table = new PortTable();
    
    final List<PortItem> items = ports.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    table.load(items, null, null);
    
    getBodyChilds().add(table);
    
    // add onload scripts
    body.setAttribute("onload", "jtm_onload();table_onload();");
  }
  
  protected PortItem dtoToItem(PortDto dto) {
    PortItem item = new PortItem();
    for (String name: dto.keySet()) {
      Field field = item.get(name);
      if (field != null) {
        field.value = field.valueOriginal = dto.get(name);
      }
    }
    return item;
  }
  
}
