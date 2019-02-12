package org.jepria.tomcat.manager.web.log.dto;

public class LogDto {
  private String name;
  private Long lastModified;
  private String lastModifiedDateLocal;
  private String lastModifiedTimeLocal;
  private Long lastModifiedAgo;
  private Integer lastModifiedAgoVerb;
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

  public String getLastModifiedDateLocal() {
    return lastModifiedDateLocal;
  }

  public void setLastModifiedDateLocal(String lastModifiedDateLocal) {
    this.lastModifiedDateLocal = lastModifiedDateLocal;
  }

  public String getLastModifiedTimeLocal() {
    return lastModifiedTimeLocal;
  }

  public void setLastModifiedTimeLocal(String lastModifiedTimeLocal) {
    this.lastModifiedTimeLocal = lastModifiedTimeLocal;
  }

  public Long getLastModifiedAgo() {
    return lastModifiedAgo;
  }

  public void setLastModifiedAgo(Long lastModifiedAgo) {
    this.lastModifiedAgo = lastModifiedAgo;
  }
  
  public Integer getLastModifiedAgoVerb() {
    return lastModifiedAgoVerb;
  }

  public void setLastModifiedAgoVerb(Integer lastModifiedAgoVerb) {
    this.lastModifiedAgoVerb = lastModifiedAgoVerb;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }
}
