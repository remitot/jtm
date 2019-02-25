package org.jepria.tomcat.suspender.core;

/**
 * Checks the uri against alive and suspended apps (whether or not some application must be unsuspended)
 */
// TODO rename to UnsuspendChecker or smth like that
public class SuspendProcessor {
  
  public static enum Result {
    /**
     * Alive app found, suspended app not found
     */
    FOUND_ALIVE,
    /**
     * Alive app not found, suspended app found
     */
    FOUND_SUSPENDED,
    /**
     * Found both alive and unsuspended, prior to the alive because of the stricter (longer) match
     */
    FOUND_BOTH_PRIOR_ALIVE,
    /**
     * Found both alive and unsuspended, prior to the suspended because of the stricter (longer) match
     */
    FOUND_BOTH_PRIOR_SUSPENDED,
    /**
     * Neither alive not suspended app found
     */
    NOT_FOUND
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
   * @param uri
   */
  public SuspendProcessor(String uri) {

    if (uri == null) {
      // TODO what it may mean?
      throw new IllegalStateException("uri is null");
    }
    
    final String appAlive = mapToAppAlive(uri);
    final String appSus = mapToAppSuspended(uri);

    // priority the appContext
    if (appSus != null) {
      
      if (appAlive == null) {
        // no unsuspended app found, but found suspended app
        
        this.result = Result.FOUND_SUSPENDED;
        this.app = appSus;
        
      } else if (appSus.length() > appAlive.length()) {
        // found both apps: suspended and alive, 
        // prefer suspended because of the stricter (longer) match
          
        this.result = Result.FOUND_BOTH_PRIOR_SUSPENDED;
        this.app = appSus;
        
      } else {
        // found both apps: suspended and alive,
        // prefer alive because of the stricter (longer) match

        this.result = Result.FOUND_BOTH_PRIOR_ALIVE;
        this.app = appAlive;
      }
      
    } else {
      if (appAlive != null) {
        // no suspended app found, but found alive app
        
        this.result = Result.FOUND_ALIVE;
        this.app = appAlive;
        
      } else {
        // neither suspended nor alive app found
        
        this.result = Result.NOT_FOUND;
        this.app = null;
      }
    }
  }
  
  protected String mapToAppAlive(String uri) {
    return AppMapperFactory.get().mapToAppAlive(uri);
  }
  
  protected String mapToAppSuspended(String uri) {
    return AppMapperFactory.get().mapToAppSuspended(uri);
  }
}
