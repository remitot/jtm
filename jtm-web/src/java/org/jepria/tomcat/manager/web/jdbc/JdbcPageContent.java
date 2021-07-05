package org.jepria.tomcat.manager.web.jdbc;

import org.jepria.tomcat.manager.web.jdbc.JdbcApi.ItemModStatus;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ItemModRequestDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.Table;

import java.util.*;
import java.util.stream.Collectors;

public class JdbcPageContent extends ArrayList<El> {

  /**
   * @param context
   * @param connections
   * @param itemModRequests mod requests to graphically overlay the table items with, may be null
   * @param itemModStatuses mod statuses to graphically overlay the table items with, may be null
   */
  // TODO consider removing overlay parameters and invoke a separate table.overlay(params) method (not in constructor)
  public JdbcPageContent(Context context, List<ConnectionDto> connections,
      List<ItemModRequestDto> itemModRequests,
      Map<String, ItemModStatus> itemModStatuses) {
    
    Text text = context.getText();

    // table html
    final JdbcTable table = new JdbcTable(context);

    final List<JdbcTable.JdbcRow> rows = connections.stream()
        .map(dto -> dtoToRow(dto)).collect(Collectors.toList());

    final List<JdbcTable.JdbcRow> rowsCreated = new ArrayList<>();
    final Set<JdbcTable.JdbcRow> rowsDeleted = new HashSet<>();

    // obtain created and deleted rows, apply modifications
    if (itemModRequests != null) {
      for (ItemModRequestDto modRequest: itemModRequests) {
        final String action = modRequest.getAction();

        if ("create".equals(action)) {
          JdbcTable.JdbcRow row = dtoToRowCreated(modRequest.getData());
          row.id = modRequest.getId();
          rowsCreated.add(row);

        } else if ("update".equals(action)) {

          // merge modifications into the existing item
          final String id = modRequest.getId();
          JdbcTable.JdbcRow targetRow = rows.stream().filter(
              item0 -> item0.id.equals(id)).findAny().orElse(null);
          if (targetRow == null) {
            // TODO cannot even treat as a new (because it can be filled only partially)
            throw new IllegalStateException("The item requested to modification not found: " + id);
          }
          ConnectionDto sourceDto = modRequest.getData();
          mergeValues(sourceDto, targetRow);

        } else if ("delete".equals(action)) {
          JdbcTable.JdbcRow rowDeleted = rows.stream().filter(row -> row.id.equals(modRequest.getId())).findFirst().orElse(null);
          if (rowDeleted == null) {
            throw new IllegalStateException("No row found by deleted ID");
          } else {
            rowsDeleted.add(rowDeleted);
          }
        }
      }
    }

    // process invalid field data
    if (itemModStatuses != null) {
      for (Map.Entry<String, ItemModStatus> modRequestIdAndModStatus: itemModStatuses.entrySet()) {
        String modRequestId = modRequestIdAndModStatus.getKey();

        if (!rowsDeleted.contains(modRequestId)) { //ignore deleted items

          final ItemModStatus modStatus = modRequestIdAndModStatus.getValue();

          if (modStatus.code == ItemModStatus.Code.INVALID_FIELD_DATA) {
            if (modStatus.invalidFieldDataMap != null) {
              for (Map.Entry<String, ItemModStatus.InvalidFieldDataCode> idAndInvalidFieldDataCode:
                  modStatus.invalidFieldDataMap.entrySet()) {

                JdbcTable.JdbcRow row;
                {
                  // lookup item
                  row = rows.stream().filter(item0 -> item0.id.equals(modRequestId))
                          .findAny().orElse(null);
                  if (row == null) {
                    // lookup items created
                    row = rowsCreated.stream().filter(item0 -> item0.id.equals(modRequestId))
                            .findAny().orElse(null);
                  }
                  if (row == null) {
                    // TODO
                    throw new IllegalStateException("No target item found by modRequestId [" + modRequestId + "]");
                  }
                }

                Table.CellField cell;
                switch (idAndInvalidFieldDataCode.getKey()) {
                  case "active": cell = row.active(); break; 
                  case "name": cell = row.name(); break; 
                  case "db": cell = row.db(); break; 
                  case "server": cell = row.server(); break; 
                  case "user": cell = row.user(); break; 
                  case "password": cell = row.password(); break;
                  default: cell = null;
                }
                
                if (cell != null) {
                  cell.invalid = true;
                  switch (idAndInvalidFieldDataCode.getValue()) {
                  case MANDATORY_EMPTY: {
                    cell.invalidMessage = text.getString("org.jepria.tomcat.manager.web.jdbc.field.invalid.empty");
                    break;
                  }
                  case DUPLICATE_NAME: {
                    cell.invalidMessage = text.getString("org.jepria.tomcat.manager.web.jdbc.field.invalid.duplicate_name");
                    break;
                  }
                  case DUPLICATE_GLOBAL: {
                    cell.invalidMessage = text.getString("org.jepria.tomcat.manager.web.jdbc.field.invalid.duplicate_global");
                    break;
                  }
                  }
                }
              }
            }
          }
        }
      }
    }

    table.load(rows, rowsCreated, rowsDeleted);

    add(table);

    // table row-create template
    final Table.TabIndex newRowTemplateTabIndex = new Table.TabIndex() {
      private int i = 0;
      @Override
      public void setNext(El el) {
        el.classList.add("has-tabindex-rel");
        el.setAttribute("tabindex-rel", i++);
      }
    };
    final JdbcTable.JdbcRow emptyItem = dtoToRowCreated(new ConnectionDto());
    final El tableNewRowTemplate = table.createRowCreated(emptyItem, newRowTemplateTabIndex);

    final El tableNewRowTemplateContainer = new El("div", context).addClass("table-new-row-template-container")
        .appendChild(tableNewRowTemplate);
    add(tableNewRowTemplateContainer);


    // control buttons
    final ControlButtons controlButtons = new ControlButtons(context);
    controlButtons.addButtonCreate();
    controlButtons.addButtonSave(context.getAppContextPath() + "/jdbc/mod");// TODO such url will erase any path- or request params of the current page
    controlButtons.addButtonReset(context.getAppContextPath() + "/jdbc");// TODO such url will erase any path- or request params of the current page
    add(controlButtons);
  }
  
  protected JdbcTable.JdbcRow dtoToRow(ConnectionDto dto) {
    JdbcTable.JdbcRow row = new JdbcTable.JdbcRow();
    row.id = dto.getId();
    row.dataModifiable = dto.isDataModifiable();
    
    {
      if (row.dataModifiable) {
        Table.CellField cell = new Table.CellField();
        cell.name = "active";
        cell.value = cell.valueOriginal = dto.getActive();
        cell.invalid = false;
        cell.invalidMessage = null;
        row.add(cell);
      } else {
        Table.CellStaticCheckbox cell = Table.Cells.withStaticCheckbox(dto.getActive(), "active");
        row.add(cell);
      }
    }
    
    {
      Table.CellField cell = new Table.CellField();
      cell.name = "name";
      cell.value = cell.valueOriginal = dto.getName();
      cell.invalid = false;
      cell.invalidMessage = null;
      row.add(cell);
    }

    {
      if (row.dataModifiable) {
        Table.CellField cell = new Table.CellField();
        cell.name = "server";
        cell.value = cell.valueOriginal = dto.getServer();
        cell.invalid = false;
        cell.invalidMessage = null;
        row.add(cell);
      } else {
        Table.CellStatic cell = Table.Cells.withStaticValue(dto.getServer(), "server");
        row.add(cell);
      }
    }

    {
      if (row.dataModifiable) {
        Table.CellField cell = new Table.CellField();
        cell.name = "db";
        cell.value = cell.valueOriginal = dto.getDb();
        cell.invalid = false;
        cell.invalidMessage = null;
        row.add(cell);
      } else {
        Table.CellStatic cell = Table.Cells.withStaticValue(dto.getDb(), "db");
        row.add(cell);
      }
    }

    {
      if (row.dataModifiable) {
        Table.CellField cell = new Table.CellField();
        cell.name = "user";
        cell.value = cell.valueOriginal = dto.getUser();
        cell.invalid = false;
        cell.invalidMessage = null;
        row.add(cell);
      } else {
        Table.CellStatic cell = Table.Cells.withStaticValue(dto.getUser(), "user");
        row.add(cell);
      }
    }

    {
      if (row.dataModifiable) {
        Table.CellField cell = new Table.CellField();
        cell.name = "password";
        cell.value = cell.valueOriginal = dto.getPassword();
        cell.invalid = false;
        cell.invalidMessage = null;
        row.add(cell);
      } else {
        Table.CellStatic cell = Table.Cells.withStaticValue(dto.getPassword(), "password");
        row.add(cell);
      }
    }
    
    return row;
  }

  /**
   * 
   * @param createdFields contains created field values only; null for fields which are not about to be created
   * @return
   */
  protected JdbcTable.JdbcRow dtoToRowCreated(ConnectionDto createdFields) {
    JdbcTable.JdbcRow row = new JdbcTable.JdbcRow();
    row.dataModifiable = true;

    {
      Table.CellStaticCheckbox cell = Table.Cells.withStaticCheckbox(false, "active"); 
      row.add(cell);
    }

    {
      Table.CellField cell = new Table.CellField();
      cell.name = "name";
      cell.value = createdFields.getName();
      cell.valueOriginal = null;
      cell.invalid = false;
      cell.invalidMessage = null;
      row.add(cell);
    }

    {
      Table.CellField cell = new Table.CellField();
      cell.name = "server";
      cell.value = createdFields.getServer();
      cell.valueOriginal = null;
      cell.invalid = false;
      cell.invalidMessage = null;
      row.add(cell);
    }

    {
      Table.CellField cell = new Table.CellField();
      cell.name = "db";
      cell.value = createdFields.getDb();
      cell.valueOriginal = null;
      cell.invalid = false;
      cell.invalidMessage = null;
      row.add(cell);
    }

    {
      Table.CellField cell = new Table.CellField();
      cell.name = "user";
      cell.value = createdFields.getUser();
      cell.valueOriginal = null;
      cell.invalid = false;
      cell.invalidMessage = null;
      row.add(cell);
    }

    {
      Table.CellField cell = new Table.CellField();
      cell.name = "password";
      cell.value = createdFields.getPassword();
      cell.valueOriginal = null;
      cell.invalid = false;
      cell.invalidMessage = null;
      row.add(cell);
    }

    return row;
  }
  
  protected static void mergeValues(ConnectionDto source, JdbcTable.JdbcRow target) {
    if (source != null && target != null) {
      if (source.getActive() != null) {
        target.active().value = source.getActive();
      }
      if (source.getName() != null) {
        target.name().value = source.getName();
      }
      if (source.getServer() != null) {
        target.server().value = source.getServer();
      }
      if (source.getDb() != null) {
        target.db().value = source.getDb();
      }
      if (source.getUser() != null) {
        target.user().value = source.getUser();
      }
      if (source.getPassword() != null) {
        target.password().value = source.getPassword();
      }
    }
  }
}
