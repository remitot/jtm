package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.httpd.apache.manager.web.jk.dto.WorkerDto;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.fields.FieldTextLabel;

public class JkDetails extends El {
  
  public JkDetails() {
    super("div");
  }
  
  public void load(JkMountDto mount, WorkerDto worker) {
    if (mount == null) {
      appendChild(new El("div").setInnerHTML("mount is null!"));
    } else {
      appendChild(new El("div").appendChild(new FieldTextLabel("active:"))// TODO NON-NLS
          .appendChild(new FieldTextLabel(mount.map.get("active"))));
      appendChild(new El("div").appendChild(new FieldTextLabel("application:"))// TODO NON-NLS
          .appendChild(new FieldTextLabel(mount.map.get("application"))));
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

