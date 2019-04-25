package org.jepria.tomcat.manager.web.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ItemModRequestDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.table.Field;
import org.jepria.web.ssr.table.Table.TabIndex;

public class JdbcPageContent implements Iterable<El> {

  private final Iterable<El> elements;
  
  @Override
  public Iterator<El> iterator() {
    return elements.iterator();
  }
  
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
    
    final List<El> elements = new ArrayList<>();
    
    // table html
    final JdbcTable table = new JdbcTable(context);
    
    final List<JdbcItem> items = connections.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    final List<JdbcItem> itemsCreated = new ArrayList<>();
    final Set<String> itemsDeleted = new HashSet<>();
    
    // obtain created and deleted items, apply modifications
    if (itemModRequests != null) {
      for (ItemModRequestDto modRequest: itemModRequests) {
        final String action = modRequest.getAction();
        
        if ("create".equals(action)) {
          JdbcItem item = dtoToItemCreated(modRequest.getData());
          item.setId(modRequest.getId());
          itemsCreated.add(item);
          
        } else if ("update".equals(action)) {
          
          // merge modifications into the existing item
          final String id = modRequest.getId();
          JdbcItem target = items.stream().filter(
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
          
          // lookup items
          JdbcItem item = items.stream().filter(item0 -> item0.getId().equals(modRequestId))
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
          
          switch (modStatus.code) {
          case INVALID_FIELD_DATA: {
            
            if (modStatus.invalidFieldDataMap != null) {
              for (Map.Entry<String, ItemModStatus.InvalidFieldDataCode> idAndInvalidFieldDataCode:
                  modStatus.invalidFieldDataMap.entrySet()) {
                Field field = item.get(idAndInvalidFieldDataCode.getKey());
                if (field != null) {
                  field.invalid = true;
                  switch (idAndInvalidFieldDataCode.getValue()) {
                  case EMPTY: case MANDATORY_EMPTY: {
                    field.invalidMessage = "Поле не должно быть пустым"; // NON-NLS
                    break;
                  }
                  case DUPLICATE_NAME: {
                    field.invalidMessage = "Такое название уже есть"; // NON-NLS
                    break;
                  }
                  case DUPLICATE_GLOBAL: {
                    field.invalidMessage = "Такое название уже есть среди Context/ResourceLink.global " 
                        + "или Server/GlobalNamingResources/Resource.name"; // NON-NLS
                    break;
                  }
                  }
                }
              }
            }
            break;
          }
          case SUCCESS: {
            // do nothing
            break;
          }
          }
        }
      }
    }
    
    table.load(items, itemsCreated, itemsDeleted);
    
    elements.add(table);

    // table row-create template
    final TabIndex newRowTemplateTabIndex = new TabIndex() {
      private int i = 0;
      @Override
      public void setNext(El el) {
        el.classList.add("has-tabindex-rel");
        el.setAttribute("tabindex-rel", i++);
      }
    };
    final JdbcItem emptyItem = new JdbcItem();
    emptyItem.active().readonly = true;
    emptyItem.active().value = "true";
    final El tableNewRowTemplate = table.createRowCreated(emptyItem, newRowTemplateTabIndex);
    
    final El tableNewRowTemplateContainer = new El("div", context).setAttribute("id", "table-new-row-template-container")
        .appendChild(tableNewRowTemplate);
    elements.add(tableNewRowTemplateContainer);
    
    
    // control buttons
    final ControlButtons controlButtons = new ControlButtons(context, "jdbc/mod", "jdbc/mod-reset"); // TODO this will erase any path- or request params of the current page
    elements.add(controlButtons);
    
    this.elements = Collections.unmodifiableList(elements);
  }
  
  protected JdbcItem dtoToItem(ConnectionDto dto) {
    JdbcItem item = new JdbcItem();
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
  
  protected JdbcItem dtoToItemCreated(Map<String, String> dto) {
    JdbcItem item = new JdbcItem();
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
