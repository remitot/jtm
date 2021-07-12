package org.jepria.tomcat.manager.web.oracle;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleApiServlet extends HttpServlet  {
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String path = req.getPathInfo();

    Matcher m;
    
    if (path != null && (m = Pattern.compile("/lob/clob/(?<downloadId>.+)").matcher(path)).matches()) {
      // download clob
      String downloadId = m.group("downloadId");
      String sessionAttrKey = OracleThinClientSsrServlet.SESSION_ATTR_KEY__CLOB_PREFIX + downloadId;

      Reader r = (Reader) req.getSession().getAttribute(sessionAttrKey);
      req.getSession().removeAttribute(sessionAttrKey);
      
      if (r == null) {
        resp.setStatus(404);
        resp.getWriter().println("The object does not exist or the stream has been already closed or expired");
        resp.flushBuffer();
        return;
        
      } else {

        resp.setContentType("text/plain; charset=UTF-8");
        int contentLength = 0;
        
        try (Reader reader = r) {
          // write reader to the response
          char[] buf = new char[2048];
          int length;
          while ((length = reader.read(buf)) > 0) {
            contentLength += length;
            resp.getWriter().write(buf, 0, length);
          }
        }

        resp.setStatus(200);
        resp.setHeader("Content-disposition", "inline");
        resp.setContentLength(contentLength);
        resp.flushBuffer();
        return;
        
      }

    } else if (path != null && (m = Pattern.compile("/lob/blob/(?<downloadId>.+)").matcher(path)).matches()) {
      // download blob
      String downloadId = m.group("downloadId");
      String sessionAttrKey = OracleThinClientSsrServlet.SESSION_ATTR_KEY__BLOB_PREFIX + downloadId;

      InputStream inputStream = (InputStream) req.getSession().getAttribute(sessionAttrKey);
      req.getSession().removeAttribute(sessionAttrKey);
      
      if (inputStream == null) {
        resp.setStatus(404);
        resp.getWriter().println("The object does not exist or the stream has been already closed or expired");
        resp.flushBuffer();
        return;
        
      } else {

        resp.setContentType("application/octet-stream");
        int contentLength = 0;
        
        try (InputStream in = inputStream) {
          // write reader to the response
          byte[] buf = new byte[2048];
          int length;
          while ((length = in.read(buf)) > 0) {
            contentLength += length;
            resp.getOutputStream().write(buf, 0, length);
          }
        }

        resp.setStatus(200);
        resp.setHeader("Content-disposition", "attachment");
        resp.setContentLength(contentLength);
        resp.flushBuffer();
        return;
      }

    } else {
      // unknown request
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported request path [" + path + "]");
      resp.flushBuffer();
      return;
    }
  }
}
