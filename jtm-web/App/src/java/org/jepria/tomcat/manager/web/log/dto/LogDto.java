package org.jepria.tomcat.manager.web.log.dto;

public class LogDto {
  private String name;
  private String lastModifiedDate;
  private String lastModifiedTime;
  private String lastModifiedTimezone;
  private Long size;
  
  public LogDto() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(String lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public String getLastModifiedTime() {
    return lastModifiedTime;
  }

  public void setLastModifiedTime(String lastModifiedTime) {
    this.lastModifiedTime = lastModifiedTime;
  }

  public String getLastModifiedTimezone() {
    return lastModifiedTimezone;
  }

  public void setLastModifiedTimezone(String lastModifiedTimezone) {
    this.lastModifiedTimezone = lastModifiedTimezone;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }
}
