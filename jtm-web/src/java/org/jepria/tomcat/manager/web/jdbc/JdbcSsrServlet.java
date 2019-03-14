package org.jepria.tomcat.manager.web.jdbc;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.core.jdbc.TomcatConfJdbc;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.HtmlPage;
import org.jepria.tomcat.manager.web.SsrServletBase;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ItemModRequestDto;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;
import org.jepria.web.ssr.StatusBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Servlet producing server-side-rendered pages
 */
public class JdbcSsrServlet extends SsrServletBase {

  private static final long serialVersionUID = -2556094883694667549L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final ApplicationalState applicationalState = getApplicationalState(req);
    
    if (checkAuth(req, resp)) {
      
      final Environment env = EnvironmentFactory.get(req);
      
      final HtmlPage htmlPage;
      
      final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
      final PageHeader pageHeader = new PageHeader(managerApacheHref, "jdbc/logout", CurrentMenuItem.JDBC);

      final List<ConnectionDto> connections = new JdbcApi().list(env);
      
      
      htmlPage = new JdbcHtmlPage(pageHeader, connections, applicationalState.itemModRequests, applicationalState.itemModStatuses);
      htmlPage.setStatusBar(createStatusBar(applicationalState.pageStatus));
  
      htmlPage.setTitle("Tomcat manager: датасорсы (JDBC)"); // NON-NLS
      htmlPage.respond(resp);

      
      applicationalState.itemModRequests = null;
      applicationalState.itemModStatuses = null;
      
    } else {
      
      if (applicationalState.clearModStateOnUnauthorizedGet) {
        applicationalState.itemModRequests = null;
      }
      
      doLogin(req, resp);
      
      applicationalState.clearModStateOnUnauthorizedGet = true;
    }
    
    applicationalState.pageStatus = null;
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final String path = req.getPathInfo();
    
    if ("/login".equals(path)) {
      
      if (!login(req, resp)) {
        getApplicationalState(req).clearModStateOnUnauthorizedGet = false;
      }
      
      // jdbc/login -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
      
    } else if ("/logout".equals(path)) {
      
      logout(req, resp);
      
      // jdbc/logout -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
        
    } else if ("/mod".equals(path)) {

      final Gson gson = new Gson();
      final List<ItemModRequestDto> itemModRequests;
        
      // read list from request parameter (as passed by form.submit)
      try {
        String data = req.getParameter("data");
        
        // convert encoding TODO fix this using accept-charset form attribute?
        data = new String(data.getBytes("ISO-8859-1"), "UTF-8");
        
        Type type = new TypeToken<List<ItemModRequestDto>>(){}.getType();
        itemModRequests = gson.fromJson(data, type);
      } catch (Throwable e) {
        // TODO
        throw new RuntimeException(e);
      }

      if (itemModRequests != null && itemModRequests.size() > 0) {
        
        if (checkAuth(req, resp)) {
          
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
              if (itemModStatus.code != ItemModStatus.Code.SUCCESS) {
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
              if (itemModStatus.code != ItemModStatus.Code.SUCCESS) {
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
              if (itemModStatus.code != ItemModStatus.Code.SUCCESS) {
                modSuccess = false;
              }
              itemModStatuses.put(itemModRequest.getId(), itemModStatus);
            }
          }
    
    
          // 4) ignore illegal actions
    
          if (modSuccess) {
            // save modifications and add a new _list to the response
            
            // Note: it is safe to save modifications to context.xml file here (before servlet response), 
            // because although Tomcat reloads the context after context.xml modification, 
            // it still fulfills the servlet requests currently under processing. 
            tomcatConf.save(env.getContextXmlOutputStream(), 
                env.getServerXmlOutputStream());
            
            // reset the servlet mod status after the successful mod
            final ApplicationalState applicationalState = getApplicationalState(req);
            applicationalState.itemModRequests = null;
            applicationalState.itemModStatuses = null;
            applicationalState.pageStatus = Status.MOD_SUCCESS;
            
          } else {
           
            // save session attributes
            ApplicationalState applicationalState = getApplicationalState(req);
            applicationalState.itemModRequests = itemModRequests;
            applicationalState.itemModStatuses = itemModStatuses;
            applicationalState.pageStatus = Status.MOD_INCORRECT_FIELD_DATA;
          }
        
        } else {

          setLoginStatus(req, LoginStatus.MOD_SESSION_EXPIRED);
          
          final ApplicationalState applicationalState = getApplicationalState(req);
          applicationalState.itemModRequests = itemModRequests;
          applicationalState.itemModStatuses = null;
        }
        
      }
      
      // jdbc/mod -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
      
    } else if ("/mod-reset".equals(path)) {
      
      
      // TODO no need to checkAuth?
      final ApplicationalState applicationalState = getApplicationalState(req);
      applicationalState.itemModRequests = null;
      applicationalState.itemModStatuses = null;
      applicationalState.pageStatus = null;

      
      // jdbc/mod-reset -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
    }
  }

  protected class ApplicationalState {
    public Status pageStatus = null;
    public List<ItemModRequestDto> itemModRequests = null;
    public Map<String, ItemModStatus> itemModStatuses = null;
    public boolean clearModStateOnUnauthorizedGet = false;
  }
  
  protected ApplicationalState getApplicationalState(HttpServletRequest request) {
    ApplicationalState applicationalState = (ApplicationalState)request.getSession().getAttribute(
        "org.jepria.tomcat.manager.web.jdbc.SessionAttributes.applicationalState");
    if (applicationalState == null) {
      applicationalState = new ApplicationalState();
      request.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.applicationalState", 
          applicationalState);
    }
    return applicationalState;
  }
  
  protected enum Status {
    MOD_SUCCESS,
    MOD_INCORRECT_FIELD_DATA,
  }
  
  protected StatusBar createStatusBar(Status status) {
    if (status == null) {
      return null;
    }
    switch (status) {
    case MOD_SUCCESS: {
      return new StatusBar(StatusBar.Type.SUCCESS, "<span class=\"span-bold\">Все изменения сохранены.</span>"); // NON-NLS 
    }
    case MOD_INCORRECT_FIELD_DATA: {
      final String statusHTML = "При попытке сохранить изменения обнаружились некорректные значения полей (выделены красным). " +
          "<span class=\"span-bold\">На сервере всё осталось без изменений.</span>"; // NON-NLS
      return new StatusBar(StatusBar.Type.ERROR, statusHTML);
    }
    }
    throw new IllegalArgumentException(String.valueOf(status));
  }
}
