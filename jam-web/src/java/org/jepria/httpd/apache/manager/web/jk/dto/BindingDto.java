package org.jepria.httpd.apache.manager.web.jk.dto;

public class BindingDto {
  public final JkMountDto jkMount;
  public final WorkerDto worker;
  
  public BindingDto(JkMountDto jkMount, WorkerDto worker) {
    this.jkMount = jkMount;
    this.worker = worker;
  }
}
