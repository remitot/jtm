package org.jepria.tomcat.manager.core.jdbc;

import java.util.Map;

/**
 * Initial connection params to apply to the newly created connections, in the endpoint files
 */
public interface ConnectionInitialParams {
  /**
   * @return default attribute values for the Context/Resource nodes
   */
  Map<String, String> contextResourceDefaultAttrs();
  
  /**
   * @return default attribute values for the Context/ResourceLink nodes
   */
  Map<String, String> contextResourceLinkDefaultAttrs();
  
  /**
   * @return default attribute values for the Server/Resource nodes
   */
  Map<String, String> serverResourceDefaultAttrs();
}
