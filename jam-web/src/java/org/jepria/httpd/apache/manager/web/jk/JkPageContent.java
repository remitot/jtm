package org.jepria.httpd.apache.manager.web.jk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jepria.httpd.apache.manager.web.jk.dto.BindingDto;
import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.httpd.apache.manager.web.jk.dto.WorkerDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.ControlButtons;
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
   * @param context
   * @param jkMounts
   */
  public JkPageContent(Context context, List<JkMountDto> jkMounts) {
    final List<El> elements = new ArrayList<>();
    
    final JkMountTable table = new JkMountTable(context);
    
    final List<JkMountTable.Record> items = jkMounts.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    table.load(items, null, null);
    
    elements.add(table);
    
    
    // control buttons
    final ControlButtons controlButtons = new ControlButtons(context);
    final String createActionUrl = context.getContextPath() + "/jk/new-binding";// TODO stopped here
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
  
  /**
   * Creates content for details of a newly created binding
   * @param context
   * @param binding
   */
  public JkPageContent(Context context) {
    final List<El> elements = new ArrayList<>();
    
    BindingDetailsTable details = new BindingDetailsTable(context, true);
    List<BindingDetailsTable.Record> records = dtoToDetailItems(null, null);
    details.load(records, null, null);
    
    elements.add(details);

    
    this.elements = Collections.unmodifiableList(elements);
  }
  
  /**
   * Creates content for details
   * @param context
   * @param binding
   */
  public JkPageContent(Context context, BindingDto binding) {
    final List<El> elements = new ArrayList<>();
    
    BindingDetailsTable details = new BindingDetailsTable(context, false);
    List<BindingDetailsTable.Record> records = dtoToDetailItems(binding.jkMount, binding.worker);
    details.load(records, null, null);
    
    elements.add(details);

    
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
  
  protected List<BindingDetailsTable.Record> dtoToDetailItems(JkMountDto mount, WorkerDto worker) {
    
    List<BindingDetailsTable.Record> items = new ArrayList<>();
    
    // mount fields
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Active", null); // TODO NON-NLS
      item.field().value = item.field().valueOriginal = (mount == null ? null : mount.map.get("active"));
      item.setId("active");
      items.add(item);
    }
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Application", null); // TODO NON-NLS
      item.field().value = item.field().valueOriginal = (mount == null ? null : mount.map.get("application"));
      item.setId("application");
      items.add(item);
    }
    
    // worker fields
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Worker", "worker1"); // TODO NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("name"));
      item.setId("workerName");
      items.add(item);
    }
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Type", "ajp13"); // TODO NON-NLS NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("type"));
      item.setId("workerType");
      items.add(item);
    }
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Host", "server.com"); // TODO NON-NLS NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("host"));
      item.setId("workerHost");
      items.add(item);
    }
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Port", "8080"); // TODO NON-NLS NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("port"));
      item.setId("workerPort");
      items.add(item);
    }
    
    return items;
  }
}
