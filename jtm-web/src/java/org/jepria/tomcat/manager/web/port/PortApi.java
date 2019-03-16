package org.jepria.tomcat.manager.web.port;

import java.util.ArrayList;
import java.util.List;

import org.jepria.tomcat.manager.core.port.TomcatConfPort;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.port.dto.PortDto;

public class PortApi {
  
  public List<PortDto> list(Environment environment) {
    
    TomcatConfPort tomcatConf = new TomcatConfPort(
        () -> environment.getContextXmlInputStream(), 
        () -> environment.getServerXmlInputStream());
    
    final List<PortDto> ports = new ArrayList<>();
    
    final PortDto ajp13port = getPort(tomcatConf, "AJP/1.3");
    if (ajp13port != null) {
      ports.add(ajp13port);
    }

    final PortDto http11port = getPort(tomcatConf, "HTTP/1.1");
    if (http11port != null) {
      ports.add(http11port);
    }
    
    return ports;
  }
  
  /**
   * @param tomcatConf
   * @param type (protocol)
   * @return
   */
  protected PortDto getPort(TomcatConfPort tomcatConf, String type) {
    String numberStr = tomcatConf.getConnectorPort(type);
    
    if (numberStr == null) {
      return null;
    }
    
    // TODO try parse or validate by regex?
    Integer.parseInt(numberStr);
    
    PortDto port = new PortDto();
    port.put("type", type);
    port.put("number", numberStr);
    
    return port;
  }
  
  
  public PortDto portHttp(Environment environment) {
    final TomcatConfPort tomcatConf = new TomcatConfPort(
        () -> environment.getContextXmlInputStream(), 
        () -> environment.getServerXmlInputStream());
    return getPort(tomcatConf, "HTTP/1.1");
  }
  
  public PortDto portAjp(Environment environment) {
    final TomcatConfPort tomcatConf = new TomcatConfPort(
        () -> environment.getContextXmlInputStream(), 
        () -> environment.getServerXmlInputStream());
    return getPort(tomcatConf, "AJP/1.3");
  }
}
