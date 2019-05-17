package org.jepria.httpd.apache.manager.web.jk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.Field;

public class JkPageContent implements Iterable<El> {

  private final Iterable<El> elements;
  
  @Override
  public Iterator<El> iterator() {
    return elements.iterator();
  }
  
  public JkPageContent(Text text, List<JkMountDto> jkMounts) {
    
    final List<El> elements = new ArrayList<>();
    
    // table html
    final JkMountTable table = new JkMountTable(text);
    
    final List<JkMountItem> items = jkMounts.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    table.load(items, null, null);
    
    elements.add(table);

    this.elements = Collections.unmodifiableList(elements);
  }
  
  protected JkMountItem dtoToItem(JkMountDto dto) {
    JkMountItem item = new JkMountItem();
    for (String name: dto.keySet()) {
      Field field = item.get(name);
      if (field != null) {
        field.value = field.valueOriginal = dto.get(name);
      }
    }
    
    item.details().value = "jk?id=" + dto.get("id");
    
    return item;
  }
}
