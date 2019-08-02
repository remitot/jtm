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
import org.jepria.tomcat.manager.web.JtmPageHeader;
import org.jepria.tomcat.manager.web.JtmPageHeader.CurrentMenuItem;
import org.jepria.tomcat.manager.web.jdbc.JdbcApi.ItemModStatus;
import org.jepria.tomcat.manager.web.jdbc.JdbcApi.ItemModStatus.Code;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.web.HttpDataEncoding;
import org.jepria.web.data.ItemModRequestDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.StatusBar;
import org.jepria.web.ssr.Text;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Servlet producing server-side-rendered pages
 */
public class JdbcSsrServlet extends SsrServletBase {

  private static final long serialVersionUID = -2556094883694667549L;

  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final Context context = Context.get(req, "text/org_jepria_tomcat_manager_web_Text");
    Text text = context.getText();
    
    final AppState appState = getAppState(req);

    final Environment env = EnvironmentFactory.get(req);
    
    final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(context);
    pageBuilder.setTitle(text.getString("org.jepria.tomcat.manager.web.jdbc.title"));
    
    String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    
    final PageHeader pageHeader = new JtmPageHeader(context, managerApacheHref, CurrentMenuItem.JDBC);
    pageBuilder.setHeader(pageHeader);
    
    
    if (checkAuth(req)) {
      pageHeader.setButtonLogout(req);
      
      final List<ConnectionDto> connections = new JdbcApi().list(env);
      
      // retrieve modRequest and modStatus from the AppState
      @SuppressWarnings("unchecked")
      List<ItemModRequestDto> itemModRequests = (List<ItemModRequestDto>)appState.modRequest;
      @SuppressWarnings("unchecked")
      Map<String, ItemModStatus> itemModStatuses = (Map<String, ItemModStatus>)appState.modStatus;
      if (itemModRequests == null) {
        @SuppressWarnings("unchecked")
        List<ItemModRequestDto> itemModRequestsAuthPers = (List<ItemModRequestDto>)getAuthPersistentData(req); 
        itemModRequests = itemModRequestsAuthPers;
      }
      
      
      JdbcPageContent content = new JdbcPageContent(context, connections, itemModRequests, itemModStatuses);
      pageBuilder.setContent(content);
      
      
      if (itemModStatuses != null) {
        boolean hasInvalidFieldData = itemModStatuses.values().stream()
            .anyMatch(modStatus -> modStatus.code == Code.INVALID_FIELD_DATA);
        
        StatusBar statusBar = createModStatusBar(context, !hasInvalidFieldData);
        pageBuilder.setStatusBar(statusBar);
      }
      
      // clear auth-persistent data
      setAuthPersistentData(req, null);
      
    } else {

      requireAuth(req, pageBuilder);
    }
    
    final HtmlPageExtBuilder.Page page = pageBuilder.build();
    page.respond(resp);
    
    clearAppState(req);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final String path = req.getPathInfo();
    
    if ("/mod".equals(path)) {

      final Gson gson = new Gson();
      final List<ItemModRequestDto> itemModRequests;
        
      // read list from request parameter (as passed by form.submit)
      try {
        final String data = HttpDataEncoding.getParameterUtf8(req, "data");
        
        Type type = new TypeToken<List<ItemModRequestDto>>(){}.getType();
        itemModRequests = gson.fromJson(data, type);
      } catch (Throwable e) {
        // TODO
        throw new RuntimeException(e);
      }

      if (itemModRequests != null && itemModRequests.size() > 0) {
        
        if (checkAuth(req)) {
          
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
            // save modifications
            
            // Note: it is safe to save modifications to context.xml file here (before servlet response), 
            // because although Tomcat reloads the context after context.xml modification, 
            // it still fulfills the servlet requests currently under processing. 
            tomcatConf.save(env.getContextXmlOutputStream(), 
                env.getServerXmlOutputStream());
            
            // clear modRequest after the successful modification
            AppState appState = getAppState(req);
            appState.modRequest = null;
            appState.modStatus = itemModStatuses;
            
          } else {
           
            // save session attributes
            AppState appState = getAppState(req);
            appState.modRequest = itemModRequests;
            appState.modStatus = itemModStatuses;
          }
          
          // clear auth-persistent data
          setAuthPersistentData(req, null);
          
        } else {

          final AppState appState = getAppState(req);
          appState.modRequest = itemModRequests;
          appState.modStatus = null;
          
          
          setAuthPersistentData(req, itemModRequests);
        }
        
      }
      
      resp.sendRedirect(req.getContextPath() + "/jdbc");
      return;
      
    } else {
      // unknown request
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not understand the request");
      return;
    }
  }

  /**
   * Creates a StatusBar for a modification status
   * @param context
   * @param success
   * @return
   */
  protected StatusBar createModStatusBar(Context context, boolean success) {
    
    if (success) {
      
      Text text = context.getText();
      return new StatusBar(context, StatusBar.Type.SUCCESS, text.getString("org.jepria.tomcat.manager.web.jdbc.status.mod_success"));
      
    } else {
      
      Text text = context.getText();
      
      final String statusHTML = text.getString("org.jepria.tomcat.manager.web.jdbc.status.mod_incorrect_field_data") 
          + " <span class=\"span-bold\">" + text.getString("org.jepria.tomcat.manager.web.jdbc.status.no_mod_performed") 
          + "</span>";
      return new StatusBar(context, StatusBar.Type.ERROR, statusHTML);
    }
  }
}
