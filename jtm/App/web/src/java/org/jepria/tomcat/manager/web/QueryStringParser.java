package org.jepria.tomcat.manager.web;

import java.util.HashMap;
import java.util.Map;

public class QueryStringParser {
  
  public static Map<String, String> parse(String queryString) {
    Map<String, String> ret = new HashMap<>();
    if (queryString != null) {
      String[] kvs = queryString.split("&");
      if (kvs != null) {
        for (String kv: kvs) {
          if (kv != null && !"".equals(kv)) {
            int splitIndex = kv.indexOf('=');
            if (splitIndex == -1) {
              // do not overwrite a normal value
              ret.putIfAbsent(kv, "");
            } else {
              String key = kv.substring(0, splitIndex);
              String value = kv.substring(splitIndex + 1);
              ret.put(key, value);
            }
          }
        }
      }
    }
    return ret;
  }
}
