package org.jepria.httpd.apache.manager.web.jk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jepria.httpd.apache.manager.web.jk.dto.BindingDto;
import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Node;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.Field;

public class JkPageContent implements Iterable<Node> {

  private final Iterable<Node> elements;
  
  @Override
  public Iterator<Node> iterator() {
    return elements.iterator();
  }
  
  /**
   * Creates content for a table
   * @param text
   * @param jkMounts
   */
  public JkPageContent(Text text, List<JkMountDto> jkMounts) {
    final List<El> elements = new ArrayList<>();
    
    final JkMountTable table = new JkMountTable(text);
    
    final List<JkMountItem> items = jkMounts.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    table.load(items, null, null);
    
    elements.add(table);

    this.elements = Collections.unmodifiableList(elements);
  }
  
  /**
   * Creates content for details
   * @param text
   * @param binding
   */
  public JkPageContent(Text text, BindingDto binding) {
    final List<El> elements = new ArrayList<>();
    
    JkDetails details = new JkDetails();
    details.load(binding.jkMount, binding.worker);
    
    elements.add(details);

    this.elements = Collections.unmodifiableList(elements);
  }
  
  protected JkMountItem dtoToItem(JkMountDto dto) {
    JkMountItem item = new JkMountItem();
    for (String name: dto.map.keySet()) {
      Field field = item.get(name);
      if (field != null) {
        field.value = field.valueOriginal = dto.map.get(name);
      }
    }
    
    item.details().value = "jk?mount-id=" + dto.map.get("id");
    
    return item;
  }
}
