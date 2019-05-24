package org.jepria.httpd.apache.manager.web.jk;

import java.util.ArrayList;
import java.util.List;

import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.httpd.apache.manager.web.jk.dto.WorkerDto;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.fields.FieldCheckBox;
import org.jepria.web.ssr.fields.FieldTextInput;
import org.jepria.web.ssr.fields.FieldTextLabel;
import org.jepria.web.ssr.fields.Fields;
import org.jepria.web.ssr.fields.Table;

public class JkDetails extends Table<JkBindingItem> {
  
  public JkDetails() {
    addClass("table-details");
    addStyle("css/jk/jk.css");
  }
  
  public void load(JkMountDto mount, WorkerDto worker) {
    
    List<JkBindingItem> items = new ArrayList<>();
    
    // mount fields
    {
      JkBindingItem item = new JkBindingItem("Active", null); // TODO NON-NLS
      item.field().value = mount == null ? null : mount.map.get("active");
      item.setId("active");
      items.add(item);
    }
      
//      El cellDelete = new El("div").addClass("cell column-delete cell_field");// TODO stopped here: remove column-delete from non-table
//      addFieldDelete(cellDelete, "del", "und");
//      
//          
//      El row = new El("div");
//      row.addClass("row");
//      
//      row.appendChild(new El("div").addClass("cell cell_field-label").appendChild(Fields.wrapCellPad(label)));
//      row.appendChild(new El("div").addClass("cell cell_field").appendChild(Fields.wrapCellPad(field)));
//      row.appendChild(cellDelete);
//      
//      appendChild(row);
    {
      JkBindingItem item = new JkBindingItem("Application", null); // TODO NON-NLS
      item.field().value = mount == null ? null : mount.map.get("application");
      item.setId("application");
      items.add(item);
    }
    
    // worker fields
    {
      JkBindingItem item = new JkBindingItem("Worker", "worker1"); // TODO NON-NLS
      item.field().value = worker == null ? null : worker.map.get("name");
      item.setId("workerName");
      items.add(item);
    }
    {
      JkBindingItem item = new JkBindingItem("Type", "ajp13"); // TODO NON-NLS NON-NLS
      item.field().value = worker == null ? null : worker.map.get("type");
      item.setId("workerType");
      items.add(item);
    }
    {
      JkBindingItem item = new JkBindingItem("Host", "server.com"); // TODO NON-NLS NON-NLS
      item.field().value = worker == null ? null : worker.map.get("host");
      item.setId("workerHost");
      items.add(item);
    }
    {
      JkBindingItem item = new JkBindingItem("Port", "8080"); // TODO NON-NLS NON-NLS
      item.field().value = worker == null ? null : worker.map.get("port");
      item.setId("workerPort");
      items.add(item);
    }
    
    load(items, null, null);
  }
  
//  protected El addFieldDelete(El cell, String titleDelete, String titleUndelete) {
//
//    El field = new El("div");
//    
//    El buttonDelete = new El("input");
//    buttonDelete.classList.add("button-delete");
//    buttonDelete.classList.add("button-delete_delete");
//    buttonDelete.setAttribute("type", "image");
//    buttonDelete.setAttribute("src", "img/delete.png");
//    if (titleDelete != null) {
//      buttonDelete.setAttribute("title", titleDelete);
//    }
//    
//    El buttonUndelete = new El("input");
//    buttonUndelete.classList.add("button-delete");
//    buttonUndelete.classList.add("button-delete_undelete");
//    buttonUndelete.setAttribute("type", "image");
//    buttonUndelete.setAttribute("src", "img/undelete.png");
//    if (titleUndelete != null) {
//      buttonUndelete.setAttribute("title", titleUndelete);
//    }
//    
//    field.appendChild(buttonDelete);
//    field.appendChild(buttonUndelete);
//    
//    El wrapper = Fields.wrapCellPad(field);  
//    cell.appendChild(wrapper);
//    
//    return field;
//  }



  @Override
  protected El createRow(JkBindingItem item, TabIndex tabIndex) {
    
    FieldTextLabel label = new FieldTextLabel();
    label.setInnerHTML(item.fieldLabel(), true);
    label.addClass("field-label");
    
    El field;
    
    if ("active".equals(item.getId())) {
      
      boolean value = !"false".equals(item.field().value);
      Boolean valueOriginal = item.field().valueOriginal == null ? null : !"false".equals(item.field().valueOriginal);
      field = new FieldCheckBox(item.getId(), value, valueOriginal, false, null);
      
    } else {
      
      String value = item.field().value;
      String valueOriginal = item.field().valueOriginal;
      field = new FieldTextInput(item.getId(), value, valueOriginal, item.placeholder(), false, null);
    }
    
    El row = new El("div");
    row.addClass("row");
    
    row.appendChild(new El("div").addClass("cell cell_field-label").appendChild(Fields.wrapCellPad(label)));
    row.appendChild(new El("div").addClass("cell cell_field").appendChild(Fields.wrapCellPad(field)));
    
    return row;
  }



  @Override
  protected El createRowCreated(JkBindingItem item, TabIndex tabIndex) {
    return null;
  }

  @Override
  protected El createHeader() {
    return null;
  }
}

