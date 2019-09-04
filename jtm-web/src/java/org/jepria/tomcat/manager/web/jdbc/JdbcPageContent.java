package org.jepria.tomcat.manager.web.jdbc;

import org.jepria.tomcat.manager.web.jdbc.JdbcApi.ItemModStatus;
import org.jepria.tomcat.manager.web.jdbc.JdbcApi.ItemModStatus.Code;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.web.data.ItemModRequestDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.Field;
import org.jepria.web.ssr.fields.Table.TabIndex;

import java.util.*;
import java.util.stream.Collectors;

public class JdbcPageContent extends ArrayList<El> {

  private static final long serialVersionUID = 1304559345888446859L;
  
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
    
    final List<JdbcTable.Record> items = connections.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    final List<JdbcTable.Record> itemsCreated = new ArrayList<>();
    final Set<String> itemsDeleted = new HashSet<>();
    
    // obtain created and deleted items, apply modifications
    if (itemModRequests != null) {
      for (ItemModRequestDto modRequest: itemModRequests) {
        final String action = modRequest.getAction();
        
        if ("create".equals(action)) {
          JdbcTable.Record item = dtoToItemCreated(modRequest.getData());
          item.setId(modRequest.getId());
          itemsCreated.add(item);
          
        } else if ("update".equals(action)) {
          
          // merge modifications into the existing item
          final String id = modRequest.getId();
          JdbcTable.Record target = items.stream().filter(
              item0 -> item0.getId().equals(id)).findAny().orElse(null);
          if (target == null) {
            // TODO cannot even treat as a new (because it can be filled only partially)
            throw new IllegalStateException("The item requested to modification not found: " + id);
          }
          Map<String, String> source = modRequest.getData();
          for (String sourceName: source.keySet()) {
            Field targetField = target.get(sourceName);
            if (targetField != null) {
              targetField.value = source.get(sourceName);
            }
          }
          
        } else if ("delete".equals(action)) {
          itemsDeleted.add(modRequest.getId());
        }
      }
    }
      
    // process invalid field data
    if (itemModStatuses != null) {
      for (Map.Entry<String, ItemModStatus> modRequestIdAndModStatus: itemModStatuses.entrySet()) {
        String modRequestId = modRequestIdAndModStatus.getKey();
        
        if (!itemsDeleted.contains(modRequestId)) { //ignore deleted items
          
          final ItemModStatus modStatus = modRequestIdAndModStatus.getValue();
          
          if (modStatus.code == Code.INVALID_FIELD_DATA) {
            if (modStatus.invalidFieldDataMap != null) {
              for (Map.Entry<String, ItemModStatus.InvalidFieldDataCode> idAndInvalidFieldDataCode:
                  modStatus.invalidFieldDataMap.entrySet()) {

                JdbcTable.Record item;
                {
                  // lookup item
                  item = items.stream().filter(item0 -> item0.getId().equals(modRequestId))
                          .findAny().orElse(null);
                  if (item == null) {
                    // lookup items created
                    item = itemsCreated.stream().filter(item0 -> item0.getId().equals(modRequestId))
                            .findAny().orElse(null);
                  }
                  if (item == null) {
                    // TODO
                    throw new IllegalStateException("No target item found by modRequestId [" + modRequestId + "]");
                  }
                }

                Field field = item.get(idAndInvalidFieldDataCode.getKey());
                if (field != null) {
                  field.invalid = true;
                  switch (idAndInvalidFieldDataCode.getValue()) {
                  case MANDATORY_EMPTY: {
                    field.invalidMessage = text.getString("org.jepria.tomcat.manager.web.jdbc.field.invalid.empty");
                    break;
                  }
                  case DUPLICATE_NAME: {
                    field.invalidMessage = text.getString("org.jepria.tomcat.manager.web.jdbc.field.invalid.duplicate_name");
                    break;
                  }
                  case DUPLICATE_GLOBAL: {
                    field.invalidMessage = text.getString("org.jepria.tomcat.manager.web.jdbc.field.invalid.duplicate_global");
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
    
    table.load(items, itemsCreated, itemsDeleted);
    
    add(table);

    // table row-create template
    final TabIndex newRowTemplateTabIndex = new TabIndex() {
      private int i = 0;
      @Override
      public void setNext(El el) {
        el.classList.add("has-tabindex-rel");
        el.setAttribute("tabindex-rel", i++);
      }
    };
    final JdbcTable.Record emptyItem = new JdbcTable.Record();
    emptyItem.active().readonly = true;
    emptyItem.active().value = "true";
    final El tableNewRowTemplate = table.createRowCreated(emptyItem, newRowTemplateTabIndex);
    
    final El tableNewRowTemplateContainer = new El("div", context).addClass("table-new-row-template-container")
        .appendChild(tableNewRowTemplate);
    add(tableNewRowTemplateContainer);
    
    
    // control buttons
    final ControlButtons controlButtons = new ControlButtons(context);
    controlButtons.addButtonCreate();
    controlButtons.addButtonSave(context.getContextPath() + "/jdbc/mod");// TODO such url will erase any path- or request params of the current page
    controlButtons.addButtonReset(context.getContextPath() + "/jdbc");// TODO such url will erase any path- or request params of the current page
    add(controlButtons);
  }
  
  protected JdbcTable.Record dtoToItem(ConnectionDto dto) {
    JdbcTable.Record item = new JdbcTable.Record();
    for (String name: dto.keySet()) {
      Field field = item.get(name);
      if (field != null) {
        field.value = field.valueOriginal = dto.get(name);
      }
    }
    item.setId(dto.get("id"));
    item.dataModifiable = dto.isDataModifiable();
    
    if (!item.dataModifiable) {
      item.active().readonly = true;
      item.server().readonly = true;
      item.db().readonly = true;
      item.user().readonly = true;
      item.password().readonly = true;
    }
    return item;
  }
  
  protected JdbcTable.Record dtoToItemCreated(Map<String, String> dto) {
    JdbcTable.Record item = new JdbcTable.Record();
    for (String name: dto.keySet()) {
      Field field = item.get(name);
      if (field != null) {
        field.value = dto.get(name);
      }
    }
    item.dataModifiable = true;
    item.active().value = "true";
    item.active().readonly = true;
    return item;
  }
}
