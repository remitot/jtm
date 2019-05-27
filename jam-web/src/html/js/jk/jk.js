function jk_onload() {
  var table = document.getElementsByClassName("table-details")[0];
  
  // TODO now we simply overwrite the script that has been added in table.js
  addRowDeleteScript_details(table);
}

function addRowDeleteScript_details(composite) {
  var buttonsDelete = composite.querySelectorAll(".row input.button-delete");
  for (var i = 0; i < buttonsDelete.length; i++) {
    buttonsDelete[i].onclick = function(event){onDeleteButtonClick_details(event.target)};
  }
}

function onDeleteButtonClick_details(button) {
  var rows = document.querySelectorAll(".table-details .row");
  for (var i = 0; i < rows.length; i++) {
  
    var row = rows[i];
    
    if (!row.classList.contains("deleted")) {
      row.classList.add("deleted");
  
      setDisabled(row, true);
      
    } else {
      row.classList.remove("deleted");
      
      setDisabled(row, false);
    }
    
    checkModifications();
  }
}