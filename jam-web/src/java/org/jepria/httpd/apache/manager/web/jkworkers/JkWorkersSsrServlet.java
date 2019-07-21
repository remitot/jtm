package org.jepria.httpd.apache.manager.web.jkworkers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.core.jk.ApacheConfJk;
import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.httpd.apache.manager.web.JamPageHeader;
import org.jepria.httpd.apache.manager.web.JamPageHeader.CurrentMenuItem;
import org.jepria.httpd.apache.manager.web.jk.JkApi;
import org.jepria.httpd.apache.manager.web.jk.JkTextPageContent;
import org.jepria.web.data.ItemModRequestDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.Text;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JkWorkersSsrServlet extends SsrServletBase {

  private static final long serialVersionUID = 451407023338158961L;

  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final Context context = Context.get(req, "text/org_jepria_httpd_apache_manager_web_Text");
    Text text = context.getText();
    
    final AppState appState = getAppState(req);

    final Environment env = EnvironmentFactory.get(req);
    
    final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(context);
    pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.jk.title"));
    
    
    final JamPageHeader pageHeader = new JamPageHeader(context, CurrentMenuItem.JK_WORKERS); 
    pageBuilder.setHeader(pageHeader);

    
    
    if (checkAuth(req)) {
      pageHeader.setButtonLogout(req);

      
      List<String> workersPropertiesLines = new JkApi().getWorkers_propertiesLines(env);
      
      // retrieve modRequests from the AppState
      List<ItemModRequestDto> itemModRequests = appState.itemModRequests;
      if (itemModRequests == null) {
        @SuppressWarnings("unchecked")
        List<ItemModRequestDto> itemModRequestsAuthPers = (List<ItemModRequestDto>)getAuthPersistentData(req); 
        itemModRequests = itemModRequestsAuthPers;
      }
      
      final List<String> itemModRequestLines = getLinesFromModRequests(itemModRequests);
      
      JkTextPageContent content = new JkTextPageContent(context, workersPropertiesLines, itemModRequestLines, CurrentMenuItem.JK_WORKERS);
      pageBuilder.setContent(content);
      pageBuilder.setBodyAttributes("onload", "common_onload();textContent_onload();");

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
        
        if (checkAuth(req)) {
          
          final Environment env = EnvironmentFactory.get(req);
          
          final ApacheConfJk apacheConf = new ApacheConfJk(
              () -> env.getMod_jk_confInputStream(), 
              () -> env.getWorkers_propertiesInputStream());
          
          final JkApi api = new JkApi();
          
          final List<String> lines = getLinesFromModRequests(itemModRequests);
          
          if (lines != null) {
            api.updateWorkers_properties(lines, apacheConf);
            
            apacheConf.save(() -> env.getMod_jk_confOutputStream(), 
                () -> env.getWorkers_propertiesOutputStream());
          }
          
          // clear modRequests after the successful modification
          final AppState appState = getAppState(req);
          appState.itemModRequests = null;
          
        } else {

          final AppState appState = getAppState(req);
          appState.itemModRequests = itemModRequests;
          
          
          setAuthPersistentData(req, itemModRequests);
        }
      }
      
      resp.sendRedirect(req.getContextPath() + "/jk/workers");
      return;
      
    } else {
      // unknown request
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not understand the request");
      return;
    }
  }
  
  protected List<String> getLinesFromModRequests(List<ItemModRequestDto> itemModRequests) {
    if (itemModRequests == null) {
      return null;
    }
    
    List<String> lines = null;
    
    for (ItemModRequestDto itemModRequest: itemModRequests) {
      if ("update".equals(itemModRequest.getAction())
          && "text-content".equals(itemModRequest.getId())) {
        
        Map<String, String> data = itemModRequest.getData();
        String text = data.get("text");
        if (text != null) {
          lines = Arrays.asList(text.split("\\R"));
        }
        
        break;
      }
    }
    
    return lines;
  }
}
