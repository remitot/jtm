package org.jepria.tomcat.manager.core.port;

import java.io.InputStream;
import java.util.function.Supplier;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jepria.tomcat.manager.core.TomcatConfBase;
import org.w3c.dom.Node;

/**
 * Class represents the configuration of Tomcat server for accessing port numbers
 */
public class TomcatConfPort extends TomcatConfBase {
  
  
  public TomcatConfPort(Supplier<InputStream> context_xmlInput,
      Supplier<InputStream> server_xmlInput) {
    super(context_xmlInput, server_xmlInput);
  }
  

  /**
   * 
   * @param protocol for example "AJP/1.3" or "HTTP/1.1"
   * @return
   */
  public String getConnectorPort(String protocol) {
    Node connector;
    try {
      final XPathExpression connectorExpr = XPathFactory.newInstance().newXPath().compile(
          "Server/Service/Connector[@protocol='" + protocol + "']");
      connector = (Node)connectorExpr.evaluate(getServer_xmlDoc(), XPathConstants.NODE);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
    
    if (connector == null) {
      return null;
    }
    
    String port = connector.getAttributes().getNamedItem("port").getNodeValue();
    return port;
  }
}
