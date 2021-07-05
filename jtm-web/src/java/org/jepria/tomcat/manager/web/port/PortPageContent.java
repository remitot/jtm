package org.jepria.tomcat.manager.web.port;

import org.jepria.tomcat.manager.web.port.dto.PortDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PortPageContent implements Iterable<El> {

  private final Iterable<El> elements;
  
  protected final Context context;
  
  @Override
  public Iterator<El> iterator() {
    return elements.iterator();
  }
  
  public PortPageContent(Context context, List<PortDto> ports) {
    this.context = context;
    
    final List<El> elements = new ArrayList<>();
    
    // table html
    final List<Table.CellHeader> header = createTableHeader();
    final Table<Table.Row> table = new Table<>(context, header);
    table.addStyle("css/port/port.css");
    
    final List<Table.Row> rows = ports.stream()
        .map(dto -> dtoToRow(dto)).collect(Collectors.toList());
    
    table.load(rows, null, null);
    
    elements.add(table);

    this.elements = Collections.unmodifiableList(elements);
  }

  protected List<Table.CellHeader> createTableHeader() {
    final List<Table.CellHeader> header = new ArrayList<>();

    Text text = context.getText();

    header.add(Table.Cells.header(text.getString("org.jepria.tomcat.manager.web.port.Table.header.column_type"), "type"));
    header.add(Table.Cells.header(text.getString("org.jepria.tomcat.manager.web.port.Table.header.column_port"), "port"));

    return header;
  }
  
  protected Table.Row dtoToRow(PortDto dto) {
    Table.Row row = new Table.Row();

    {
      String value = dto.getType();
      Table.Cell cell = Table.Cells.withStaticValue(value, "type");
      row.add(cell);
    }

    {
      String value = dto.getNumber();
      Table.Cell cell = Table.Cells.withStaticValue(value, "number");
      row.add(cell);
    }
    
    return row;
  }
  
}
