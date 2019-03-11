package org.jepria.tomcat.manager.web.jdbc;

import java.util.List;
import java.util.Map;

import org.jepria.tomcat.manager.web.jdbc.dto.ItemModRequestDto;

/**
 * Class representing a servlet status of the entire modification request
 */
/*package*/class ServletModStatus {
  public boolean success;
  public List<ItemModRequestDto> itemModRequests;
  public Map<String, ItemModStatus> itemModStatuses;
}
