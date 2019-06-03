package org.jepria.httpd.apache.manager.web.jk.dto;

public class BindingDto {
  public final JkMountDto jkMount;
  public final WorkerDto worker;
  
  
  public String httpPort;
  public String httpLink;
  /**
   * {@code 0} or {@code null}: AJP request succeeded or has not been executed
   * {@code 1}: AJP request failed
   */
  public Integer httpErrorCode;
  
  public BindingDto(JkMountDto jkMount, WorkerDto worker) {
    this.jkMount = jkMount;
    this.worker = worker;
  }
}
