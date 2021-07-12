package org.jepria.web.ssr.fields;


import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlEscaper;
import org.jepria.web.ssr.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Table<R extends Table.Row> extends El {

  /**
   * Basic class for all types of cell content 
   */
  public static class Cell {
    /**
     * The cell will have {@code column-<name>} CSS class
     * and in case of editable cell its {@code input} will have such {@code name} attribute value
     * @return
     */
    public String name;
  }
  
  /**
   * Non-editable checkbox (boolean) cell.
   */
  public static class CellStaticCheckbox extends Cell {
    public boolean value;
  }
  
  /**
   * Non-editable cell, containing arbitrary html.
   */
  public static class CellStatic extends Cell {
    public Node content;
  }

  /**
   * Header cell
   */
  public static class CellHeader extends Cell {
    public String text;
  }

  /**
   * Editable cell, operating string values only.
   * Able to modify data and mark value invalid 
   */
  public static class CellField extends Cell {
    public String value;
    public String valueOriginal;
    public boolean invalid;
    public String invalidMessage;
  }

  /**
   * Helper methods for creating various cells
   */
  public static class Cells {
    private Cells() {}

    public static CellStatic withStaticValue(String value, String name) {
      return withNode(Node.fromHtml(HtmlEscaper.escape(value, true)), name);
    }

    public static CellStaticCheckbox withStaticCheckbox(boolean value, String name) {
      CellStaticCheckbox cell = new CellStaticCheckbox();
      cell.name = name;
      cell.value = value;
      return cell;
    }

    /**
     * 
     * @param value "true" or not "true"
     * @param name
     * @return
     */
    public static CellStaticCheckbox withStaticCheckbox(String value, String name) {
      return withStaticCheckbox("true".equals(value), name);
    }

    public static CellStatic withNode(Node node, String name) {
      CellStatic cell = new CellStatic();
      cell.name = name;
      cell.content = node;
      return cell;
    }

    public static CellHeader header(String text, String name) {
      CellHeader cell = new CellHeader();
      cell.name = name;
      cell.text = text;
      return cell;
    }
  }

  public static class Row extends ArrayList<Cell> {}
  
  
  private int tabIndexValue;
  
  public interface TabIndex {
    /**
     * Assigns the next {@code tabindex} to the element
     * @param el
     */
    void setNext(El el);
  }
  
  protected final List<CellHeader> header;
  protected El headerEl;

  /**
   * 
   * @param context
   * @param header null for no header
   */
  public Table(Context context, List<CellHeader> header) {
    super("div", context);
    
    this.header = header;
    this.headerEl = createHeader();
    
    addClass("table");
    
    addScript(new Script("js/table.js", "table_onload", "table__controlButtons_onload"));
    addStyle("css/common.css");
    addStyle("css/table.css");
  }
  
  /**
   * Clear the table and load new data.
   * 
   * The table supports the following kinds of cells: 
   * - static (display-only, non-editable) cells 
   *    represented by {@link CellStatic} objects
   * - regular fields (whose values are currently original, non-modified) 
   *    represended by {@link CellField} objects with {@link CellField#value} equals to {@link CellField#valueOriginal}
   * - fields modified (whose values are currently modified)
   *    represended by {@link CellField} objects with {@link CellField#value} not equals to {@link CellField#valueOriginal}
   * 
   * @param rows rows of cells to fill the table with. Contains all of static cells, modified and non-modified fields.
   * @param rowsCreated optional rows to render as {@code .created}, may be null
   * @param rowsDeleted optional rows from {@code items} to render as {@code .deleted}, may be null. Matching by equals method, so both references and duplicated objects will match 
   */
  public void load(List<R> rows, List<R> rowsCreated, Set<R> rowsDeleted) {
    
    // reset
    childs.clear();
    tabIndexValue = 1;
    
    
    final TabIndex tabIndex = new TabIndex() {
      @Override
      public void setNext(El el) {
        el.setAttribute("tabindex", tabIndexValue++);
      }
    };
    
    
    if (headerEl != null) {
      appendChild(headerEl);
    }
    
    if (rows != null) {

      final boolean isEditable =
          rows.stream().anyMatch(row ->
              row.stream().anyMatch(cell -> cell instanceof CellField));
      
      boolean evenOddGray = true; // for unmodifiable table
      
      for (R row: rows) {
        
        final El rowEl = createRow(row, tabIndex);
        
        if (!isEditable) {
          if (evenOddGray) {
            rowEl.addClass("even-odd-gray");
          }
          evenOddGray = !evenOddGray;
        }
        
        if (rowsDeleted != null && rowsDeleted.contains(row)) {
          rowEl.classList.add("deleted");
        } else {
          // check any field modified
          if (row.stream().anyMatch(cell -> {
            if (cell instanceof CellField) {
              CellField cellField = (CellField) cell;
              return !Objects.equals(cellField.value, cellField.valueOriginal);
            }
            return false;
          })) {
            rowEl.classList.add("modified");
          }
        }
        
        appendChild(rowEl);
      }
    }
    if (rowsCreated != null) {
      for (R row: rowsCreated) {
        El rowCreated = createRowCreated(row, tabIndex);
        if (rowCreated != null) {
          appendChild(rowCreated);
        }
      }
    }
    
    setAttribute("tabindex-next", tabIndexValue);
  }
  
  /**
   * Creates and returns a table row without adding it to the table. 
   * @param row
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return
   */
  protected El createRow(R row, TabIndex tabIndex) {
    final El rowEl = new El("div", context);
    rowEl.classList.add("row");

    El div = new El("div", rowEl.context);
    div.classList.add("flexColumns");
    div.classList.add("column-left");
    
    for (Cell cell: row) {
      String columnClassName;
      {
        String cellName = cell.name;
        if (cellName != null) {
          columnClassName = "column-" + cellName;
        } else {
          columnClassName = "column";
        }
      }
      
      El cellEl = createCell(div, columnClassName);
      cellEl.classList.add("cell-field");
      
      fillCell(cellEl, cell, tabIndex);
    }
    
    rowEl.appendChild(div);
    return rowEl;
  }
  
  /**
   * Creates a table row (in created state) representing a single item, that has not been saved to the server yet,
   * but having a UI table row created for saving.
   * <br/>Implementors should not manually add the element to the table, just return it.
   * @param row optional data from the UI to fill the created table row with, may be null
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return {@code null} if no rowCreated required for the table (e.g. the table does not support creation of new items)
   */
  protected  El createRowCreated(R row, TabIndex tabIndex) {return null;}
  
  protected static void addField(El cell, El field) {
    El wrapper = Fields.wrapCellPad(field);
    cell.appendChild(wrapper);
    
    field.classList.add("table__field-text_inactivatible");
    field.classList.add("table__field_disableable");
  }
  
  protected static void addStrike(El cell) {
    El cellStrike = new El("div", cell.context);
    cellStrike.classList.add("cell__strike");
    cell.appendChild(cellStrike);
  }

  /**
   * Creates a field and adds it into the cell 
   * @param cellEl
   * @param cell
   * @param tabIndex
   * @return the created field
   */
  protected El fillCell(El cellEl, Cell cell, TabIndex tabIndex) {

    El fieldEl;
    
    if (cell instanceof CellField) {
      CellField cellField = (CellField) cell;
      fieldEl = new FieldTextInput(cellEl.context, cell.name,
          cellField.value, cellField.valueOriginal,
          cellField.invalid, cellField.invalidMessage);

      if (tabIndex != null) {
        tabIndex.setNext(fieldEl);
      }

    } else if (cell instanceof CellStatic) {

      CellStatic cellStatic = (CellStatic) cell;
      Node content = cellStatic.content;

      if (content == null) {
        fieldEl = new FieldTextLabel(cellEl.context);

      } else {
        String html = content.printHtml();
        fieldEl = new FieldTextLabel(cellEl.context, html);
      }

    } else if (cell == null) {
      // TODO allow null cells
      throw new UnsupportedOperationException("Not implemented yet");

    } else {
      throw new IllegalArgumentException(cell.getClass().getCanonicalName());
    }

    addField(cellEl, fieldEl);

    addStrike(cellEl);
    
    return fieldEl;
  }
  
  /**
   * 
   * @param cell
   * @param tabIndex
   * @param titleDelete text to display as a title of delete button. If {@code null} then no title
   * @param titleUndelete text to display as a title of undelete button. If {@code null} then no title
   */
  protected El addFieldDelete(El cell, TabIndex tabIndex, String titleDelete, String titleUndelete) {

    El field = new El("div", context);
    
    {// button delete
      El button = new El("input", context);
      button.classList.add("img-button");
      button.classList.add("button-delete");
      button.classList.add("button-delete_delete");
      button.setAttribute("type", "image");
      button.setAttribute("src", context.getAppContextPath() + "/img/delete.png");
      if (titleDelete != null) {
        button.setAttribute("title", titleDelete);
      }
      if (tabIndex != null) {
        tabIndex.setNext(button);
      }
      
      field.appendChild(button);
    }
    
    {// button undelete
      El button = new El("input", context);
      button.classList.add("img-button");
      button.classList.add("button-delete");
      button.classList.add("button-delete_undelete");
      button.setAttribute("type", "image");
      button.setAttribute("src", context.getAppContextPath() + "/img/undelete.png");
      if (titleUndelete != null) {
        button.setAttribute("title", titleUndelete);
      }
      if (tabIndex != null) {
        tabIndex.setNext(button);
      }
      
      field.appendChild(button);
    }
    
    El wrapper = Fields.wrapCellPad(field);  
    cell.appendChild(wrapper);
    
    return field;
  }
  
  /**
   * 
   * @param row
   * @param columnClass nullable
   * @return
   */
  protected El createCell(El row, String columnClass) {
    El cell = new El("div", row.context);
    cell.classList.add("cell");
    if (columnClass != null) {
      cell.classList.add(columnClass);
    }
    row.appendChild(cell);
    return cell;
  }
  
  /**
   * 
   * @param cell
   * @param field
   * @param titleActive text to display as a title of active checkbox. If {@code null} then empty title
   * @param titleInactive text to display as a title of inactive checkbox. If {@code null} then empty title
   * @return
   */
  protected FieldCheckBox addCheckbox(El cell, CellField field, String titleActive, String titleInactive) {

    FieldCheckBox checkbox;

    {
      boolean active = !"false".equals(field.value);
      Boolean valueOriginal;
      if (field.valueOriginal != null) {
        valueOriginal = !"false".equals(field.valueOriginal);
      } else {
        valueOriginal = null;
      }

      checkbox = new FieldCheckBox(cell.context, field.name, active, valueOriginal, field.invalid, field.invalidMessage);

      checkbox.setEnabled(true);

      // add text attributes
      checkbox.setTitleActive(titleActive);
      checkbox.setTitleInactive(titleInactive);


      El wrapper = Fields.wrapCellPad(checkbox);
      cell.appendChild(wrapper);
    }


    checkbox.classList.add("table__checkbox");
    checkbox.classList.add("table__field_disableable");

    
    addStrike(cell);

    return checkbox;
  }

  /**
   *
   * @param cellEl
   * @param cell
   * @param titleActive text to display as a title of active checkbox. If {@code null} then empty title
   * @param titleInactive text to display as a title of inactive checkbox. If {@code null} then empty title
   * @return
   */
  protected FieldCheckBox addCheckbox(El cellEl, CellStaticCheckbox cell, String titleActive, String titleInactive) {

    FieldCheckBox checkbox;

    {
      boolean active = !Boolean.FALSE.equals(cell.value);

      checkbox = new FieldCheckBox(cellEl.context, cell.name, active, null, false, null);

      checkbox.setEnabled(false);

      // add text attributes
      checkbox.setTitleActive(titleActive);
      checkbox.setTitleInactive(titleInactive);


      El wrapper = Fields.wrapCellPad(checkbox);
      cellEl.appendChild(wrapper);
    }


    checkbox.classList.add("table__checkbox");
    checkbox.classList.add("table__field_disableable");


    addStrike(cellEl);

    return checkbox;
  }
  
  /**
   * Creates and returns a table header without adding it to the table. 
   * @return {@code null} if no header required for the table
   */
  protected El createHeader() {
    if (header == null) {
      return null;
    } else {

      final El headerEl = new El("div", context);
      headerEl.classList.add("header");

      El flexColumns = new El("div", headerEl.context);
      flexColumns.classList.add("flexColumns");
      flexColumns.classList.add("column-left");

      for (CellHeader cell : header) {
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
  }
}
