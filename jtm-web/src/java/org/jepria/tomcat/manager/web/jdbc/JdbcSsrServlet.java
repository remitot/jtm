package org.jepria.tomcat.manager.web.jdbc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.core.jdbc.TomcatConfJdbc;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ModDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ItemModRequestDto;
import org.jepria.tomcat.manager.web.jdbc.ssr.JdbcItem;
import org.jepria.tomcat.manager.web.jdbc.ssr.JdbcTable;
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;
import org.jepria.web.ssr.StatusBar;
import org.jepria.web.ssr.table.Field;
import org.jepria.web.ssr.table.Table.TabIndex;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Servlet producing server-side-rendered pages
 */
public class JdbcSsrServlet extends HttpServlet {

  private static final long serialVersionUID = -2556094883694667549L;

  private static JdbcItem dtoToItem(ConnectionDto dto) {
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
  
  private static JdbcItem dtoToItemCreated(Map<String, String> dto) {
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
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    final El html = new El("html");
    
    final El head = new El("head")
        .appendChild(new El("title").setInnerHTML("Tomcat manager: датасорсы (JDBC)")) // NON-NLS
        .appendChild(new El("meta").setAttribute("http-equiv", "X-UA-Compatible").setAttribute("content", "IE=Edge"))
        .appendChild(new El("meta").setAttribute("http-equiv", "Content-Type").setAttribute("content", "text/html;charset=UTF-8"));
    
    final El body = new El("body").setAttribute("onload", "jtm_onload();table_onload();checkbox_onload();controlButtons_onload();");
    
    final String managerApacheHref = EnvironmentFactory.get(req).getProperty(
        "org.jepria.tomcat.manager.web.managerApacheHref");
    final PageHeader pageHeader = new PageHeader(managerApacheHref, CurrentMenuItem.JDBC);
    

    body.appendChild(pageHeader);
    
    // table html
    final List<ConnectionDto> connections = new JdbcApi().list(EnvironmentFactory.get(req));
    
    // forward compatibility
    final List<JdbcItem> items = connections.stream()
        .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
    
    
    final JdbcTable table = new JdbcTable();
    
    final List<JdbcItem> itemsCreated = new ArrayList<>();
    final Set<String> itemsDeleted = new HashSet<>();
    
    
    final ServletModStatus servletModStatus = (ServletModStatus)req.getSession().getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.servletModStatus");
    
    if (servletModStatus != null) {

      if (servletModStatus.success) {
        
        final String statusBarHTML = "<span class=\"span-bold\">Все изменения сохранены.</span>"; // NON-NLS
        StatusBar statusBar = new StatusBar(StatusBar.Type.SUCCESS, statusBarHTML);
        body.appendChild(statusBar);
        
      } else {
      
        final String statusBarHTML = "При попытке сохранить изменения обнаружились некорректные значения полей (выделены красным). " +
            "<span class=\"span-bold\">На сервере всё осталось без изменений.</span>"; // NON-NLS 
        StatusBar statusBar = new StatusBar(StatusBar.Type.ERROR, statusBarHTML);
        body.appendChild(statusBar);
        
        // obtain created and deleted items, apply modifications
        final List<ItemModRequestDto> modRequests = servletModStatus.itemModRequests;
        if (modRequests != null) {
          for (ItemModRequestDto modRequest: modRequests) {
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
        final Map<String, ItemModStatus> modStatuses = servletModStatus.itemModStatuses;
        if (modStatuses != null) {
          for (Map.Entry<String, ItemModStatus> modRequestIdAndModStatus: modStatuses.entrySet()) {
            String modRequestId = modRequestIdAndModStatus.getKey();
            
            if (!itemsDeleted.contains(modRequestId)) { //ignore deleted items
              
              ItemModStatus modStatus = modRequestIdAndModStatus.getValue(); 
              if (modStatus.code == ItemModStatus.SC_INVALID_FIELD_DATA) {
                
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
                
                if (modStatus.invalidFieldDataMap != null) {
                  for (Map.Entry<String, ItemModStatus.InvalidFieldDataCode> idAndInvalidFieldDataCode:
                      modStatus.invalidFieldDataMap.entrySet()) {
                    Field field = item.get(idAndInvalidFieldDataCode.getKey());
                    if (field != null) {
                      field.invalid = true;
                      switch (idAndInvalidFieldDataCode.getValue()) {
                      case MANDATORY_EMPTY: {
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
              } else {
                // TODO process other statuses
              }
            }
          }
        }
      }
      
      // reset the servlet mod status after the first request 
      modReset(req);
    }
    
    table.load(items, itemsCreated, itemsDeleted);
    
    body.appendChild(table);

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
    
    final El tableNewRowTemplateContainer = new El("div").setAttribute("id", "table-new-row-template-container")
        .appendChild(tableNewRowTemplate);
    body.appendChild(tableNewRowTemplateContainer);
    
    
    // control buttons
    final ControlButtons controlButtons = new ControlButtons();
    body.appendChild(controlButtons);
    
    

    // add all scripts and styles to the head
    for (String style: body.getStyles()) {
      head.appendChild(new El("link").setAttribute("rel", "stylesheet").setAttribute("href", style));
    }
    for (String script: body.getScripts()) {
      head.appendChild(new El("script").setAttribute("type", "text/javascript").setAttribute("src", script));
    }
    
    
    html.appendChild(head);
    html.appendChild(body);
    
    resp.setContentType("text/html; charset=UTF-8");
    resp.getWriter().print("<!DOCTYPE html>");
    html.render(resp.getWriter());
    resp.flushBuffer();
    return;
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final ModDto mod;
    
    try {
      Type type = new TypeToken<ModDto>(){}.getType();
      mod = new Gson().fromJson(new InputStreamReader(req.getInputStream()), type);
    } catch (Throwable e) {
      // TODO
      throw new RuntimeException(e);
    }
    
    if ("mod".equals(mod.getAction())) {
    
      // read list from request body
      final List<ItemModRequestDto> itemModRequests = mod.getData();
      
      if (itemModRequests != null && itemModRequests.size() > 0) {
      
        // Map<modRequestId, modStatus>
        final Map<String, ItemModStatus> itemModStatuses = new HashMap<>();
        
        final Environment env = EnvironmentFactory.get(req);
        
        final boolean createContextResources = "true".equals(
            env.getProperty("org.jepria.tomcat.manager.web.jdbc.createContextResources"));
        
        final TomcatConfJdbc tomcatConf = new TomcatConfJdbc(
            () -> env.getContextXmlInputStream(), 
            () -> env.getServerXmlInputStream(),
            createContextResources);
  
        final JdbcApi api = new JdbcApi();
        
        boolean modSuccess = true; 
        
        // 1) perform all updates
        for (ItemModRequestDto itemModRequest: itemModRequests) {
          if ("update".equals(itemModRequest.getAction())) {
            ItemModStatus itemModStatus = api.updateConnection(
                itemModRequest.getId(), itemModRequest.getData(), tomcatConf);
            if (itemModStatus.code != ItemModStatus.SC_SUCCESS) {
              modSuccess = false;
            }
            itemModStatuses.put(itemModRequest.getId(), itemModStatus);
          }
        }
  
  
        // 2) perform all deletions
        for (ItemModRequestDto itemModRequest: itemModRequests) {
          if ("delete".equals(itemModRequest.getAction())) {
            ItemModStatus itemModStatus = api.deleteConnection(
                itemModRequest.getId(), tomcatConf);
            if (itemModStatus.code != ItemModStatus.SC_SUCCESS) {
              modSuccess = false;
            }
            itemModStatuses.put(itemModRequest.getId(), itemModStatus);
          }
        }
  
  
        // 3) perform all creations
        for (ItemModRequestDto itemModRequest: itemModRequests) {
          if ("create".equals(itemModRequest.getAction())) {
            ItemModStatus itemModStatus = api.createConnection(itemModRequest.getData(), tomcatConf, 
                env.getResourceInitialParams());
            if (itemModStatus.code != ItemModStatus.SC_SUCCESS) {
              modSuccess = false;
            }
            itemModStatuses.put(itemModRequest.getId(), itemModStatus);
          }
        }
  
  
        // 4) ignore illegal actions
  
        
        final ServletModStatus servletModStatus = new ServletModStatus();
        
        if (modSuccess) {
          // save modifications and add a new _list to the response
          
          // Note: it is safe to save modifications to context.xml file here (before servlet response), 
          // because although Tomcat reloads the context after context.xml modification, 
          // it still fulfills the servlet requests currently under processing. 
          tomcatConf.save(env.getContextXmlOutputStream(), 
              env.getServerXmlOutputStream());
          
          servletModStatus.success = true;
          
        } else {
          
          servletModStatus.success = false;
          servletModStatus.itemModRequests = itemModRequests;
          servletModStatus.itemModStatuses = itemModStatuses;
        }
        
        req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.servletModStatus", servletModStatus);
        
        // redirect to the base ssr url must be made by the client
        return;
      }
      
    } else if ("mod-reset".equals(mod.getAction())) {
      modReset(req);
      
      // redirect to the base ssr url must be made by the client
      return;
    }
  }
  
  private void modReset(HttpServletRequest req) {
    req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.servletModStatus");
  }
  
  /**
   * Class representing a servlet status of the entire modification request
   */
  private static class ServletModStatus {
    public boolean success;
    public List<ItemModRequestDto> itemModRequests;
    public Map<String, ItemModStatus> itemModStatuses;
  }
}
