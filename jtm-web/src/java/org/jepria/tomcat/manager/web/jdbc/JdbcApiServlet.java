package org.jepria.tomcat.manager.web.jdbc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.jdbc.JdbcApi.ModResponse;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ModRequestBodyDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ModRequestDto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JdbcApiServlet extends HttpServlet {

  private static final long serialVersionUID = -7724868882541481749L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    String path = req.getPathInfo();
    
    if ("/list".equals(path)) {
      
      try {
        final List<ConnectionDto> connections = new JdbcApi().list(EnvironmentFactory.get(req));

        
        Map<String, Object> responseJsonMap = new HashMap<>();
        responseJsonMap.put("_list", connections);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String responseBody = gson.toJson(responseJsonMap);
        
        
        // write response
        if (responseBody != null) {
          resp.setContentType("application/json; charset=UTF-8");
          resp.getWriter().print(responseBody);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
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
    
    if ("/mod".equals(path)) {
      
      try {

        // read list from request body
        final List<ModRequestDto> modRequests;
        
        try {
          Type type = new TypeToken<ArrayList<ModRequestDto>>(){}.getType();
          modRequests = new Gson().fromJson(new InputStreamReader(req.getInputStream()), type);
          
        } catch (Throwable e) {
          e.printStackTrace();

          resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
          resp.flushBuffer();
          return;
        } 
        
        
        // convert list to map
        final Map<String, ModRequestBodyDto> modRequestBodyMap = new HashMap<>();
        
        if (modRequests != null) {
          for (ModRequestDto modRequest: modRequests) {
            final String modRequestId = modRequest.getModRequestId();
            
            // validate modRequestId fields
            if (modRequestId == null || "".equals(modRequestId)) {
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                  "Found missing or empty modRequestId fields");
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
      
        
        
        // prepare response map
        final Map<String, Object> responseJsonMap = new HashMap<>();
        
        // convert map to list of JSON objects
        List<Map<String, Object>> modStatusList = new ArrayList<>();
        for (Map.Entry<String, ModStatus> entry: modResponse.modStatusMap.entrySet()) {
          Map<String, Object> jsonMap = new HashMap<>();
          jsonMap.put("modRequestId", entry.getKey());
          int code = entry.getValue().code;
          jsonMap.put("modStatusCode", code);
          if (code == ModStatus.SC_INVALID_FIELD_DATA) {
            jsonMap.put("invalidFieldData", entry.getValue().invalidFieldDataMap);
          }
          modStatusList.add(jsonMap);
        }
        
        responseJsonMap.put("modStatusList", modStatusList);
        
        
        if (modResponse.allModSuccess) {
          responseJsonMap.put("_list", new JdbcApi().list(EnvironmentFactory.get(req)));
        }
        
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String responseBody = gson.toJson(responseJsonMap);
        
        
        // write response
        if (responseBody != null) {
          resp.setContentType("application/json; charset=UTF-8");
          resp.getWriter().print(responseBody);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
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
}
