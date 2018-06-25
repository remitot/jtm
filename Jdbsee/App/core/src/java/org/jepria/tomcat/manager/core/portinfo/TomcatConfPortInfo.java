package org.jepria.tomcat.manager.core.portinfo;

import java.io.InputStream;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.jepria.tomcat.manager.core.TomcatConfBase;
import org.jepria.tomcat.manager.core.TransactionException;
import org.w3c.dom.Node;

/**
 * Class represents the configuration of Tomcat server for accessing port numbers
 */
public class TomcatConfPortInfo extends TomcatConfBase {
  
  public TomcatConfPortInfo(InputStream contextXmlInputStream, InputStream serverXmlInputStream)
      throws TransactionException {
    super(contextXmlInputStream, serverXmlInputStream);
  }

  /**
   * 
   * @param protocol for example "AJP/1.3" or "HTTP/1.1"
   * @return
   */
  public String getConnectorPort(String protocol) {
    try {
      final XPathExpression connectorExpr = XPathFactory.newInstance().newXPath().compile(
          "Server/Service/Connector[@protocol='" + protocol + "']");
      Node connector = (Node)connectorExpr.evaluate(serverDoc, XPathConstants.NODE);
      
      if (connector == null) {
        return null;
      }
      
      String portNumber = connector.getAttributes().getNamedItem("port").getNodeValue();
      return portNumber;
      
    } catch (Throwable e) {
      handleThrowable(e);
      return null;
    }
  }
}
