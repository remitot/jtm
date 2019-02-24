package org.jepria.tomcat.suspender;

/**
 * Processes the uri against suspending: whether or not some application must be unsuspended
 */
// TODO rename to UnsuspendChecker or smth like that
public class SuspendProcessor {
  
  public static enum Result {
    FOUND,
    NOT_FOUND,
    FOUND_SUSPENDED
  }
  
  private final Result result;
  private final String app;
  
  public Result getResult() {
    return result;
  }

  public String getApp() {
    return app;
  }

  /**
   * 
   * @param uri
   * @param handler
   * @return the application name to unsuspend (and the process normally), or {@code null} if either 
   * (neither unsuspended (alive, deployed), nor suspended app found)
   * or (unsuspended (alive, deployed) app found) 
   */
  public SuspendProcessor(String uri) {

    if (uri == null) {
      // TODO what it may mean?
      throw new IllegalStateException("uri is null");
    }
    
    final String appUnsus = mapToAppUnsuspended(uri);
    final String appSus = mapToAppSuspended(uri);

    // priority the appContext
    if (appSus != null) {
      
      if (appUnsus == null
        // no unsuspended app found, but found suspended app
          
          || appSus.length() > appUnsus.length()
            // found both apps: suspended and unsuspended, 
            // prefer suspended because of the stricter (longer) match
          
          ) {
        
        // unsuspend the suspended app
        this.result = Result.FOUND_SUSPENDED;
        this.app = appSus;
        
      } else {
        // found both apps: suspended and unsuspended,
        // prefer unsuspended because of the stricter (longer) match

        this.result = Result.FOUND;
        this.app = appUnsus;
      }
      
    } else {
      if (appUnsus != null) {
        // no suspended app found, but found unsuspended app
        
        this.result = Result.FOUND;
        this.app = appUnsus;
        
      } else {
        // neither suspended nor unsuspended app found
        
        this.result = Result.NOT_FOUND;
        this.app = null;
      }
    }
  }
  
  protected String mapToAppUnsuspended(String uri) {
    return AppMapperFactory.get().mapToAppUnsuspended(uri);
  }
  
  protected String mapToAppSuspended(String uri) {
    return AppMapperFactory.get().mapToAppSuspended(uri);
  }
}
