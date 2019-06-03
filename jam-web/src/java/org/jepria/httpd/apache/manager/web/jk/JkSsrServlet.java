package org.jepria.httpd.apache.manager.web.jk;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.httpd.apache.manager.web.JamPageHeader;
import org.jepria.httpd.apache.manager.web.JamPageHeader.CurrentMenuItem;
import org.jepria.httpd.apache.manager.web.jk.JkApi.ModStatus;
import org.jepria.httpd.apache.manager.web.jk.JkApi.ModStatus.Code;
import org.jepria.httpd.apache.manager.web.jk.JkApi.ModStatus.InvalidFieldDataCode;
import org.jepria.httpd.apache.manager.web.jk.dto.BindingDto;
import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.web.data.ItemModRequestDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.Field;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JkSsrServlet extends SsrServletBase {

  private static final long serialVersionUID = -5587074686993550317L;

  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    Context context = Context.get(req, "text/org_jepria_httpd_apache_manager_web_Text");
    Text text = context.getText();

    final AppState appState = getAppState(req);

    final Environment env = EnvironmentFactory.get(req);

    final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(context);

    
    // what view to show on the page
    
    final String mountId;
    // whether to show details view for an existing binding (by mountId)
    final boolean details;
    // whether to show list view
    final boolean list;
    // whether to show details view for a newly created binding
    final boolean newBinding;
    {
      final String path = req.getPathInfo();
      if (path == null || "/".equals(path) || "".equals(path)) {
        mountId = null;
        details = newBinding = false;
        list = true;
      } else {
        if ("/new-binding".equals(path)) {
          mountId = null;
          details = list = false;
          newBinding = true;
        } else {
          mountId = path.substring("/".length());
          list = newBinding = false;
          details = true;
        }
      }
    }
      


    // page header and title
    
    final JamPageHeader pageHeader;
    {
      if (details) {
        pageHeader = new JamPageHeader(context, CurrentMenuItem.JK_DETAILS);
        pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.jk.title") + "&nbsp;&mdash;&nbsp;"
            + text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJkDetails.default"));
      } else if (newBinding) {
        pageHeader = new JamPageHeader(context, CurrentMenuItem.JK_NEW_BINDING);
        pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.jk.title") + "&nbsp;&mdash;&nbsp;"
            + text.getString("org.jepria.httpd.apache.manager.web.PageHeader.itemJkNewBinding"));
      } else {
        pageHeader = new JamPageHeader(context, CurrentMenuItem.JK);
        pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.jk.title"));
      }
    }
    pageBuilder.setHeader(pageHeader);

    
    
    if (checkAuth(req)) {

      pageHeader.setButtonLogout(req);

      if (details) {
        // show details view for for an existing binding (by mountId)


        BindingDto binding = new JkApi().getBinding(env, mountId);
        // TODO process binding == null here (not found or already removed)


        // retrieve modRequests and modStatuses from the AppState ant auth-persistent data
        List<ItemModRequestDto> itemModRequests = appState.itemModRequests;
        @SuppressWarnings("unchecked")
        Map<String, ModStatus> itemModStatuses = (Map<String, ModStatus>)appState.itemModStatuses;
        if (itemModRequests == null) {
          @SuppressWarnings("unchecked")
          List<ItemModRequestDto> itemModRequestsAuthPers = (List<ItemModRequestDto>)getAuthPersistentData(req); 
          itemModRequests = itemModRequestsAuthPers;
        }


        
        // details header text and page title (from application) 
        {
          if (binding != null && binding.jkMount != null) {
            String application = binding.jkMount.map.get("application");
            final int maxBindingTitleLength = 24;
            if (application != null && application.length() > maxBindingTitleLength) { 
              application = application.substring(0, maxBindingTitleLength) + "...";
            }
            pageHeader.setCurrentDetailsMenuItemText(application);
            pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.jk.title") + "&nbsp;&mdash;&nbsp;" + application);
          }
        }
        
        
          
        // create records
        
        List<BindingDetailsTable.Record> records = new ArrayList<>();
        {
          if (binding.jkMount != null) {
            {
              BindingDetailsTable.Record record = new BindingDetailsTable.Record("active");
              record.field().value = record.field().valueOriginal = binding.jkMount.map.get("active");
              records.add(record);
            }
            {
              BindingDetailsTable.Record record = new BindingDetailsTable.Record("application");
              record.field().value = record.field().valueOriginal = binding.jkMount.map.get("application");
              records.add(record);
            }
          }
          
          if (binding.worker != null) {
            {
              BindingDetailsTable.Record record = new BindingDetailsTable.Record("workerName");
              record.field().value = binding.worker.map.get("name");
              
              // TODO the user may want to change the worker name or worker-name-to-port binding, 
              // or to have multiple same-host-and-port workers (e.g. for different applications). Consider this! 
              record.field().readonly = true; // for now, the worker name is read-only; modification only through host:port binding
              records.add(record);
            }
            {
              BindingDetailsTable.Record record = new BindingDetailsTable.Record("host");
              record.field().value = record.field().valueOriginal = binding.worker.map.get("host");
              records.add(record);
            }
            if ("ajp13".equalsIgnoreCase(binding.worker.map.get("type"))) {
              {
                BindingDetailsTable.Record record = new BindingDetailsTable.Record("ajpPort");
                record.field().value = record.field().valueOriginal = binding.worker.map.get("port");
                records.add(record);
              }
            } else {
              // TODO what if another port type?
            }
          }
          
          
          
          // http port
          
          if (binding.httpPort != null || binding.httpErrorCode != null) {
            BindingDetailsTable.Record recordHttpPort;
            {
              recordHttpPort = new BindingDetailsTable.Record("httpPort");
              records.add(recordHttpPort);
            }
            
            if (binding.httpPort != null) {
              recordHttpPort.field().value = recordHttpPort.field().valueOriginal = binding.httpPort;
            }
            if (binding.httpErrorCode != null && binding.httpErrorCode == 1) {
              recordHttpPort.field().value = recordHttpPort.field().valueOriginal = ""; // empty string instead of null to avoid initial modified field state
              recordHttpPort.setHint("Failed to get HTTP port number for the Tomcat instance, see logs for details");// TODO NON-NLS
            }
          }
          

          // http link
          
          if (binding.httpLink != null) {
            BindingDetailsTable.Record record = new BindingDetailsTable.Record("link");
            record.field().value = binding.httpLink;
            record.field().readonly = true;
            records.add(record);
          }
        }

        overlayFields(records, itemModRequests);

        processInvalidFieldData(records, itemModStatuses);
        
        // page content
        List<El> content = new ArrayList<>();
        {
          BindingDetailsTable table = new BindingDetailsTable(context);
          table.load(records, null, null);
          content.add(table);
          
          // control buttons
          final BindingDetailsControlButtons controlButtons = new BindingDetailsControlButtons(context);
          controlButtons.addButtonSave(context.getContextPath() + "/jk/" + mountId + "/mod");// TODO such url will erase any path- or request params of the current page
          controlButtons.addButtonDelete(context.getContextPath() + "/jk/" + mountId + "/del");// TODO such url will erase any path- or request params of the current page
          content.add(controlButtons);
        }
        
        
        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();checkbox_onload();controlButtons_onload();");

      } else if (newBinding) {
        // show details view for a newly created binding


        // retrieve modRequests and modStatuses from the AppState ant auth-persistent data
        List<ItemModRequestDto> itemModRequests = appState.itemModRequests;
        @SuppressWarnings("unchecked")
        Map<String, ModStatus> itemModStatuses = (Map<String, ModStatus>)appState.itemModStatuses;
        if (itemModRequests == null) {
          @SuppressWarnings("unchecked")
          List<ItemModRequestDto> itemModRequestsAuthPers = (List<ItemModRequestDto>)getAuthPersistentData(req); 
          itemModRequests = itemModRequestsAuthPers;
        }


        // create records
        
        List<BindingDetailsTable.Record> records = new ArrayList<>();
        {
          {
            BindingDetailsTable.Record record = new BindingDetailsTable.Record("active");
            record.field().value = record.field().valueOriginal = null;
            record.field().readonly = true;
            records.add(record);
          }
          {
            BindingDetailsTable.Record record = new BindingDetailsTable.Record("application");
            record.field().value = record.field().valueOriginal = null;
            records.add(record);
          }
          {
            BindingDetailsTable.Record record = new BindingDetailsTable.Record("workerName");
            record.field().value = "lookup existing"; // TODO NON-NLS
            record.field().readonly = true;
            records.add(record);
          }
          {
            BindingDetailsTable.Record record = new BindingDetailsTable.Record("host");
            record.field().value = record.field().valueOriginal = null;
            records.add(record);
          }
          {
            BindingDetailsTable.Record record = new BindingDetailsTable.Record("ajpPort");
            record.field().value = record.field().valueOriginal = null;
            records.add(record);
          }
          {
            BindingDetailsTable.Record record = new BindingDetailsTable.Record("httpPort");
            record.field().value = record.field().valueOriginal = null;
            records.add(record);
          }
        }       

        overlayFields(records, itemModRequests);

        processInvalidFieldData(records, itemModStatuses);
        
        // page content
        List<El> content = new ArrayList<>();
        {
          BindingDetailsTable table = new BindingDetailsTable(context);
          table.load(records, null, null);
          content.add(table);
          
          final BindingDetailsControlButtons controlButtons = new BindingDetailsControlButtons(context);
          controlButtons.addButtonSave(context.getContextPath() + "/jk/new-binding/mod");// TODO such url will erase any path- or request params of the current page
          content.add(controlButtons);
        }

        
        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();checkbox_onload();controlButtons_onload();");

      } else if (list) {
        // show list view

        final List<JkMountDto> jkMounts = new JkApi().getJkMounts(env);

        JkMountTablePageContent content = new JkMountTablePageContent(context, jkMounts);
        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();");
      }

      // clear auth-persistent data
      setAuthPersistentData(req, null);
      
    } else {

      requireAuth(req, pageBuilder);
    }

    HtmlPageExtBuilder.Page page = pageBuilder.build();
    page.respond(resp);

    clearAppState(req);
  }
  
  protected void processInvalidFieldData(Iterable<BindingDetailsTable.Record> records, Map<String, ModStatus> modStatuses) {
    if (modStatuses != null) {
      for (Map.Entry<String, ModStatus> modRequestIdAndModStatus: modStatuses.entrySet()) {
        String modRequestId = modRequestIdAndModStatus.getKey();
        ModStatus modStatus = modRequestIdAndModStatus.getValue();

        if (modStatus.code == Code.INVALID_FIELD_DATA && modStatus.invalidFieldDataMap != null) {
          for (BindingDetailsTable.Record record: records) {
            if (modRequestId.equals(record.getId())) {
              ModStatus.InvalidFieldDataCode invalidFieldDataCode = modStatus.invalidFieldDataMap.get("field");
              if (invalidFieldDataCode != null) {
                Field field = record.field();
                field.invalid = true;
                switch (invalidFieldDataCode) {
                case MANDATORY_EMPTY: {
                  field.invalidMessage = "manda is empty"; // TODO NON-NLS
                  break;
                }
                case DUPLICATE_APPLICATION: {
                  field.invalidMessage = "duplicate application"; // TODO NON-NLS
                  break;
                }
                case BOTH_HTTP_AJP_PORT_EMPTY: {
                  field.invalidMessage = "either http or ajp must be filled"; // TODO NON-NLS
                  break;
                }
                case BOTH_HTTP_AJP_PORT: {
                  field.invalidMessage = "both http and ajp"; // TODO NON-NLS
                  break;
                }
                case HTTP_PORT_REQUEST_FAILED: {
                  field.invalidMessage = "Could not get AJP port over HTTP"; // TODO NON-NLS
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

  protected void overlayFields(List<BindingDetailsTable.Record> records, List<ItemModRequestDto> itemModRequests) {
    if (records != null && itemModRequests != null) {

      // create map from list
      Map<String, BindingDetailsTable.Record> recordMap = new HashMap<>();
      {
        for (BindingDetailsTable.Record record: records) {
          recordMap.put(record.getId(), record);
        }
      }

      for (ItemModRequestDto itemModRequest: itemModRequests) {
        String modRequestId = itemModRequest.getId();
        Map<String, String> modRequestData = itemModRequest.getData();
        if (modRequestId != null && modRequestData != null) {
          String modValue = modRequestData.get("field");
          if (modValue != null) {
            BindingDetailsTable.Record record = recordMap.get(modRequestId);
            if (record != null) {
              record.field().value = modValue;
            }
          }
        }
      }
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    boolean unknownRequest = false;

    final String path = req.getPathInfo();

    if (path == null) {
      unknownRequest = true;

    } else {
      final String[] split = path.split("(?=/)");

      if (split.length == 2) {

        if ("/mod".equals(split[1])) {

          final String mountId = split[0].substring("/".length());

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

              final JkApi api = new JkApi();

              Map<String, String> fields = new HashMap<>();
              {
                for (ItemModRequestDto itemModRequest: itemModRequests) {
                  fields.put(itemModRequest.getId(), itemModRequest.getData().get("field"));
                }
              }

              ModStatus modStatus;
              if ("new-binding".equals(mountId)) {
                modStatus = api.createBinding(env, fields);
              } else {
                modStatus = api.updateBinding(env, mountId, fields);
              }

              Map<String, ModStatus> modStatuses = null;
              if (modStatus.code == Code.INVALID_FIELD_DATA && modStatus.invalidFieldDataMap != null) {
                modStatuses = new HashMap<>();
                for (Map.Entry<String, InvalidFieldDataCode> e: modStatus.invalidFieldDataMap.entrySet()) {
                  Map<String, InvalidFieldDataCode> map = new HashMap<>();
                  map.put("field", e.getValue());
                  modStatuses.put(e.getKey(), ModStatus.errInvalidFieldData(map));
                }
              }

              if (modStatus.code == Code.SUCCESS) {

                // clear modRequests after the successful modification (but preserve modStatuses)
                final AppState appState = getAppState(req);
                appState.itemModRequests = null;
                appState.itemModStatuses = modStatuses;

              } else {

                // save session attributes
                AppState appState = getAppState(req);
                appState.itemModRequests = itemModRequests;
                appState.itemModStatuses = modStatuses;
              }

              // clear auth-persistent data
              setAuthPersistentData(req, null);

            } else {

              final AppState appState = getAppState(req);
              appState.itemModRequests = itemModRequests;
              appState.itemModStatuses = null;


              setAuthPersistentData(req, itemModRequests);
            }

          }

          resp.sendRedirect(req.getContextPath() + "/jk/" + mountId);
          return;

        } else if ("/del".equals(split[1])) {

          final String mountId = split[0];
          // TODO delete binding by mountId

        } else {

          unknownRequest = true;
        }

      } else {
        unknownRequest = true;
      }
    }

    if (unknownRequest) {
      // unknown request
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not understand the request");
      return;
    }
  }
}
