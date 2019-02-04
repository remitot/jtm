package org.jepria.tomcat.manager.web.modjk;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ModjkApiServlet extends HttpServlet {

  private static final long serialVersionUID = -3831454096594936484L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    resp.setContentType("application/json; charset=UTF-8");
    
    String path = req.getPathInfo();
    
    if ("/list".equals(path)) {
      
      try {
        
        List<Map<String, Object>> connectionDtos = new ArrayList<>();
        Map<String, Object> m = new HashMap<>();
        m.put("pattern", "App1");m.put("worker", "Server1");m.put("active", true);m.put("location", "loc-1");
        connectionDtos.add(m);
        m = new HashMap<>();
        m.put("pattern", "App2");m.put("worker", "Server2");m.put("active", true);m.put("location", "loc-2");
        connectionDtos.add(m);
        m = new HashMap<>();
        m.put("pattern", "App3");m.put("worker", "Server3");m.put("active", false);m.put("location", "loc-3");
        connectionDtos.add(m);
        
        Map<String, Object> responseJsonMap = new HashMap<>();
        responseJsonMap.put("_list", connectionDtos);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(responseJsonMap, new PrintStream(resp.getOutputStream()));
        
      } catch (Throwable e) {
        e.printStackTrace();

        resp.getOutputStream().println("Oops! Something went wrong.");//TODO
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.flushBuffer();
        return;
      }

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.flushBuffer();
    }
  }
  
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String path = req.getPathInfo();
    
    if (!"/mod".equals(path)) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
    
    resp.setContentType("application/json; charset=UTF-8");

  }
}
