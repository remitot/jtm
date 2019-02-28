package org.jepria.tomcat.manager.web.jdbc;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.jepria.tomcat.manager.core.jdbc.Connection;
import org.jepria.tomcat.manager.core.jdbc.TomcatConfJdbc;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;

public class JdbcApi {
  
  private static boolean isCreateContextResources(Environment environment) {
    return "true".equals(environment.getProperty("org.jepria.tomcat.manager.web.jdbc.createContextResources"));
  }
  
  public List<ConnectionDto> list(HttpServletRequest request) {
    
    final Environment environment = EnvironmentFactory.get(request);
    
    final TomcatConfJdbc tomcatConf = new TomcatConfJdbc(
        () -> environment.getContextXmlInputStream(), 
        () -> environment.getServerXmlInputStream(),
        isCreateContextResources(environment));
    
    return getConnections(tomcatConf);
  }
  
  private static List<ConnectionDto> getConnections(TomcatConfJdbc tomcatConf) {
    Map<String, Connection> connections = tomcatConf.getConnections();

    // list all connections
    return connections.entrySet().stream().map(
        entry -> connectionToDto(entry.getKey(), entry.getValue()))
        .sorted(connectionSorter()).collect(Collectors.toList());
  }
  
  private static Comparator<ConnectionDto> connectionSorter() {
    return new Comparator<ConnectionDto>() {
      @Override
      public int compare(ConnectionDto o1, ConnectionDto o2) {
        int nameCmp = o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        if (nameCmp == 0) {
          // the active is the first
          if (o1.getActive() && !o2.getActive()) {
            return -1;
          } else if (o2.getActive() && !o1.getActive()) {
            return 1;
          } else {
            return 0;
          }
        } else {
          return nameCmp;
        }
      }
    };
  }

  private static ConnectionDto connectionToDto(String id, Connection connection) {
    Objects.requireNonNull(id);
    
    ConnectionDto dto = new ConnectionDto();
    
    dto.setDataModifiable(connection.isDataModifiable());
    dto.setActive(connection.isActive());
    dto.setDb(connection.getDb());
    dto.setId(id);
    dto.setName(connection.getName());
    dto.setPassword(connection.getPassword());
    dto.setServer(connection.getServer());
    dto.setUser(connection.getUser());
    
    return dto;
  }
}
