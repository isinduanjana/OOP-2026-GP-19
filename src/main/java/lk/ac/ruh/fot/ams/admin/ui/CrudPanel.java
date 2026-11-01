package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [M1 - ADMIN MODULE] Generic "table + search + Add / Edit / Delete" page.
 * UserManagementPanel, CourseManagementPanel, NoticeManagementPanel and
 * TimetableManagementPanel are all subclasses - each one only says
 * WHAT to show (columns, rows) and HOW to edit/delete one item.
 *
 * Connection to common: extends BasePanel (so the dashboard can show it and calls
 * refresh() when the page opens); uses DataTableModel + UI.table for the table.
 *
 * OOP:
 *  - Abstraction : abstract class with abstract methods columns(), loadItems(), toRow(),
 *                  openEditor(), deleteItem(), describe(), idOf().
 *  - Generics    : CrudPanel&lt;T&gt; works for any item type (User, Course, Notice, TimetableEntry).
 *  - Inheritance : 4 concrete subclasses inherit the whole toolbar / table / delete logic.
 *  - Polymorphism: refresh() and onDelete() call the abstract methods; Java runs the
 *                  subclass version at run time. AdminDashboard keeps them as CrudPanel&lt;?&gt;.
 *  - Template Method: refresh() = beforeLoad() -> loadItems() -> accept() -> toRow().
 *
 * NOTE: columns() and columnWidths() are called from this constructor, so subclasses must
 * return constant values there (subclass fields are not initialised yet at that moment).
 *
 * @param <T> the type of item shown in each row
 */
public abstract class CrudPanel<T> extends BasePanel {

    protected final DataTableModel model;
    protected final JTable table;
    protected final JTextField searchField;
    protected final ModernButton addButton;
    protected final ModernButton editButton;
    protected final ModernButton deleteButton;

    private final String itemName;
    private final JPanel filterRow = UI.row();
    private final JPanel actionRow = UI.row();
    private final JLabel countLabel = UI.muted("");
    private final JLabel flashLabel = new JLabel(" ");
    private final Map<JButton, Boolean> selectionButtons = new LinkedHashMap<>();
    private final CardPanel tableCard = new CardPanel(new BorderLayout(0, 14));
    private final List<T> allItems = new ArrayList<>();
    private Timer flashTimer;

    /**
     * @param itemName singular name used in buttons and messages, e.g. "user"
     */
    protected CrudPanel(User user, Color accent, String itemName) {
        super(user, accent);
        this.itemName = itemName;

        model = new DataTableModel(columns());
        table = UI.table(model);
        int[] widths = columnWidths();
        if (widths != null) UI.setColumnWidths(table, widths);

        searchField = UI.searchField("Search " + itemName + "s...");
        searchField.setPreferredSize(new Dimension(230, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applySearch(); }
            @Override public void removeUpdate(DocumentEvent e) { applySearch(); }
            @Override public void changedUpdate(DocumentEvent e) { applySearch(); }
        });
        filterRow.add(searchField);

        addButton = accentButton("Add " + itemName, "plus");
        addButton.addActionListener(e -> openAddDialog());
        editButton = UI.secondary("Edit", "edit");
        editButton.addActionListener(e -> onEdit());
        deleteButton = UI.danger("Delete", "trash");
        deleteButton.addActionListener(e -> onDelete());
        actionRow.add(editButton);
        actionRow.add(deleteButton);
        selectionButtons.put(editButton, true);
        selectionButtons.put(deleteButton, true);

        // double-click a row = edit
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) onEdit();
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) selectionChanged();
        });

        // ---- layout:  [search + filters ........ Add]
        //               [Edit Delete extras ...... count]
        //               [            table             ]
        //               [flash message ......... hint  ]
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(UI.toolbar(filterRow, UI.rowRight(addButton)), BorderLayout.NORTH);
        top.add(UI.toolbar(actionRow, UI.rowRight(countLabel)), BorderLayout.SOUTH);

        flashLabel.setFont(Theme.FONT_BOLD);
        flashLabel.setForeground(Theme.SUCCESS);
        JPanel footer = UI.toolbar(flashLabel, UI.muted("Tip: double-click a row to edit it"));

        tableCard.add(top, BorderLayout.NORTH);
        tableCard.add(UI.scroll(table), BorderLayout.CENTER);
        tableCard.add(footer, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);
        editButton.setEnabled(false);     // nothing selected yet (do NOT call overridable hooks here)
        deleteButton.setEnabled(false);
    }

    // ================================================================ abstract (each page decides)
    /** Column headers of the table. */
    protected abstract String[] columns();

    /** Loads all items from the database. */
    protected abstract List<T> loadItems() throws AppException;

    /** Converts one item to the values of a table row. */
    protected abstract Object[] toRow(T item);

    /**
     * Opens the add (item == null) or edit dialog.
     * @return true when something was saved (the table is then reloaded)
     */
    protected abstract boolean openEditor(T itemOrNull) throws AppException;

    /** Deletes the item from the database (the user already confirmed). */
    protected abstract void deleteItem(T item) throws AppException;

    /** Short human-readable description used in messages, e.g. "ICT2132 - OOP". */
    protected abstract String describe(T item);

    /** Primary key, used to keep the same row selected after a reload. */
    protected abstract int idOf(T item);

    // ================================================================ hooks (optional overrides)
    /** Preferred column widths (constant values). */
    protected int[] columnWidths() {
        return null;
    }

    /** Called before loadItems(), e.g. to fill filter combos once. */
    protected void beforeLoad() throws AppException {
    }

    /** Filter combos: return false to hide an item (no database call). */
    protected boolean accept(T item) {
        return true;
    }

    /** Throw a ValidationException to forbid deleting this item (e.g. your own account). */
    protected void checkCanDelete(T item) throws AppException {
    }

    /** Extra sentence for the delete confirmation (e.g. what else will be removed). */
    protected String deleteWarning(T item) throws AppException {
        return "";
    }

    /** Called whenever the selected row changes (item may be null). */
    protected void onSelectionChanged(T item) {
    }

    // ================================================================ helpers for subclasses
    /** Adds a filter control next to the search box. */
    protected void addFilter(JComponent c) {
        filterRow.add(c);
    }

    /** Adds an extra action button (enabled only when a row is selected, if requested). */
    protected void addAction(JButton b, boolean needsSelection) {
        actionRow.add(b);
        selectionButtons.put(b, needsSelection);
        b.setEnabled(!needsSelection || getSelected() != null);
    }

    /** Puts a panel to the right of the table card (e.g. a preview). */
    protected void setSidePanel(JComponent side) {
        add(side, BorderLayout.EAST);
    }

    /** Puts a panel above the table card (e.g. summary chips). */
    protected void setTopPanel(JComponent topPanel) {
        add(topPanel, BorderLayout.NORTH);
    }

    /** The item of the selected row, or null. */
    protected T getSelected() {
        return model.getItem(UI.selectedModelRow(table));
    }

    /** All loaded items (before filters). */
    protected List<T> getAllItems() {
        return allItems;
    }

    /** Re-applies accept() on the cached items (call from filter combos). */
    protected void applyFilter() {
        T selected = getSelected();
        model.clear();
        for (T item : allItems) {
            if (accept(item)) model.addRow(item, toRow(item));       // polymorphic calls
        }
        model.refresh();
        reselect(selected == null ? -1 : idOf(selected));
        updateCount();
    }

    /** Shows a green message under the table for a few seconds. */
    protected void flash(String message) {
        flashLabel.setText(message);
        if (flashTimer != null) flashTimer.stop();
        flashTimer = new Timer(5000, e -> flashLabel.setText(" "));
        flashTimer.setRepeats(false);
        flashTimer.start();
    }

    protected String getItemName() {
        return itemName;
    }

    // ================================================================ behaviour
    /** Template method: load -> filter -> show. */
    @Override
    public void refresh() {
        T selected = getSelected();
        int keepId = selected == null ? -1 : idOf(selected);
        safe(() -> {
            beforeLoad();
            List<T> items = loadItems();
            allItems.clear();
            allItems.addAll(items);
            applyFilter();
            reselect(keepId);
        });
    }

    /** Opens the "Add" dialog (also used by the dashboard quick actions). */
    public void openAddDialog() {
        safe(() -> {
            if (openEditor(null)) {
                refresh();
                flash("New " + itemName + " saved.");
            }
        });
    }

    private void onEdit() {
        T item = getSelected();
        if (item == null) {
            UI.warn(this, "Please select a " + itemName + " first.");
            return;
        }
        safe(() -> {
            if (openEditor(item)) {
                refresh();
                flash("Changes to " + describe(item) + " saved.");
            }
        });
    }

    private void onDelete() {
        T item = getSelected();
        if (item == null) {
            UI.warn(this, "Please select a " + itemName + " first.");
            return;
        }
        safe(() -> {
            checkCanDelete(item);
            String msg = "Delete " + itemName + " \"" + describe(item) + "\"?" + deleteWarning(item)
                    + "\n\nThis cannot be undone.";
            if (UI.confirm(this, msg)) {
                deleteItem(item);
                table.clearSelection();
                refresh();
                flash(capital(itemName) + " \"" + describe(item) + "\" deleted.");
            }
        });
    }

    private void selectionChanged() {
        T item = getSelected();
        for (Map.Entry<JButton, Boolean> e : selectionButtons.entrySet()) {
            e.getKey().setEnabled(!e.getValue() || item != null);
        }
        onSelectionChanged(item);
    }

    private void applySearch() {
        UI.filter(table, searchField.getText());
        updateCount();
    }

    private void updateCount() {
        int shown = table.getRowCount();
        int total = allItems.size();
        countLabel.setText(shown == total ? total + " " + itemName + (total == 1 ? "" : "s")
                : "Showing " + shown + " of " + total + " " + itemName + "s");
    }

    private void reselect(int id) {
        if (id < 0) {
            selectionChanged();
            return;
        }
        for (int r = 0; r < model.getRowCount(); r++) {
            T item = model.getItem(r);
            if (idOf(item) == id) {
                int view = table.convertRowIndexToView(r);
                if (view >= 0) {
                    table.setRowSelectionInterval(view, view);
                    table.scrollRectToVisible(table.getCellRect(view, 0, true));
                }
                break;
            }
        }
        selectionChanged();
    }

    private static String capital(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
