# Application configuration

The out-of-the-box (non-customized) application uses the default configuration properties
stored in `App/app-conf-default.properties` file (`WEB-INF/app-conf-default.properties` in the war).

Each configuration property may be overridden individually using [Tomcat JNDI entries](https://tomcat.apache.org/tomcat-7.0-doc/config/context.html#Environment_Entries) or using a single custom application configuration file. 
* To override a property using Tomcat JNDI entries, create a `Context/Environment` entry of a type `String`, with name of the property to override, and the overridden value. Example:
```
<Context>
  ...
  <Environment name="org.jepria.tomcat.manager.web.jdbc.protocol" value="jdbc:mysql://"
      type="java.lang.String" override="false"/>
  ...
</Context>
```
* To override a property (or multiple properties) using a single custom application configuration file, create the file at any local place, define that place in a JNDI entry with the `"org.jepria.tomcat.manager.web.conf.file"` name, then override the properties you need in that file.
Note that the properties overridden in `Context/Environment` have higher priority than the same properties overridden in a custom configuration file.
Example:
```
<Context>
  ...
  <Environment name="org.jepria.tomcat.manager.web.conf.file" 
      value="/path/to/local/conf/app-conf.properties"
      type="java.lang.String" override="false"/>
  ...
</Context>
```
Contents of the `/path/to/local/conf/app-conf.properties` file:
```
# This property is overridden
org.jepria.tomcat.manager.web.jdbc.createContextResources=true

# This property is overridden, but shadowed by the Context/Environment overriding (see above)
org.jepria.tomcat.manager.web.jdbc.protocol=jdbc:postgresql://

# This property is not overridden, so the internal default value is used
# org.jepria.tomcat.manager.web.jdbc.initial_attrs.ServerResource.initialSize=50
```