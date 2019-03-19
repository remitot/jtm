package org.jepria.web.ssr;

import java.util.Objects;

import org.jepria.web.ssr.table.Collection;

public class ForbiddenFragment extends AuthFragment {
  
  /**
   * @param logoutActionUrl html {@code action} value of the {@code form} to submit on logout button click, non null
   */
  public ForbiddenFragment(String logoutActionUrl, String userPrincipalName) {
    
    Objects.requireNonNull(logoutActionUrl);
    
    final El form = new El("form").addClass("auth-form")
        .setAttribute("action", logoutActionUrl)
        .setAttribute("method", "post");

    
    final El rowLabel = new El("div").addClass("auth-form__row");
    final String innerHTML = "Пользователь<br/><span class=\"span-bold\">" + userPrincipalName + "</span><br/>не имеет нужных прав"; // NON-NLS
    final El label = new El("label").setInnerHTML(innerHTML); // NON-NLS
    rowLabel.appendChild(label);
    form.appendChild(rowLabel);
    
    
    final El rowButtonLogout = new El("div").addClass("auth-form__row");
    final El buttonLogout = new El("button").setAttribute("type", "submit").addClass("big-black-button")
        .setInnerHTML("ВЫЙТИ"); // NON-NLS
    rowButtonLogout.appendChild(buttonLogout);
    form.appendChild(rowButtonLogout);
    
    appendChild(form);
  }
  
  @Override
  protected void addScripts(Collection scripts) {
    super.addScripts(scripts);
    scripts.add("js/jtm-common.js");
  }
}
