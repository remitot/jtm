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
  
  /**
   * Creates content for details of a newly created binding
   * @param context
   * @param binding
   */
  public JkPageContent(Context context) {
    final List<El> elements = new ArrayList<>();
    
    BindingDetailsTable details = new BindingDetailsTable(context);
    List<BindingDetailsTable.Record> records = createBindingRecords(null, null, true);
    details.load(records, null, null);
    
    elements.add(details);
    
    
    final JkControlButtons controlButtons = new JkControlButtons(context);
    controlButtons.addButtonCancel(context.getContextPath() + "/jk");// TODO such url will erase any path- or request params of the current page
    controlButtons.addButtonSave(context.getContextPath() + "/jk/new-binding");// TODO such url will erase any path- or request params of the current page
    elements.add(controlButtons);
    
    
    this.elements = Collections.unmodifiableList(elements);
  }
  
  /**
   * Creates content for details
   * @param context
   * @param binding
   */
  public JkPageContent(Context context, String mountId, BindingDto binding) {
    final List<El> elements = new ArrayList<>();
    
    // TODO process binding == null here (not found or already removed)
    
    BindingDetailsTable details = new BindingDetailsTable(context);
    List<BindingDetailsTable.Record> records = createBindingRecords(binding.jkMount, binding.worker, false);
    details.load(records, null, null);
    
    elements.add(details);
    
    
    // control buttons
    final JkControlButtons controlButtons = new JkControlButtons(context);
    controlButtons.addButtonCancel(context.getContextPath() + "/jk");// TODO such url will erase any path- or request params of the current page
    controlButtons.addButtonSave(context.getContextPath() + "/jk/" + mountId);// TODO such url will erase any path- or request params of the current page
    controlButtons.addButtonDelete(context.getContextPath() + "/jk/" + mountId + "/delete");// TODO such url will erase any path- or request params of the current page
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
  
  protected List<BindingDetailsTable.Record> createBindingRecords(JkMountDto mount, WorkerDto worker, boolean newBinding) {
    
    List<BindingDetailsTable.Record> records = new ArrayList<>();
    
    // mount fields
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Active", null); // TODO NON-NLS
      item.field().value = item.field().valueOriginal = (mount == null ? null : mount.map.get("active"));
      if (newBinding) {
        item.field().readonly = true;
      }
      item.setId("active");
      records.add(item);
    }
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Application", null); // TODO NON-NLS
      item.field().value = item.field().valueOriginal = (mount == null ? null : mount.map.get("application"));
      item.setId("application");
      records.add(item);
    }
    
    // worker fields
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Worker", "worker1"); // TODO NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("name"));
      item.setId("workerName");
      records.add(item);
    }
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Type", "ajp13"); // TODO NON-NLS NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("type"));
      item.setId("workerType");
      records.add(item);
    }
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Host", "server.com"); // TODO NON-NLS NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("host"));
      item.setId("workerHost");
      records.add(item);
    }
    {
      BindingDetailsTable.Record item = new BindingDetailsTable.Record("Port", "8080"); // TODO NON-NLS NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("port"));
      item.setId("workerPort");
      records.add(item);
    }
    
    return records;
  }
}
