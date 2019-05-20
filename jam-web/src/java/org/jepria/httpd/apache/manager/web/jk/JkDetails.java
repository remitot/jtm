package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.httpd.apache.manager.web.jk.dto.WorkerDto;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.fields.FieldCheckBox;
import org.jepria.web.ssr.fields.FieldTextInput;
import org.jepria.web.ssr.fields.FieldTextLabel;
import org.jepria.web.ssr.fields.Fields;

public class JkDetails extends El {
  
  public JkDetails() {
    super("div");
    
    addClass("details");
    setAttribute("id", "table");
    
    addStyle("css/table.css");
    addStyle("css/jk/details.css");
  }
  
  public void load(JkMountDto mount, WorkerDto worker) {
    if (mount == null) {
      appendChild(new El("div").setInnerHTML("mount is null!"));
    } else {
      {
        FieldTextLabel label = new FieldTextLabel();
        label.setInnerHTML("active", true);// TODO NON-NLS
        label.addClass("field-label");
        
        boolean value = !"false".equals(mount.map.get("active"));
        FieldCheckBox field = new FieldCheckBox(
            "active", value, value, false, null);
        
        El row = new El("div");
        row.addClass("row");
        
        row.appendChild(new El("div").addClass("cell field-label").appendChild(Fields.wrapCellPad(label)));
        row.appendChild(new El("div").addClass("cell field-active").appendChild(Fields.wrapCellPad(field)));
        
        appendChild(row);
      }
      {
        FieldTextLabel label = new FieldTextLabel();
        label.setInnerHTML("application", true);// TODO NON-NLS
        label.addClass("field-label");
        
        String value = mount.map.get("application");
        FieldTextInput field = new FieldTextInput(
            "application", value, value, "application", false, null);// TODO NON-NLS placeholder
        
        El row = new El("div");
        row.addClass("row");
        
        row.appendChild(new El("div").addClass("cell field-label").appendChild(Fields.wrapCellPad(label)));
        row.appendChild(new El("div").addClass("cell").appendChild(Fields.wrapCellPad(field)));
        
        appendChild(row);
      }
    }
    if (worker == null) {
      appendChild(new El("div").setInnerHTML("worker is null!"));
    } else {
      appendChild(new El("div").appendChild(new FieldTextLabel("name:"))// TODO NON-NLS
          .appendChild(new FieldTextLabel(worker.map.get("name"))));
      appendChild(new El("div").appendChild(new FieldTextLabel("host:"))// TODO NON-NLS
          .appendChild(new FieldTextLabel(worker.map.get("host"))));
      appendChild(new El("div").appendChild(new FieldTextLabel("type:"))// TODO NON-NLS
          .appendChild(new FieldTextLabel(worker.map.get("type"))));
      appendChild(new El("div").appendChild(new FieldTextLabel("port:"))// TODO NON-NLS
          .appendChild(new FieldTextLabel(worker.map.get("port"))));
    }
  }
}

