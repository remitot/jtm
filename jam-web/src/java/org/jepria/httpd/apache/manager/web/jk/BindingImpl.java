package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.httpd.apache.manager.core.jk.JkMount;
import org.jepria.httpd.apache.manager.core.jk.Worker;

public class BindingImpl implements Binding {

  private final String jkMountId;
  private final JkMount jkMount;
  
  private final String workerId;
  private final Worker worker;
  
  public BindingImpl(String jkMountId, JkMount jkMount, String workerId, Worker worker) {
    this.jkMountId = jkMountId;
    this.jkMount = jkMount;
    this.workerId = workerId;
    this.worker = worker;
  }

  @Override
  public String jkMountId() {
    return jkMountId;
  }

  @Override
  public JkMount jkMount() {
    return jkMount;
  }

  @Override
  public String workerId() {
    return workerId;
  }

  @Override
  public Worker worker() {
    return worker;
  }
}
