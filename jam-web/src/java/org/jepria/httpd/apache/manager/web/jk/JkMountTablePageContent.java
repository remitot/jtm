package org.jepria.httpd.apache.manager.web.jk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Node;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.Field;

public class JkMountTablePageContent implements Iterable<Node> {

  private final Iterable<Node> elements;
  
  @Override
  public Iterator<Node> iterator() {
    return elements.iterator();
  }
  
  /**
   * Creates content for a table
   * @param context
   * @param jkMounts
   */
  public JkMountTablePageContent(Context context, List<JkMountDto> jkMounts) {
    final List<El> elements = new ArrayList<>();
    
    final JkMountTable table = new JkMountTable(context);
    
    final List<JkMountTable.Record> items = jkMounts.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    table.load(items, null, null);
    
    elements.add(table);
    
    
    // control buttons
    final ControlButtons controlButtons = new ControlButtons(context);
    final String createActionUrl = context.getContextPath() + "/jk/new-binding";
    {
      Text text = context.getText();
      
      final El formCreate = new El("form", context).setAttribute("action", createActionUrl).setAttribute("method", "get")
          .addClass("button-form");
      
      El button = new El("button", context)
          .setAttribute("type", "submit")
          .addClass("control-button")
          .addClass("big-black-button")
          .setInnerHTML(text.getString("org.jepria.web.ssr.ControlButtons.buttonCreate.text"), true);
      
      formCreate.appendChild(button);
      
      controlButtons.appendChild(formCreate);
    }
    elements.add(controlButtons);
    

    this.elements = Collections.unmodifiableList(elements);
  }
  
  protected JkMountTable.Record dtoToItem(JkMountDto dto) {
    JkMountTable.Record item = new JkMountTable.Record();
    for (String name: dto.map.keySet()) {
      Field field = item.get(name);
      if (field != null) {
        field.value = field.valueOriginal = dto.map.get(name);
      }
    }
    
    item.details().value = "jk/" + dto.map.get("id");
    
    return item;
  }
}