package lk.ac.ruh.fot.ams.ui.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

/**
 * [COMMON PART] Simple table model: column names + rows of Object[].
 * Read-only unless columns are marked editable. Each row can also carry the
 * original object (e.g. a Course) so screens can get the selected item back.
 */
public class DataTableModel extends AbstractTableModel {

    private final String[] columns;
    private final List<Object[]> rows = new ArrayList<>();
    private final List<Object> items = new ArrayList<>();
    private final Set<Integer> editableColumns = new HashSet<>();
    private Class<?>[] columnClasses;

    public DataTableModel(String... columns) {
        this.columns = columns;
    }

    public void setEditableColumns(int... cols) {
        editableColumns.clear();
        for (int c : cols) editableColumns.add(c);
    }

    public void setColumnClasses(Class<?>... classes) {
        this.columnClasses = classes;
    }

    public void clear() {
        rows.clear();
        items.clear();
        fireTableDataChanged();
    }

    /** Adds a row and remembers the object it represents. */
    public void addRow(Object item, Object... values) {
        rows.add(values);
        items.add(item);
    }

    /** Call after adding rows. */
    public void refresh() {
        fireTableDataChanged();
    }

    @SuppressWarnings("unchecked")
    public <T> T getItem(int modelRow) {
        return modelRow < 0 || modelRow >= items.size() ? null : (T) items.get(modelRow);
    }

    public List<Object[]> getRows() {
        return rows;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int row, int col) {
        Object[] r = rows.get(row);
        return col < r.length ? r[col] : null;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        rows.get(row)[col] = value;
        fireTableCellUpdated(row, col);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return editableColumns.contains(col);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (columnClasses != null && col < columnClasses.length && columnClasses[col] != null) {
            return columnClasses[col];
        }
        return Object.class;
    }
}
