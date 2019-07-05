package org.jepria.tomcat.manager.web.log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.jepria.tomcat.manager.web.log.dto.LogDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;

public class LogPageContent implements Iterable<El> {

  private final Iterable<El> elements;
  
  protected final Context context;
  
  @Override
  public Iterator<El> iterator() {
    return elements.iterator();
  }
  
  protected final DateTimeFormat clientDateTimeFormat;
  protected final DateTimeFormat gmtDateTimeFormat;
  
  /**
   * 
   * @param logs
   * @param clientTimeZone if non-null, the file last modified timestamp will be displayed in this timezone, otherwise in UTC timezone 
   */
  public LogPageContent(Context context, List<LogDto> logs, TimeZone clientTimeZone) {
    this.context = context;
    
    clientDateTimeFormat = clientTimeZone == null ? null : new DateTimeFormat(clientTimeZone);
    gmtDateTimeFormat = new DateTimeFormat(TimeZone.getTimeZone("GMT"));
    
    final List<El> elements = new ArrayList<>();
    
    // table html
    final LogTable table = new LogTable(context);
    
    final List<LogTable.Record> items = logs.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    table.load(items, null, null);
    
    elements.add(table);

    this.elements = Collections.unmodifiableList(elements);
  }
  
  /**
   * Threshold for a log file size in bytes to warn the user it is large 
   */
  public static final long FILE_SIZE_THRESHOLD_LARGE = 52428800;
  
  protected LogTable.Record dtoToItem(LogDto dto) {
    final LogTable.Record item = new LogTable.Record();
    
    item.name().value = dto.getName();
    
    if (dto.getLastModified() != null) {
      item.lastmod().value = getItemLastModifiedValue(dto.getLastModified());
    }
    
    item.largeFile = dto.getSize() != null && dto.getSize() >= FILE_SIZE_THRESHOLD_LARGE;
    
    item.size_().value = getItemSizeValue(dto.getSize());
    
    item.download().value = "api/log?filename=" + dto.getName();
    
    item.open().value = "api/log?filename=" + dto.getName() + "&inline";
    
    item.monitor().value = "log-monitor?filename=" + dto.getName();
    
    return item;
  }
  
  protected String getItemLastModifiedValue(long lastModifiedTimestamp) {
    
    Text text = context.getText();
    
    final String lastModifiedDateTime;
    
    final long lastModifiedAgoMs = System.currentTimeMillis() - lastModifiedTimestamp;
    String lastModifiedAgoVerb;
    
    
    {
      if (lastModifiedAgoMs <= 10000) {
        lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb1");
      } else if (lastModifiedAgoMs <= 90000) {
        lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb2");
      } else if (lastModifiedAgoMs <= 150000) {
        lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb3");
      } else if (lastModifiedAgoMs <= 210000) {
        lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb4");
      } else if (lastModifiedAgoMs <= 450000) {
        lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb5");
      } else if (lastModifiedAgoMs <= 900000) {
        lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb6");
      } else if (lastModifiedAgoMs <= 2700000) {
        lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb7");
      } else if (lastModifiedAgoMs <= 5400000) {
        lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb8");
      } else if (lastModifiedAgoMs <= 9000000) {
        lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb9");
      } else if (lastModifiedAgoMs <= 12600000) {
        lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb10");
      } else {
        lastModifiedAgoVerb = null;
      }
    }
    
    
    final Date lastModified = new Date(lastModifiedTimestamp);
    
    if (clientDateTimeFormat == null) {
      // the client timezone has not been set
      
      lastModifiedDateTime = gmtDateTimeFormat.formatDate(lastModified) + " " + gmtDateTimeFormat.formatTime(lastModified) + " GMT";

      // neither 'today' nor 'yesterday' without client timezone
      
    } else {
      // the client timezone has been set
      
      final String lastModifiedDateStr = clientDateTimeFormat.formatDate(lastModified);
      
      lastModifiedDateTime = lastModifiedDateStr + " " + clientDateTimeFormat.formatTime(lastModified);
      
      if (lastModifiedAgoVerb == null) {
        
        {// try local 'today' or 'yesterday'
          final String todayDateLocal = clientDateTimeFormat.formatDate(new Date());
          final String yesterdayDateLocal = clientDateTimeFormat.formatDate(new Date(System.currentTimeMillis() - 86400000));
          
          if (todayDateLocal.equals(lastModifiedDateStr)) {
            // modified on the local 'today'
            lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb11");
          } else if (yesterdayDateLocal.equals(lastModifiedDateStr)) {
            // modified on the local 'yesterday'
            lastModifiedAgoVerb = text.getString("org.jepria.tomcat.manager.web.log.item_lastMod_verb12");
          }
        }
        
      }
    }
    
    return lastModifiedDateTime + (lastModifiedAgoVerb == null ? "" : (", <b>" + lastModifiedAgoVerb + "</b>"));
  }
  
  /**
   * 
   * @param size in bytes
   * @return
   */
  protected String getItemSizeValue(Long size) {
    if (size == null) {
      return null;
    }
    
    String value;
    String unit;
    
    if (size < 1048576) {
      long kb = (long)Math.ceil((double)size / 1024);
      value = String.format("%4d", kb);
      unit = "KB"; // TODO non-nls;
    } else if (size < 1073741824) {
      long mb = (long)Math.ceil((double)size / 1048576);
      value = String.format("%4d", mb);
      unit = "MB"; // TODO non-nls;
    } else {
      double gb = (double)size / 1073741824;
      value = String.format(Locale.UK, "%4.1f", gb);
      unit = "GB"; // TODO non-nls;
    }
    
    return value + " " + unit;
  }
  
  private static class DateTimeFormat {
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat timeFormat;
    
    public DateTimeFormat(TimeZone clientTimeZone) {
      dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      dateFormat.setTimeZone(clientTimeZone);
      timeFormat = new SimpleDateFormat("HH:mm:ss");
      timeFormat.setTimeZone(clientTimeZone);
    }
    public String formatDate(Date date) {
      return dateFormat.format(date);
    }
    public String formatTime(Date date) {
      return timeFormat.format(date);
    }
  }
}
