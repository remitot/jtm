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

import org.jepria.httpd.apache.manager.core.jk.ApacheConfJk;
import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.httpd.apache.manager.web.JamPageHeader;
import org.jepria.httpd.apache.manager.web.JamPageHeader.CurrentMenuItem;
import org.jepria.httpd.apache.manager.web.jk.AjpAdapter.AjpException;
import org.jepria.httpd.apache.manager.web.jk.JkApi.ModStatus;
import org.jepria.httpd.apache.manager.web.jk.JkApi.ModStatus.Code;
import org.jepria.httpd.apache.manager.web.jk.JkApi.ModStatus.InvalidFieldDataCode;
import org.jepria.httpd.apache.manager.web.jk.dto.BindingDto;
import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.web.data.ItemModRequestDto;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.PageHeader;
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
    pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.jk.title"));


    final String mountId;
    final boolean details;
    final boolean list;
    final boolean newBinding;


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



    final PageHeader pageHeader;
    if (details) {
      pageHeader = new JamPageHeader(context, CurrentMenuItem.JK_DETAILS);
    } else if (newBinding) {
      pageHeader = new JamPageHeader(context, CurrentMenuItem.JK_NEW_BINDING);
    } else {
      pageHeader = new JamPageHeader(context, CurrentMenuItem.JK);
    }
    pageBuilder.setHeader(pageHeader);

    if (checkAuth(req)) {

      pageHeader.setButtonLogout(req);

      
      List<ItemModRequestDto> itemModRequests = appState.itemModRequests;
      @SuppressWarnings("unchecked")
      Map<String, ModStatus> itemModStatuses = (Map<String, ModStatus>)appState.itemModStatuses;
      
      
      if (details) {
        // show details for JkMount by id

        BindingDto binding = new JkApi().getBinding(env, mountId);
        
        // TODO process binding == null here (not found or already removed)

        Map<String, String> fields = new HashMap<>();
        { // convert dtos to fields
          if (binding.jkMount != null) {
            fields.put("active", binding.jkMount.map.get("active"));
            fields.put("application", binding.jkMount.map.get("application"));
          }
          if (binding.worker != null) {
            fields.put("workerName", binding.worker.map.get("name"));
            fields.put("host", binding.worker.map.get("host"));
            if ("ajp13".equalsIgnoreCase(binding.worker.map.get("type"))) {
              fields.put("ajpPort", binding.worker.map.get("port"));
            }
          }
        }
        
        // TODO stopped here: merge modFields into records
        List<BindingDetailsTable.Record> records = new ArrayList<>();
        {
          DetailsRecordCreator c = new DetailsRecordCreator();
          records.add(c.createRecordActive(fields.get("active")));
          final String application = fields.get("application");
          records.add(c.createRecordApplication(application));
          records.add(c.createRecordWorkerName(fields.get("workerName")));
          final String host = fields.get("host");
          records.add(c.createRecordHost(host));
          final String ajpPort = fields.get("ajpPort");
          records.add(c.createRecordAjpPort(ajpPort));

          if (host != null && ajpPort != null) {
            // request http port over ajp
            Integer ajpPortInt = Integer.parseInt(ajpPort);
            String tomcatManagerExtCtxPath = lookupTomcatManagerPath(env, host, ajpPortInt);
            int httpPort;
            try {
              httpPort = AjpAdapter.requestHttpPortOverAjp(host, ajpPortInt, tomcatManagerExtCtxPath);
              records.add(c.createRecordHttpPort(String.valueOf(httpPort)));

              if (application != null) {
                StringBuilder link = new StringBuilder();
                link.append("http://").append(host);
                if (httpPort != 80) {
                  link.append(':').append(httpPort);
                }
                link.append("/").append(application);
                records.add(c.createRecordLink(link.toString()));
              }

            } catch (AjpException e) {
              e.printStackTrace();

              BindingDetailsTable.Record record = c.createRecordHttpPort("");
              record.setHint("Failed to get HTTP port number for the Tomcat instance, see logs for details");// TODO NON-NLS
              records.add(record);
            }
          }
        }
        
        overlayFields(records, itemModRequests);
        
        // process invalid field data
        if (itemModStatuses != null) {
          for (Map.Entry<String, ModStatus> modRequestIdAndModStatus: itemModStatuses.entrySet()) {
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
                    case BOTH_HTTP_AJP_PORT: {
                      field.invalidMessage = "both http and ajp"; // TODO NON-NLS
                      break;
                    }
                    }
                  }
                }
              }
            }
          }
        }
        
        BindingDetailsPageContent content = new BindingDetailsPageContent(context, records, mountId);

        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();checkbox_onload();controlButtons_onload();");

      } else if (newBinding) {
        // show details for a newly created binding

        List<BindingDetailsTable.Record> records = new ArrayList<>();
        {
          DetailsRecordCreator c = new DetailsRecordCreator();

          BindingDetailsTable.Record active = c.createRecordActive(null);
          active.field().readonly = true;
          records.add(active);
          
          records.add(c.createRecordApplication(null));
          records.add(c.createRecordWorkerName(null));
          records.add(c.createRecordHost(null));
          records.add(c.createRecordAjpPort(null));
          records.add(c.createRecordHttpPort(null));
        }       
        
        overlayFields(records, itemModRequests);

        BindingDetailsPageContent content = new BindingDetailsPageContent(context, records);

        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();checkbox_onload();controlButtons_onload();");

      } else if (list) {
        // show table

        final List<JkMountDto> jkMounts = new JkApi().getJkMounts(env);

        JkMountTablePageContent content = new JkMountTablePageContent(context, jkMounts);
        pageBuilder.setContent(content);
        pageBuilder.setBodyAttributes("onload", "common_onload();table_onload();");
      }
      
    } else {

      requireAuth(req, pageBuilder);
    }

    HtmlPageExtBuilder.Page page = pageBuilder.build();
    page.respond(resp);
    
    clearAppState(req);
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

  protected final class DetailsRecordCreator {
    public BindingDetailsTable.Record createRecordActive(String value) {
      BindingDetailsTable.Record record = new BindingDetailsTable.Record("Active", null); // TODO NON-NLS
      record.field().value = record.field().valueOriginal = value;
      record.setId("active");
      return record;
    }

    public BindingDetailsTable.Record createRecordApplication(String value) {
      BindingDetailsTable.Record record = new BindingDetailsTable.Record("Application", null); // TODO NON-NLS
      record.field().value = record.field().valueOriginal = value;
      record.setId("application");
      return record;
    }

    public BindingDetailsTable.Record createRecordWorkerName(String value) {
      BindingDetailsTable.Record record = new BindingDetailsTable.Record("Worker", "worker1"); // TODO NON-NLS
      record.field().value = record.field().valueOriginal = value;
      record.setId("workerName");
      return record;
    }

    public BindingDetailsTable.Record createRecordHost(String value) {
      BindingDetailsTable.Record record = new BindingDetailsTable.Record("Host", "server.com"); // TODO NON-NLS NON-NLS
      record.field().value = record.field().valueOriginal = value;
      record.setId("host");
      return record;
    }

    public BindingDetailsTable.Record createRecordAjpPort(String value) {
      BindingDetailsTable.Record record = new BindingDetailsTable.Record("AJP port", "8009"); // TODO NON-NLS NON-NLS
      record.field().value = record.field().valueOriginal = value;
      record.setId("ajpPort");
      return record;
    }

    public BindingDetailsTable.Record createRecordHttpPort(String value) {
      BindingDetailsTable.Record record = new BindingDetailsTable.Record("HTTP port", "8080"); // TODO NON-NLS NON-NLS
      record.field().value = record.field().valueOriginal = value;
      record.setId("httpPort");
      return record;
    }

    public BindingDetailsTable.Record createRecordLink(String value) {
      BindingDetailsTable.Record record = new BindingDetailsTable.Record("Link", null); // TODO NON-NLS
      record.field().value = value;
      record.field().readonly = true;
      record.setId("link");
      return record;
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    boolean unknownRequest = false;

    final String path = req.getPathInfo();

    if (path == null) {
      unknownRequest = true;

    } else if ("/new-binding".equals(path)) {

      // TODO create new binding

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

              final ApacheConfJk conf = new ApacheConfJk(
                  () -> env.getMod_jk_confInputStream(), 
                  () -> env.getWorkers_propertiesInputStream());

              final JkApi api = new JkApi();

              Map<String, String> fields = new HashMap<>();
              {
                for (ItemModRequestDto itemModRequest: itemModRequests) {
                  fields.put(itemModRequest.getId(), itemModRequest.getData().get("field"));
                }
              }

              ModStatus modStatus = api.updateBinding(mountId, fields, conf);

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
                // save modifications and add a new _list to the response

                conf.save(env.getMod_jk_confOutputStream(), 
                    env.getWorkers_propertiesOutputStream());

                // reset the servlet mod status after the successful mod
                final AppState appState = getAppState(req);
                appState.itemModRequests = null;
                appState.itemModStatuses = modStatuses;

              } else {

                // save session attributes
                AppState appState = getAppState(req);
                appState.itemModRequests = itemModRequests;
                appState.itemModStatuses = modStatuses;
              }

            } else {
              // TODO
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

  protected String lookupTomcatManagerPath(Environment environment, String host, int port) {
    String tomcatManagerPath = environment.getProperty("org.jepria.httpd.apache.manager.web.TomcatManager." + host + "." + port + ".path");
    if (tomcatManagerPath == null) {
      tomcatManagerPath = environment.getProperty("org.jepria.httpd.apache.manager.web.TomcatManager.default.path");
      if (tomcatManagerPath == null) {
        throw new RuntimeException("Misconfiguration exception: "
            + "mandatory configuration property \"org.jepria.httpd.apache.manager.web.TomcatManager.default.path\" is not defined");
      }
    }
    return tomcatManagerPath;
  }
}
