package org.jepria.tomcat.manager.web.jdbc;

import org.jepria.tomcat.manager.web.jdbc.JdbcTable.JdbcRow;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.CheckBox;
import org.jepria.web.ssr.fields.Fields;
import org.jepria.web.ssr.fields.Table;

import java.util.Arrays;
import java.util.List;

public class JdbcTable extends Table<JdbcRow> {

  public static class JdbcRow extends Table.Row {
    public String id;
    public boolean dataModifiable = true;
    
    public CellField active() {
      return (CellField) get(0);
    }
    public CellField name() {
      return (CellField) get(1);
    }
    public CellField server() {
      return (CellField) get(2);
    }
    public CellField db() {
      return (CellField) get(3);
    }
    public CellField user() {
      return (CellField) get(4);
    }
    public CellField password() {
      return (CellField) get(5);
    }
  }

  public JdbcTable(Context context) {
    super(context, createTableHeader(context));
    addStyle("css/jdbc/jdbc.css");
  }

  protected static List<Table.CellHeader> createTableHeader(Context context) {
    Text text = context.getText();
    return Arrays.asList(
        Cells.header(text.getString("org.jepria.tomcat.manager.web.jdbc.Table.header.column_name"), "name"),
        Cells.header(text.getString("org.jepria.tomcat.manager.web.jdbc.Table.header.column_server"), "server"),
        Cells.header(text.getString("org.jepria.tomcat.manager.web.jdbc.Table.header.column_db"), "db"),
        Cells.header(text.getString("org.jepria.tomcat.manager.web.jdbc.Table.header.column_user"), "user"),
        Cells.header(text.getString("org.jepria.tomcat.manager.web.jdbc.Table.header.column_password"), "password"));
  }
  
  @Override
  protected El createHeader() {

    Text text = context.getText();
    
    El headerEl = new El("div", context);
    headerEl.classList.add("header");

    {
      El cellEl = createCell(headerEl, "column-active");// empty cell
      cellEl.classList.add("column-left");

      cellEl = createCell(headerEl, "column-delete");// empty cell

      cellEl = createCell(headerEl, "column-test");// empty cell
    }
    
    El flexColumns = new El("div", context);
    flexColumns.classList.add("flexColumns");

    for (CellHeader cell: header) {
      String columnClassName;
      {
        String cellName = cell.name;
        if (cellName != null) {
          columnClassName = "column-" + cellName;
        } else {
          columnClassName = "column";
        }
      }

      El cellEl = createCell(flexColumns, columnClassName);
      String cellText = cell.text;
      if (cellText != null) {
        El label = new El("label", cellEl.context);
        label.setInnerHTML(cell.text, true);
        cellEl.appendChild(label);
      }

    }
    
    headerEl.appendChild(flexColumns);
    return headerEl;
  }

  @Override
  public El createRow(JdbcRow row, TabIndex tabIndex) {

    Text text = context.getText();

    final El rowEl = new El("div", context);
    rowEl.classList.add("row");

    {
      El cell = createCell(rowEl, "column-active");
      cell.classList.add("column-left");
      cell.classList.add("cell-field");
      String titleCheckboxActive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.active");
      String titleCheckboxInactive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.inactive");
      CellField fieldActive = row.active();
      CheckBox checkBox = addCheckbox(cell, fieldActive, titleCheckboxActive, titleCheckboxInactive);
      if (row.dataModifiable) {
        tabIndex.setNext(checkBox.input);
      } else {
        addFieldUnmodifiableTitle(checkBox);
      }

      if ("false".equals(fieldActive.value)) {
        rowEl.classList.add("inactive");
      }
    }

    El cellDelete = createCell(rowEl, "column-delete");
    El cellTest = createCell(rowEl, "column-test");


    El flexColumns = new El("div", rowEl.context);
    flexColumns.classList.add("flexColumns");

    {
      El cell = createCell(flexColumns, "column-name");
      cell.classList.add("cell-field");
      fillCell(cell, row.name(), tabIndex);
    }

    {
      El cell = createCell(flexColumns, "column-server");
      cell.classList.add("cell-field");
      El field = fillCell(cell, row.server(), tabIndex);
      if (!row.dataModifiable) {
        addFieldUnmodifiableTitle(field);
      }
    }

    {
      El cell = createCell(flexColumns, "column-db");
      cell.classList.add("cell-field");
      El field = fillCell(cell, row.db(), tabIndex);
      if (!row.dataModifiable) {
        addFieldUnmodifiableTitle(field);
      }
    }

    {
      El cell = createCell(flexColumns, "column-user");
      cell.classList.add("cell-field");
      El field = fillCell(cell, row.user(), tabIndex);
      if (!row.dataModifiable) {
        addFieldUnmodifiableTitle(field);
      }
    }

    {
      El cell = createCell(flexColumns, "column-password");
      cell.classList.add("cell-field");
      El field = fillCell(cell, row.password(), tabIndex);
      if (!row.dataModifiable) {
        addFieldUnmodifiableTitle(field);
      }
    }

    addFieldTest(cellTest, tabIndex, row.name().valueOriginal);

    if (row.dataModifiable) {
      String titleDelete = text.getString("org.jepria.web.ssr.table.buttonDelete.title.delete");
      String titleUndelete = text.getString("org.jepria.web.ssr.table.buttonDelete.title.undelete");
      addFieldDelete(cellDelete, tabIndex, titleDelete, titleUndelete);
    }

    rowEl.appendChild(flexColumns);

    rowEl.setAttribute("item-id", row.id);
    return rowEl;
  }

  protected void addFieldUnmodifiableTitle(El field) {
    field.setAttribute("title", context.getText().getString("org.jepria.tomcat.manager.web.jdbc.field.unmodifiable"));
  }

  @Override
  public El createRowCreated(JdbcRow row, TabIndex tabIndex) {

    Text text = context.getText();

    El rowEl = new El("div", context);
    rowEl.classList.add("row");
    rowEl.classList.add("created");

    {
      El cellEl = createCell(rowEl, "column-active");
      cellEl.classList.add("column-left");
      cellEl.classList.add("cell-field");
      String titleCheckboxActive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.active");
      String titleCheckboxInactive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.inactive");
      CellStaticCheckbox cellActive = Cells.withStaticCheckbox(true, "active");
      addCheckbox(cellEl, cellActive, titleCheckboxActive, titleCheckboxInactive);
    }
    
    El cellDelete = createCell(rowEl, "column-delete");
    El cellTest = createCell(rowEl, "column-test"); // empty cell because testing new connections is unsupported

    El flexColumns = new El("div", rowEl.context);
    flexColumns.classList.add("flexColumns");

    {
      El cellEl = createCell(flexColumns, "column-name");
      cellEl.classList.add("cell-field");
      El field = fillCell(cellEl, row.name(), tabIndex);
      field.setAttribute("placeholder", "jdbc/MyDataSource");
    }

    {
      El cellEl = createCell(flexColumns, "column-server");
      cellEl.classList.add("cell-field");
      El field = fillCell(cellEl, row.server(), tabIndex);
      field.setAttribute("placeholder", "db-server:1521");
    }

    {
      El cellEl = createCell(flexColumns, "column-db");
      cellEl.classList.add("cell-field");
      El field = fillCell(cellEl, row.db(), tabIndex);
      field.setAttribute("placeholder", "MYDATABASE");
    }

    {
      El cellEl = createCell(flexColumns, "column-user");
      cellEl.classList.add("cell-field");
      El field = fillCell(cellEl, row.user(), tabIndex);
      field.setAttribute("placeholder", "me");
    }

    {
      El cellEl = createCell(flexColumns, "column-password");
      cellEl.classList.add("cell-field");
      El field = fillCell(cellEl, row.password(), tabIndex);
      field.setAttribute("placeholder", "mysecret");
    }


    String titleDelete = text.getString("org.jepria.web.ssr.table.buttonDelete.title.delete");
    String titleUndelete = text.getString("org.jepria.web.ssr.table.buttonDelete.title.undelete");
    addFieldDelete(cellDelete, tabIndex, titleDelete, titleUndelete);


    rowEl.appendChild(flexColumns);

    rowEl.setAttribute("item-id", row.id);
    return rowEl;
  }

  /**
   *
   * @param cell
   * @param tabIndex
   * @param connectionName if null, no link
   */
  protected El addFieldTest(El cell, TabIndex tabIndex, String connectionName) {
    El field = new El("div", context);

    {// active link with image
      El a = new El("a", context);
      a.setAttribute("target", "_blank");
      a.addClass("button-test_active");

      String href = context.getAppContextPath() + "/oracle/" + connectionName +
          "?query-input=select * from dual";// TODO sample query may depend on connection type
      a.setAttribute("href", href);

      El img = new El("img", context);
      img.classList.add("img-button");
      img.setAttribute("src", context.getAppContextPath() + "/img/jdbc/test.png");
      String title = context.getText().getString("org.jepria.tomcat.manager.web.jdbc.Table.buttonTest.title");
      a.setAttribute("title", title);
      a.appendChild(img);
      if (tabIndex != null) {
        tabIndex.setNext(a);
      }

      field.appendChild(a);
    }

    {// inactive (grayed out) image
      El img = new El("img", context);
      img.classList.add("img-button");
      img.addClass("button-test_inactive");

      img.setAttribute("src", context.getAppContextPath() + "/img/jdbc/test.png");
      String title = context.getText().getString("org.jepria.tomcat.manager.web.jdbc.Table.buttonTest_inactive.title");
      img.setAttribute("title", title);

      field.appendChild(img);
    }

    El wrapper = Fields.wrapCellPad(field);
    cell.appendChild(wrapper);

    addStrike(cell);

    return field;
  }
}
