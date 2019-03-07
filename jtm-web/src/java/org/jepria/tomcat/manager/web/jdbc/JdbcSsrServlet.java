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

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    final String path = req.getPathInfo();
    
    if (path == null || "".equals(path)) {
      
      try {
        // table html
        final List<ConnectionDto> connections = new JdbcApi().list(EnvironmentFactory.get(req));
        
        // forward compatibility
        final List<JdbcItem> items = connections.stream().map(conn -> {
          JdbcItem item = new JdbcItem();
          for (String name: conn.keySet()) {
            Field field = item.get(name);
            if (field != null) {
              field.value = field.valueOriginal = conn.get(name);
            }
          }
          item.setId(conn.get("id"));
          item.dataModifiable = conn.getDataModifiable();
          return item;
        }).collect(Collectors.toList());
        
        
        final JdbcTable table = new JdbcTable();
        
        final List<JdbcItem> itemsCreated = new ArrayList<>();
        final Map<String, JdbcItem> itemsModified = new HashMap<>();
        final Set<String> itemsDeleted = new HashSet<>();
//        final List<ModRequestDto> modRequests = (List<ModRequestDto>)req.getSession().getAttribute(
//            "org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modRequests");
//        if (modRequests != null) {
//          for (ModRequestDto modRequest: modRequests) {
//            final ModRequestBodyDto modRequestBody = modRequest.getModRequestBody(); 
//            final String action = modRequestBody.getAction();
//            if ("create".equals(action)) {
//              itemsCreated.add(modRequestBody.getData());
//            } else if ("update".equals(action)) {
//              itemsModified.put(modRequestBody.getId(), modRequestBody.getData());
//            } else if ("delete".equals(action)) {
//              itemsDeleted.add(modRequestBody.getId());
//            }
//          }
//        }
//        
//        final ModResponse modResponse = (ModResponse)req.getSession().getAttribute(
//            "org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modResponse");
//        if (modResponse != null) {
//          for (Map.Entry<String, ModStatus> e: modResponse.modStatusMap.entrySet()) {
//            ModStatus modStatus = e.getValue(); 
//            if (modStatus.code == ModStatus.SC_INVALID_FIELD_DATA) {
//              // TODO stopped here: mark fields in table invalid
////              modStatus.invalidFieldDataMap
//            }
//          }
//        }
        
        table.load(items, itemsCreated, itemsModified, itemsDeleted);
        
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
    
    if ((path == null || "".equals(path)) && req.getParameter("mod") != null) {
      
      try {

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
            
            // validate modRequestId fields
            if (modRequestId == null || "".equals(modRequestId)) {
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                  "Some modRequestId fields are missing or empty");
              resp.flushBuffer();
              return;
              
            } else if (modRequestBodyMap.put(modRequestId, modRequest.getModRequestBody()) != null) {
              
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                  "Duplicate modRequestId field values found: [" + modRequestId + "]");
              resp.flushBuffer();
              return;
            }
          }
        }
        
        
        ModResponse modResponse = new JdbcApi().mod(EnvironmentFactory.get(req), modRequestBodyMap);
        
        
        if (modResponse.allModSuccess) {
          req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modRequests");
        } else {
          req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modRequests", modRequests);
          req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.modResponse", modResponse);
        }
        
        resp.sendRedirect("jdbc");
        return;
        
      } catch (Throwable e) {
        // TODO
        throw new RuntimeException(e);
      }
      
    } else {
   // TODO
      throw new RuntimeException();
    }
  }
  
}
