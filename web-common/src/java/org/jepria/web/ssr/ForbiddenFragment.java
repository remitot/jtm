package org.jepria.web.ssr;

public class ForbiddenFragment extends AuthFragment {
  
  /**
   * @param logoutRedirectPath path to redirect after a successful logout (on logout form submit button click). 
   * If {@code null}, no redirect will be performed
   */
  public ForbiddenFragment(Context context, String logoutRedirectPath, String userPrincipalName) {
    super(context);

    final String action = "logout" + (logoutRedirectPath != null ? ("?redirect=" + logoutRedirectPath) : "");
    
    final El form = new El("form", context).addClass("auth-form")
        .setAttribute("action", action)
        .setAttribute("method", "post");

    
    final El rowLabel = new El("div", context).addClass("auth-form__row");
    final String innerHTML = context.getText("org.jepria.web.ssr.ForbiddenFragment.text.user")
        + "<br/><span class=\"span-bold\">" + userPrincipalName + "</span><br/>" 
        + context.getText("org.jepria.web.ssr.ForbiddenFragment.text.no_rights");
    
    final El label = new El("label", context).setInnerHTML(innerHTML);
    rowLabel.appendChild(label);
    form.appendChild(rowLabel);
    
    
    final El rowButtonLogout = new El("div", context).addClass("auth-form__row");
    final El buttonLogout = new El("button", context).setAttribute("type", "submit").addClass("big-black-button")
        .setInnerHTML(context.getText("org.jepria.web.ssr.common.buttonLogout.text"));
    rowButtonLogout.appendChild(buttonLogout);
    form.appendChild(rowButtonLogout);
    
    appendChild(form);
    
    addScript("js/jtm-common.js");
  }
}
