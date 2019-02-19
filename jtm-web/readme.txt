Application configuration:

The out-of-the-box (non-customized) application uses the default configuration properties
stored in App/conf-default folder. All those properties may be overidden (custom configuration).

Configuration using JNDI entries (in Tomcat container):

Properties contained in App/conf-default/application.properties may be overridden
individually using Tomcat JNDI entries, for example:
<Context>
  ...
  <Environment name="org.jepria.tomcat.manager.web.jdbc.protocol" value="jdbc:mysql://"
      type="java.lang.String" override="false"/>
  ...
</Context>

Properties contained in App/conf-default/jdbc.initial/* files may only be overriden
by an entire file, not individually. To do so, define the custom application conf directory
in a JNDI entry, under the "org.jepria.tomcat.manager.web.appConfDirectory" name:
<Context>
  ...
  <Environment name="org.jepria.tomcat.manager.web.appConfDirectory" 
      value="/path/to/local/conf"
      type="java.lang.String" override="false"/>
  ...
</Context>
Then, place the files with overridden properties under that directory,
maintaining the original file and directory names and structure:
path
|
+--to
    |
    +--local
       |
       +--conf
          |
          +--jdbc.initial
          |  |
          |  +--ContextResourceLink.properties
          |
          +--application.properties

/path/to/local/conf/jdbc.initial/ContextResourceLink.properties contents:
# overridden property:
closeMethod=myAwesomeMethod
# non-overridden (default) property:
type=javax.sql.DataSource

/path/to/local/conf/jdbc.initial/application.properties contents:
# overridden property, whose value however is shadowed by the Context/Environment override:
org.jepria.tomcat.manager.web.jdbc.protocol=my:awesome:protocol://
# overridden property:
org.jepria.tomcat.manager.web.jdbc.createContextResources=true
# non-overridden (default) property:
org.jepria.tomcat.manager.web.managerApacheHref=/manager-apache

Properties from application.properties file may too be overridden by the entire file.
However, individually overridden properties prevail over those overridden by the entire file.   
