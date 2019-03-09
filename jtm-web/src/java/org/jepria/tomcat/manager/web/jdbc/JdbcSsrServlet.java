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
import org.jepria.tomcat.manager.web.jdbc.dto.ModRequestDto;
import org.jepria.tomcat.manager.web.jdbc.ssr.JdbcItem;
import org.jepria.tomcat.manager.web.jdbc.ssr.JdbcTable;
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
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
    item.dataModifiable = dto.getDataModifiable();
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
    item.active().value = "true";
    item.active().readonly = true;
    
    // create items do not have client 'item-id's
    
    item.dataModifiable = true;
    return item;
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    final String path = req.getPathInfo();
    
    if (path == null || "".equals(path)) {
      
      try {
        
        StatusBar statusBar = null; 
        
        // table html
        final List<ConnectionDto> connections = new JdbcApi().list(EnvironmentFactory.get(req));
        
        // forward compatibility
        final List<JdbcItem> items = connections.stream()
            .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
        
        
        final JdbcTable table = new JdbcTable();
        
        final List<JdbcItem> itemsCreated = new ArrayList<>();
        final Set<String> itemsDeleted = new HashSet<>();
        
        
        final Mod mod = (Mod)req.getSession().getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod");
        
        if (mod != null) {
          
          if (mod.success) {
            final String statusBarHTML = "<span class=\"span-bold\">Все изменения сохранены.</span>"; // NON-NLS
            statusBar = new StatusBar(StatusBar.Type.SUCCESS, statusBarHTML);
            
          } else {
          
            final String statusBarHTML = "При попытке сохранить изменения обнаружились некорректные значения полей (выделены красным). " +
                "<span class=\"span-bold\">На сервере всё осталось без изменений.</span>"; // NON-NLS 
            statusBar = new StatusBar(StatusBar.Type.ERROR, statusBarHTML);
            
            // obtain created and deleted items, apply modifications
            final List<ModRequestDto> modRequests = mod.modRequests;
            if (modRequests != null) {
              for (ModRequestDto modRequest: modRequests) {
                final String action = modRequest.getAction();
                
                if ("create".equals(action)) {
                  itemsCreated.add(dtoToItemCreated(modRequest.getData()));
                  
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
            final Map<String, ModStatus> modStatuses = mod.modStatuses;
            if (modStatuses != null) {
              for (Map.Entry<String, ModStatus> modRequestIdAndModStatus: modStatuses.entrySet()) {
                String modRequestId = modRequestIdAndModStatus.getKey();
                
                if (!itemsDeleted.contains(modRequestId)) { //ignore deleted items
                  
                  ModStatus modStatus = modRequestIdAndModStatus.getValue(); 
                  if (modStatus.code == ModStatus.SC_INVALID_FIELD_DATA) {
                    
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
                      for (Map.Entry<String, ModStatus.InvalidFieldDataCode> idAndInvalidFieldDataCode:
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
        }
        
        table.load(items, itemsCreated, itemsDeleted);
        
        final String tableHtml = table.printHtml();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableHtml", tableHtml);
        
        // table script
        final String tableScript = table.printScripts();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableScript", tableScript);
        
        // table style
        final String tableStyle = table.printStyles();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableStyle", tableStyle);
        
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
        final String tableNewRowTemplateHtml = table.createRowCreated(emptyItem, newRowTemplateTabIndex).printHtml();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.tableNewRowTemplateHtml", tableNewRowTemplateHtml);
        
        // control buttons
        final ControlButtons controlButtons = new ControlButtons();
        
        final String controlButtonsHtml = controlButtons.printHtml();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.controlButtonsHtml", controlButtonsHtml);
        
        final String controlButtonsScript = controlButtons.printScripts();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.controlButtonsScript", controlButtonsScript);
        
        // status bar
        final String statusBarHtml;
        final String statusBarScript;
        
        if (statusBar != null) {
          statusBarHtml = statusBar.printHtml();
          statusBarScript = statusBar.printScripts();
        } else {
          statusBarHtml = statusBarScript = "";
        }
        
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.statusBarHtml", statusBarHtml);
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.statusBarScript", statusBarScript);
        
        // forward to the target page
        req.getRequestDispatcher("/gui/jdbc-ssr/jdbc-target.jsp").forward(req, resp);
        return;
        
      } catch (Throwable e) {
        e.printStackTrace();

        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.flushBuffer();
        return;
      }

    } else {
      
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String path = req.getPathInfo();
    
    if ((path == null || "".equals(path))) {
      
      if (req.getParameter("mod") != null) {
      
        // read list from request body
        final List<ModRequestDto> modRequests;
        
        try {
          Type type = new TypeToken<ArrayList<ModRequestDto>>(){}.getType();
          modRequests = new Gson().fromJson(new InputStreamReader(req.getInputStream()), type);
          
        } catch (Throwable e) {
          // TODO
          throw new RuntimeException(e);
        }
        
        // Map<modRequestId, modStatus>
        final Map<String, ModStatus> modStatuses = new HashMap<>();
        
        final Environment env = EnvironmentFactory.get(req);
        
        final boolean createContextResources = "true".equals(
            env.getProperty("org.jepria.tomcat.manager.web.jdbc.createContextResources"));
        
        final TomcatConfJdbc tomcatConf = new TomcatConfJdbc(
            () -> env.getContextXmlInputStream(), 
            () -> env.getServerXmlInputStream(),
            createContextResources);

        final JdbcApi api = new JdbcApi();
        
        boolean allModSuccess = true; 
        
        // 1) perform all updates
        for (ModRequestDto modRequest: modRequests) {
          if ("update".equals(modRequest.getAction())) {
            ModStatus modStatus = api.updateConnection(
                modRequest.getId(), modRequest.getData(), tomcatConf);
            if (modStatus.code != ModStatus.SC_SUCCESS) {
              allModSuccess = false;
            }
            modStatuses.put(modRequest.getId(), modStatus);
          }
        }


        // 2) perform all deletions
        for (ModRequestDto modRequest: modRequests) {
          if ("delete".equals(modRequest.getAction())) {
            ModStatus modStatus = api.deleteConnection(
                modRequest.getId(), tomcatConf);
            if (modStatus.code != ModStatus.SC_SUCCESS) {
              allModSuccess = false;
            }
            modStatuses.put(modRequest.getId(), modStatus);
          }
        }


        // 3) perform all creations
        for (ModRequestDto modRequest: modRequests) {
          if ("create".equals(modRequest.getAction())) {
            ModStatus modStatus = api.createConnection(modRequest.getData(), tomcatConf, 
                env.getResourceInitialParams());
            if (modStatus.code != ModStatus.SC_SUCCESS) {
              allModSuccess = false;
            }
            modStatuses.put(modRequest.getId(), modStatus);
          }
        }


        // 4) ignore illegal actions

        
        final Mod mod = new Mod();
        
        if (allModSuccess) {
          // save modifications and add a new _list to the response
          
          // Note: it is safe to save modifications to context.xml file here (before servlet response), 
          // because although Tomcat reloads the context after context.xml modification, 
          // it still fulfills the servlet requests currently under processing. 
          tomcatConf.save(env.getContextXmlOutputStream(), 
              env.getServerXmlOutputStream());
          
          mod.success = true;
          
        } else {
          
          mod.success = false;
          mod.modRequests = modRequests;
          mod.modStatuses = modStatuses;
        }
        
        req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod", mod);
        
        // redirect to the base ssr url must be made by the client
        return;
        
      } else if (req.getParameter("mod-reset") != null) {
        modReset(req);
        
        // redirect to the base ssr url must be made by the client
        return;
      }
      
    } else {
   // TODO
      throw new RuntimeException();
    }
  }
  
  private void modReset(HttpServletRequest req) {
    req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod");
  }
  
  private static class Mod {
    public boolean success;
    public List<ModRequestDto> modRequests;
    public Map<String, ModStatus> modStatuses;
  }
}
