package org.jepria.tomcat.manager.core.jdbc;

import java.util.Map;

/**
 * Initial connection params to apply no the newly created connections, in the endpoint files
 */
public interface ConnectionInitialParams {
  /**
   * @return initial attribute values for the Context/Resource nodes
   */
  Map<String, String> contextResourceNodeAttributeValues();
  
  /**
   * @return initial attribute values for the Context/ResourceLink nodes
   */
  Map<String, String> contextResourceLinkNodeAttributeValues();
  
  /**
   * @return initial attribute values for the Server/Resource nodes
   */
  Map<String, String> serverResourceNodeAttributeValues();
}
