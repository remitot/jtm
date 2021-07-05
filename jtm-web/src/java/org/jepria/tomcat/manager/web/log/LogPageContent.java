package org.jepria.tomcat.manager.web.log;

import org.jepria.tomcat.manager.web.log.dto.LogDto;
import org.jepria.web.ssr.*;
import org.jepria.web.ssr.fields.Table;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    final List<Table.CellHeader> header = createTableHeader();
    final Table<Table.Row> table = new Table<>(context, header);
    table.addStyle("css/log/log.css");
    
    final List<Table.Row> rows = logs.stream()
        .map(dto -> dtoToRow(dto)).collect(Collectors.toList());
    
    table.load(rows, null, null);
    
    elements.add(table);

    this.elements = Collections.unmodifiableList(elements);
  }
  
  protected List<Table.CellHeader> createTableHeader() {
    final List<Table.CellHeader> header = new ArrayList<>();
    
    Text text = context.getText();

    header.add(Table.Cells.header(text.getString("org.jepria.tomcat.manager.web.log.Table.header.column_name"), "name"));
    header.add(Table.Cells.header(text.getString("org.jepria.tomcat.manager.web.log.Table.header.column_lastmod"), "lastmod"));
    header.add(Table.Cells.header(text.getString("org.jepria.tomcat.manager.web.log.Table.header.column_size"), "size"));
    header.add(Table.Cells.header(null, "download"));
    header.add(Table.Cells.header(null, "open"));
    header.add(Table.Cells.header(null, "monitor"));

    return header;
  }
  
  /**
   * Threshold for a log file size in bytes to warn the user it is large 
   */
  public static final long FILE_SIZE_THRESHOLD_LARGE = 10485760; // 10 MB
  
  protected Table.Row dtoToRow(LogDto dto) {
    final Table.Row row = new Table.Row();
    
    Text text = context.getText();
    
    {
      String value = dto.getName();
      Table.Cell cell = Table.Cells.withStaticValue(value, "name");
      row.add(cell);
    }
    
    {
      final Node node;
      {
        if (dto.getLastModified() != null) {
          ItemLastModifiedInfo itemLastModifiedInfo = getItemLastModifiedInfo(dto.getLastModified());
          Node nodeDate = Node.fromHtml(HtmlEscaper.escape(itemLastModifiedInfo.lastModifiedDateTime, true));
          
          if (itemLastModifiedInfo.lastModifiedAgoVerb != null) {
            Node nodeComma = Node.fromHtml(", ");
            Node nodeHint = new El("b", context).setInnerHTML(itemLastModifiedInfo.lastModifiedAgoVerb);
            node = Node.fromNodes(nodeDate, nodeComma, nodeHint);
          } else {
            node = nodeDate;
          }
        } else {
          node = null;
        }
      }
      Table.Cell cell = Table.Cells.withNode(node, "lastmod");
      row.add(cell);
    }

    ItemSizeInfo itemSizeInfo = getItemSizeInfo(dto);
    String largeFileHintTitle = null;
    if (itemSizeInfo.largeFile) {
      largeFileHintTitle = text.getString("org.jepria.tomcat.manager.web.log.item.largeFile") + " (" + itemSizeInfo.sizeHint + ")";
    }
    
    {
      final Node node;
      {
        String valueEsc = HtmlEscaper.escape(itemSizeInfo.sizeFieldValue, true);
        if (itemSizeInfo.largeFile) {
          node = new El("b", context)
              .addClass("b_large-file")
              .setAttribute("title", largeFileHintTitle)
              .setInnerHTML(valueEsc);
        } else {
          node = Node.fromHtml(valueEsc);
        }
      }
      
      Table.Cell cell = Table.Cells.withNode(node, "size");
      row.add(cell);
    }

    {
      final Node node;
      {
        El a = new El("a", context)
            .setAttribute("href", context.getAppContextPath() + "/api/log?filename=" + dto.getName()) // TODO escape or not?
            .setAttribute("title", text.getString("org.jepria.tomcat.manager.web.log.item_download.title"))
            .setInnerHTML(text.getString("org.jepria.tomcat.manager.web.log.item_download.text"));
  
        if (itemSizeInfo.largeFile) {
          El img = new El("img", context)
              .addClass("field-text__hint_large-file")
              .setAttribute("src", context.getAppContextPath() + "/img/log/hint.png")
              .setAttribute("title", largeFileHintTitle);
          node = Node.fromNodes(a, img);
        } else {
          node = a;
        }
      }

      Table.Cell cell = Table.Cells.withNode(node, "download");
      row.add(cell);
    }


    {
      final Node node;
      {
        El a = new El("a", context)
            .setAttribute("href", context.getAppContextPath() + "/api/log?filename=" + dto.getName() + "&inline") // TODO escape or not?
            .setAttribute("target", "_blank")
            .setAttribute("title", text.getString("org.jepria.tomcat.manager.web.log.item_open.title"))
            .setInnerHTML(text.getString("org.jepria.tomcat.manager.web.log.item_open.text"));

        if (itemSizeInfo.largeFile) {
          String hintTitle = text.getString("org.jepria.tomcat.manager.web.log.item.largeFile") + " (" + itemSizeInfo.sizeHint + ")";
          El img = new El("img", context).addClass("field-text__hint_large-file")
              .setAttribute("src", context.getAppContextPath() + "/img/log/hint.png")
              .setAttribute("title", hintTitle);
          node = Node.fromNodes(a, img);
        } else {
          node = a;
        }
      }

      Table.Cell cell = Table.Cells.withNode(node, "open");
      row.add(cell);
    }

    {
      final Node node;
      {
        node = new El("a", context)
            .setAttribute("href", context.getAppContextPath() + "/log-monitor?filename=" + dto.getName()) // TODO escape or not?
            .setAttribute("target", "_blank")
            .setAttribute("title", text.getString("org.jepria.tomcat.manager.web.log.item_monitor.title"))
            .setInnerHTML(text.getString("org.jepria.tomcat.manager.web.log.item_monitor.text"));
      }

      Table.Cell cell = Table.Cells.withNode(node, "monitor");
      row.add(cell);
    }
    
    return row;
  }
  
  protected static class ItemSizeInfo {
    /**
     * Whether or not the field is large
     */
    public boolean largeFile;
    /**
     * Value to display in the field
     */
    public String sizeFieldValue;
    /**
     * Hint text
     */
    public String sizeHint;
  }
  
  protected ItemSizeInfo getItemSizeInfo(LogDto dto) {
    if (dto == null) {
      return null;
    }
    
    ItemSizeInfo ret = new ItemSizeInfo();
    
    final Long size = dto.getSize();
    
    if (size == null) {
      ret.largeFile = false;
      ret.sizeFieldValue = null;
      ret.sizeHint = null;
      
    } else {
      ret.largeFile = size >= FILE_SIZE_THRESHOLD_LARGE;
      
      String value;
      String unit;
      
      if (size < 1048576) {
        long kb = (long)Math.ceil((double)size / 1024);
        value = String.valueOf(kb);
        unit = "KB"; // TODO non-nls;
      } else if (size < 1073741824) {
        long mb = (long)Math.ceil((double)size / 1048576);
        value = String.valueOf(mb);
        unit = "MB"; // TODO non-nls;
      } else {
        double gb = (double)size / 1073741824;
        value = String.format(Locale.UK, "%.1f", gb);
        { // trim trailing ".0", if any
          final String decimalZero = ".0";
          if (value.endsWith(decimalZero)) {
            value = value.substring(0, value.length() - decimalZero.length());
          }
        }
        unit = "GB"; // TODO non-nls;
      }
      
      ret.sizeFieldValue = String.format("%4s", value) + " " + unit;
      ret.sizeHint = value + " " + unit;
    }
    
    return ret;
  }

  protected static class ItemLastModifiedInfo {
    public String lastModifiedDateTime;
    public String lastModifiedAgoVerb;
  }
  
  protected ItemLastModifiedInfo getItemLastModifiedInfo(long lastModifiedTimestamp) {
    
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

    ItemLastModifiedInfo info = new ItemLastModifiedInfo();
    info.lastModifiedDateTime = lastModifiedDateTime;
    info.lastModifiedAgoVerb = lastModifiedAgoVerb;
    
    return info;
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
