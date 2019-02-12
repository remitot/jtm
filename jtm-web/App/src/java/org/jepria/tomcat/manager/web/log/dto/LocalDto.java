package org.jepria.tomcat.manager.web.log.dto;

public class LocalDto {
  private String lastModifiedDate;
  private String lastModifiedTime;
  private Integer lastModifiedAgoVerb;
  
  public LocalDto() {}
  
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
  
  public Integer getLastModifiedAgoVerb() {
    return lastModifiedAgoVerb;
  }

  public void setLastModifiedAgoVerb(Integer lastModifiedAgoVerb) {
    this.lastModifiedAgoVerb = lastModifiedAgoVerb;
  }
}
