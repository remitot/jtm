package org.jepria.tomcat.manager.web.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.jepria.tomcat.manager.web.log.dto.LogDto;
import org.jepria.web.ssr.HtmlPage;

public class LogHtmlPage extends HtmlPage {
  
public static final String PAGE_TITLE = "Tomcat manager: логи"; // NON-NLS
  
  public final LogTable table;
  
  protected final DateTimeFormat clientDateTimeFormat;
  protected final DateTimeFormat gmtDateTimeFormat;
  
  /**
   * 
   * @param logs
   * @param clientTimeZone if non-null, the file last modified timestamp will be displayed in this timezone, otherwise in UTC timezone 
   */
  public LogHtmlPage(List<LogDto> logs, TimeZone clientTimeZone) {
    clientDateTimeFormat = clientTimeZone == null ? null : new DateTimeFormat(clientTimeZone);
    gmtDateTimeFormat = new DateTimeFormat(TimeZone.getTimeZone("GMT"));
    
    
    setTitle(PAGE_TITLE);
    
    // table html
    table = new LogTable();
    
    final List<LogItem> items = logs.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    table.load(items, null, null);
    
    getBodyChilds().add(table);

    
    body.addScript("css/jtm-common.css");
    body.setAttribute("onload", "jtm_onload();table_onload();");
  }
  
  protected LogItem dtoToItem(LogDto dto) {
    final LogItem item = new LogItem();
    
    item.name().value = dto.getName();
    
    if (dto.getLastModified() != null) {
      item.lastmod().value = getItemLastModifiedValue(dto.getLastModified());
    }
    
    item.download().value = "<a href=\"api/log?filename=" + dto.getName() + "\""
        + " title=\"Скачать на компьютер\">Сохранить</a>"; // NON-NLS // NON-NLS
    
    item.open().value = "<a href=\"api/log?filename=" + dto.getName() + "&inline\""
        + " target=\"_blank\" title=\"Открыть в новой вкладке браузера\">Посмотреть</a>"; // NON-NLS // NON-NLS
    
    item.monitor().value = "<a href=\"log-monitor?filename=" + dto.getName() + "\""
        + " target=\"_blank\" title=\"Открыть в читалке\">Отслеживать</a>"; // NON-NLS // NON-NLS
    
    return item;
  }
  
  protected String getItemLastModifiedValue(long lastModifiedTimestamp) {
    
    final String lastModifiedDate;
    final String lastModifiedTime;
    String lastModifiedAgoVerb = null;
    

    final long lastModifiedAgoMs = System.currentTimeMillis() - lastModifiedTimestamp;
    if (lastModifiedAgoMs <= 10000) {
      lastModifiedAgoVerb = "только что"; // NON-NLS;
    } else if (lastModifiedAgoMs <= 90000) {
      lastModifiedAgoVerb = "минуту назад"; // NON-NLS;
    } else if (lastModifiedAgoMs <= 150000) {
      lastModifiedAgoVerb = "две минуты назад"; // NON-NLS;
    } else if (lastModifiedAgoMs <= 210000) {
      lastModifiedAgoVerb = "три минуты назад"; // NON-NLS;
    } else if (lastModifiedAgoMs <= 450000) {
      lastModifiedAgoVerb = "пять минут назад"; // NON-NLS;
    } else if (lastModifiedAgoMs <= 900000) {
      lastModifiedAgoVerb = "10 минут назад"; // NON-NLS;
    } else if (lastModifiedAgoMs <= 2700000) {
      lastModifiedAgoVerb = "полчаса назад"; // NON-NLS;
    } else if (lastModifiedAgoMs <= 5400000) {
      lastModifiedAgoVerb = "час назад"; // NON-NLS;
    } else if (lastModifiedAgoMs <= 9000000) {
      lastModifiedAgoVerb = "два часа назад"; // NON-NLS;
    } else if (lastModifiedAgoMs <= 12600000) {
      lastModifiedAgoVerb = "три часа назад"; // NON-NLS;
    }
    
    
    final Date lastModified = new Date(lastModifiedTimestamp);
    
    if (clientDateTimeFormat == null) {
      
      lastModifiedDate = gmtDateTimeFormat.formatDate(lastModified);
      lastModifiedTime = gmtDateTimeFormat.formatTime(lastModified);

      // neither 'today' nor 'yesterday' without client timezone
      
      return lastModifiedDate + " " + lastModifiedTime + " GMT" + (lastModifiedAgoVerb == null ? "" : (", <b>" + lastModifiedAgoVerb + "</b>"));
      
    } else {
      
      lastModifiedDate = clientDateTimeFormat.formatDate(lastModified);
      lastModifiedTime = clientDateTimeFormat.formatTime(lastModified);
      
      if (lastModifiedAgoVerb == null) {
        // try local 'today' or 'yesterday'
        final String todayDateLocal = clientDateTimeFormat.formatDate(new Date());
        final String yesterdayDateLocal = clientDateTimeFormat.formatDate(new Date(System.currentTimeMillis() - 86400000));
        
        if (todayDateLocal.equals(lastModifiedDate)) {
          // modified on the local 'today'
          lastModifiedAgoVerb = "сегодня"; // NON-NLS;
        } else if (yesterdayDateLocal.equals(lastModifiedDate)) {
          // modified on the local 'yesterday'
          lastModifiedAgoVerb = "вчера"; // NON-NLS;
        }
      }
      
      return lastModifiedDate + " " + lastModifiedTime + (lastModifiedAgoVerb == null ? "" : (", <b>" + lastModifiedAgoVerb + "</b>"));
    }
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
