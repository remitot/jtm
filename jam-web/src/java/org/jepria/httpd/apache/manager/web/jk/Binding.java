package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.httpd.apache.manager.core.jk.JkMount;
import org.jepria.httpd.apache.manager.core.jk.Worker;

public interface Binding {
  String jkMountId();
  JkMount jkMount();
  String workerId();
  Worker worker();
}
