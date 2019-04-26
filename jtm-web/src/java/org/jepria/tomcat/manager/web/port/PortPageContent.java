package org.jepria.tomcat.manager.web.port;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jepria.tomcat.manager.web.port.dto.PortDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.table.Field;

public class PortPageContent implements Iterable<El> {

  private final Iterable<El> elements;
  
  @Override
  public Iterator<El> iterator() {
    return elements.iterator();
  }
  
  public PortPageContent(Context context, List<PortDto> ports) {
    
    final List<El> elements = new ArrayList<>();
    
    // table html
    final PortTable table = new PortTable(context);
    
    final List<PortItem> items = ports.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    table.load(items, null, null);
    
    elements.add(table);

    this.elements = Collections.unmodifiableList(elements);
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
