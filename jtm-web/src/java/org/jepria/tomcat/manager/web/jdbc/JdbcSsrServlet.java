package org.jepria.tomcat.manager.web.jdbc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.jdbc.JdbcApi.ModResponse;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ModRequestBodyDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ModRequestDto;
import org.jepria.tomcat.manager.web.jdbc.ssr.JdbcItem;
import org.jepria.tomcat.manager.web.jdbc.ssr.JdbcTable;
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
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
  
  private static JdbcItem dtoToCreateItem(ConnectionDto dto) {
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
        // table html
        final List<ConnectionDto> connections = new JdbcApi().list(EnvironmentFactory.get(req));
        
        // forward compatibility
        final List<JdbcItem> items = connections.stream()
            .map(dto -> dtoToItem(dto)).collect(Collectors.toList());
        
        
        final JdbcTable table = new JdbcTable();
        
        final List<JdbcItem> itemsCreated = new ArrayList<>();
        final Set<String> itemsDeleted = new HashSet<>();
        
        final List<ModRequestDto> modRequests = (List<ModRequestDto>)req.getSession().getAttribute(
            "org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modRequests");
        if (modRequests != null) {
          for (ModRequestDto modRequest: modRequests) {
            final ModRequestBodyDto modRequestBody = modRequest.getModRequestBody(); 
            final String action = modRequestBody.getAction();
            if ("create".equals(action)) {
              itemsCreated.add(dtoToCreateItem(modRequestBody.getData()));
            } else if ("update".equals(action)) {
              
              // merger modifications into the existing item
              final String id = modRequestBody.getId();
              JdbcItem target = items.stream().filter(
                  item0 -> item0.getId().equals(id)).findAny().orElse(null);
              if (target == null) {
                // TODO cannot even treat as a new (because it can be filled only partially)
                throw new IllegalStateException("The item requested to modification not found: " + id);
              }
              ConnectionDto source = modRequestBody.getData();
              for (String sourceName: source.keySet()) {
                Field targetField = target.get(sourceName);
                if (targetField != null) {
                  targetField.value = source.get(sourceName);
                }
              }
            } else if ("delete".equals(action)) {
              itemsDeleted.add(modRequestBody.getId());
            }
          }
        }
        
        final ModResponse modResponse = (ModResponse)req.getSession().getAttribute(
            "org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modResponse");
        if (modResponse != null) {
          for (Map.Entry<String, ModStatus> e: modResponse.modStatusMap.entrySet()) {
            ModStatus modStatus = e.getValue(); 
            if (modStatus.code == ModStatus.SC_INVALID_FIELD_DATA) {
              // TODO stopped here: mark fields in table invalid
//              modStatus.invalidFieldDataMap
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
        ControlButtons controlButtons = new ControlButtons();
        
        final String controlButtonsHtml = controlButtons.printHtml();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.controlButtonsHtml", controlButtonsHtml);
        
        final String controlButtonsScript = controlButtons.printScripts();
        req.setAttribute("org.jepria.tomcat.manager.web.jdbc.ssr.controlButtonsScript", controlButtonsScript);
        
        
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
        
        
        // convert list to map
        final Map<String, ModRequestBodyDto> modRequestBodyMap = new HashMap<>();
        
        if (modRequests != null) {
          for (ModRequestDto modRequest: modRequests) {
            final String modRequestId = modRequest.getModRequestId();

            // TODO or return 400?
            Objects.requireNonNull(modRequestId, "modRequestId must not be null");
            
            modRequestBodyMap.put(modRequestId, modRequest.getModRequestBody());
          }
        }
        
        
        ModResponse modResponse = new JdbcApi().mod(EnvironmentFactory.get(req), modRequestBodyMap);
        
        
        if (modResponse.allModSuccess) {
          modReset(req);
        } else {
          req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modRequests", modRequests);
          req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modResponse", modResponse);
        }
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
    req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modRequests");
    req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modResponse");
  }
}
