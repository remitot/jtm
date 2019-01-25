package org.jepria.tomcat.manager.web.logmonitor;

import java.util.List;

public class MonitorGuiParams {
  /**
   * Non-null. Name of the monitored file to display at the html head 
   */
  private final String filename;
  /**
   * Non-null. Name (and port) of the current host to display at the html head
   */
  private final String host;
  /**
   * Non-null, maybe empty. Log content lines to be displayed above the split anchor.
   */
  private final List<String> contentLinesTop;
  /**
   * Non-null, maybe empty. Log content lines to be displayed below the split anchor.
   */
  private final List<String> contentLinesBottom;
  /**
   * Non-null. Whether the first line of the {@link #contentLinesTop} is the first line in the file.
   */
  private final boolean fileBeginReached;
  /**
   * Nullable. If the log monitor client window can be scrolled up (to load the next portion of top lines), then the value is the url to load.
   */
  private final String loadTopUrl;
  /**
   * Nullable. If the displayed anchor can be reset (to the last loaded line, then the value is the url to reset.
   */
  private final String resetAnchorUrl;
  
  public MonitorGuiParams(String filename, String host, List<String> contentLinesTop, List<String> contentLinesBottom,
      boolean fileBeginReached, String loadTopUrl, String resetAnchorUrl) {
    this.filename = filename;
    this.host = host;
    this.contentLinesTop = contentLinesTop;
    this.contentLinesBottom = contentLinesBottom;
    this.fileBeginReached = fileBeginReached;
    this.loadTopUrl = loadTopUrl;
    this.resetAnchorUrl = resetAnchorUrl;
  }

  public String getFilename() {
    return filename;
  }

  public String getHost() {
    return host;
  }

  public List<String> getContentLinesTop() {
    return contentLinesTop;
  }

  public List<String> getContentLinesBottom() {
    return contentLinesBottom;
  }

  public boolean isFileBeginReached() {
    return fileBeginReached;
  }

  public String getLoadTopUrl() {
    return loadTopUrl;
  }

  public String getResetAnchorUrl() {
    return resetAnchorUrl;
  }
  
  
}
