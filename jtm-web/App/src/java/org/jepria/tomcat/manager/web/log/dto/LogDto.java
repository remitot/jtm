package org.jepria.tomcat.manager.web.log.dto;

public class LogDto {
  private String name;
  private Long lastModified;
  private LocalDto local;
  private Long size;
  
  public LogDto() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getLastModified() {
    return lastModified;
  }

  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  public LocalDto getLocal() {
    return local;
  }

  public void setLocal(LocalDto local) {
    this.local = local;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }
}
