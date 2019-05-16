package org.jepria.web.ssr;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ForbiddenFragment extends AuthFragment {
  
  /**
   * @param logoutRedirectPath path to redirect after a successful logout (on logout form submit button click). 
   * If {@code null}, no redirect will be performed
   */
  public ForbiddenFragment(Text text, String logoutRedirectPath, String userPrincipalName) {
    super(text);

    final String action;
    {
      StringBuilder sb = new StringBuilder();
      sb.append("logout");
      if (logoutRedirectPath != null) {
        sb.append("?redirect=");
        try {
          sb.append(URLEncoder.encode(logoutRedirectPath, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          // impossible
          throw new RuntimeException(e);
        }
      }
      action = sb.toString();
    }
    
    final El form = new El("form").addClass("auth-form")
        .setAttribute("action", action)
        .setAttribute("method", "post");

    
    final El rowLabel = new El("div").addClass("auth-form__row");
    final String innerHTML = text.getString("org.jepria.web.ssr.ForbiddenFragment.text.user")
        + "<br/><span class=\"span-bold\">" + userPrincipalName + "</span><br/>" 
        + text.getString("org.jepria.web.ssr.ForbiddenFragment.text.no_rights");
    
    final El label = new El("label").setInnerHTML(innerHTML);
    rowLabel.appendChild(label);
    form.appendChild(rowLabel);
    
    
    final El rowButtonLogout = new El("div").addClass("auth-form__row");
    final El buttonLogout = new El("button").setAttribute("type", "submit").addClass("big-black-button")
        .setInnerHTML(text.getString("org.jepria.web.ssr.common.buttonLogout.text"));
    rowButtonLogout.appendChild(buttonLogout);
    form.appendChild(rowButtonLogout);
    
    appendChild(form);
    
    addScript("js/common.js");
    addStyle("css/common.css");
  }
}
