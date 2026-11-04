package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import lk.ac.ruh.fot.ams.dao.AssessmentDAO;
import lk.ac.ruh.fot.ams.dao.StudentDAO;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.lecturer.dao.LecturerMarksDAO;
import lk.ac.ruh.fot.ams.lecturer.service.MarkParser;
import lk.ac.ruh.fot.ams.model.Assessment;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;


public class MarksEntryPanel extends LecturerPage {

    private static final int COL_MARK = 4;
    private static final int COL_STATUS = 5;

    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final LecturerMarksDAO marksDAO = new LecturerMarksDAO();

    private final JComboBox<Course> courseBox = new JComboBox<>();
    private final JComboBox<Assessment> assessmentBox = new JComboBox<>();
    private final DataTableModel model = new DataTableModel("TG No", "Name", "Type", "Batch", "Mark /100", "Status");
    private final JTable table = UI.table(model);
    private final JTextField search = UI.searchField("Search student...");
    private final JLabel info = UI.muted("");

    private final StatCard weightCard;
    private final StatCard enteredCard;
    private final StatCard averageCard;
    private final StatCard rangeCard;

    /** Marks as they are in the database (studentId -> mark) - used to find changed rows. */
    private Map<Integer, Double> saved = new HashMap<>();
    private boolean loading;

    public MarksEntryPanel(User user, Color accent) {
        super(user, accent);
        weightCard = new StatCard("Weight", "-", "chart", accent);
        enteredCard = new StatCard("Marks entered", "-", "clipboard", Theme.INFO);
        averageCard = new StatCard("Average", "-", "grade", Theme.SUCCESS);
        rangeCard = new StatCard("Highest / lowest", "-", "award", Theme.WARNING);

        courseBox.setPreferredSize(new Dimension(330, 34));
        assessmentBox.setPreferredSize(new Dimension(280, 34));

        ModernButton save = accentButton("Save all", "check");
        save.addActionListener(e -> saveAll());
        ModernButton clear = UI.secondary("Clear selected mark", "trash");
        clear.addActionListener(e -> clearSelected());
        ModernButton reload = UI.secondary("Discard changes", "refresh");
        reload.addActionListener(e -> loadMarks());

        CardPanel filters = new CardPanel(new GridLayout(2, 1, 0, 8));
        filters.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        filters.add(UI.row(UI.label("Course"), courseBox, Box.createHorizontalStrut(10),
                UI.label("Assessment"), assessmentBox));
        filters.add(UI.row(UI.muted("Type a mark between 0 and 100 in the \"Mark /100\" column (leave empty = not entered),"
                + " then press Save all.")));

        JPanel north = new JPanel(new BorderLayout(0, 14));
        north.setOpaque(false);
        north.add(filters, BorderLayout.NORTH);
        north.add(statRow(weightCard, enteredCard, averageCard, rangeCard), BorderLayout.CENTER);

        // editable mark column with a centred text editor
        model.setEditableColumns(COL_MARK);
        JTextField editor = new JTextField();
        editor.setHorizontalAlignment(SwingConstants.RIGHT);
        editor.setBorder(BorderFactory.createLineBorder(accent, 2));
        DefaultCellEditor cellEditor = new DefaultCellEditor(editor);
        cellEditor.setClickCountToStart(1);
        table.getColumnModel().getColumn(COL_MARK).setCellEditor(cellEditor);
        table.getColumnModel().getColumn(COL_MARK).setCellRenderer(new MarkRenderer());
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(new StatusColumnRenderer());
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setSurrendersFocusOnKeystroke(true);
        UI.setColumnWidths(table, 120, 220, 110, 70, 110, 150);
        model.addTableModelListener(e -> {
            if (!loading && e.getColumn() == COL_MARK && e.getFirstRow() >= 0 && e.getFirstRow() == e.getLastRow()) {
                updateRowStatus(e.getFirstRow());
            }
        });
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { UI.filter(table, search.getText()); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { UI.filter(table, search.getText()); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { UI.filter(table, search.getText()); }
        });

        CardPanel tableCard = new CardPanel(new BorderLayout(0, 12));
        tableCard.add(UI.toolbar(UI.row(UI.heading("Enrolled students"), info), UI.rowRight(UI.label("Search"), search)), BorderLayout.NORTH);
        tableCard.add(UI.scroll(table), BorderLayout.CENTER);
        tableCard.add(UI.rowRight(clear, reload, save), BorderLayout.SOUTH);
        tableCard.setPreferredSize(new Dimension(400, 380));

        add(north, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        courseBox.addActionListener(e -> { if (!loading) loadAssessments(); });
        assessmentBox.addActionListener(e -> { if (!loading) loadMarks(); });
    }

    @Override
    public String getTitle() {
        return "Marks Entry";
    }

    @Override
    public String getSubtitle() {
        return "Upload marks for quizzes, assignments, mid and final exams (all out of 100)";
    }

    @Override
    public void refresh() {
        loading = true;
        boolean ok = safe(() -> fillCombo(courseBox, myCourses()));
        loading = false;
        if (ok) loadAssessments();
    }

    private void loadAssessments() {
        Course c = (Course) courseBox.getSelectedItem();
        loading = true;
        if (c == null) {
            assessmentBox.removeAllItems();
        } else {
            safe(() -> fillCombo(assessmentBox, assessmentDAO.findByCourse(c.getCourseId())));
        }
        loading = false;
        loadMarks();
    }

    /** Loads enrolled students + their current mark for the chosen assessment. */
    private void loadMarks() {
        stopEditing();
        Course c = (Course) courseBox.getSelectedItem();
        Assessment a = (Assessment) assessmentBox.getSelectedItem();
        loading = true;
        model.clear();
        saved = new HashMap<>();
        if (c != null && a != null) {
            safe(() -> {
                saved = marksDAO.findMarks(a.getAssessmentId());
                for (Undergraduate s : studentDAO.findByCourse(c.getCourseId())) {
                    Double m = saved.get(s.getStudentId());
                    model.addRow(s, s.getTgNo(), s.getFullName(), s.getStudentType().toString(), s.getBatch(),
                            MarkParser.format(m), m == null ? "Not entered" : "Saved");
                }
            });
        }
        model.refresh();
        loading = false;
        updateStats();
    }

    /** Re-checks one edited row and shows "Changed", "Invalid" ... in the status column. */
    private void updateRowStatus(int row) {
        Undergraduate s = model.getItem(row);
        Object v = model.getValueAt(row, COL_MARK);
        String status;
        try {
            Double m = MarkParser.parse(v == null ? null : v.toString(), s.getTgNo());
            Double old = saved.get(s.getStudentId());
            if (m == null) status = old == null ? "Not entered" : "Will be cleared";
            else status = m.equals(old) ? "Saved" : "Changed (not saved)";
        } catch (ValidationException ex) {
            status = "Invalid (0-100)";
        }
        loading = true;
        model.setValueAt(status, row, COL_STATUS);
        loading = false;
        updateStats();
    }

    private void updateStats() {
        Assessment a = (Assessment) assessmentBox.getSelectedItem();
        weightCard.setValue(a == null ? "-" : (int) a.getPercentage() + "%");
        weightCard.setCaption(a == null ? "" : a.getType() + " - of final 100");
        int entered = 0;
        double sum = 0;
        double hi = -1;
        double lo = 101;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, COL_MARK);
            try {
                Double m = MarkParser.parse(v == null ? null : v.toString(), "");
                if (m != null) {
                    entered++;
                    sum += m;
                    hi = Math.max(hi, m);
                    lo = Math.min(lo, m);
                }
            } catch (ValidationException ignored) {
                // invalid rows are shown in the status column
            }
        }
        int total = model.getRowCount();
        enteredCard.setValue(entered + " / " + total);
        enteredCard.setCaption(total - entered + " still missing");
        averageCard.setValue(entered == 0 ? "-" : String.format("%.1f", sum / entered));
        averageCard.setCaption("out of 100");
        rangeCard.setValue(entered == 0 ? "-" : MarkParser.format(hi) + " / " + MarkParser.format(lo));
        rangeCard.setCaption("this assessment");
        info.setText(total + " student(s)");
    }

    /** Validates every row, then saves only the changed ones in one transaction. */
    private void saveAll() {
        stopEditing();
        Course c = (Course) courseBox.getSelectedItem();
        Assessment a = (Assessment) assessmentBox.getSelectedItem();
        if (c == null || a == null) return;
        int[] written = {0};
        boolean ok = safe(() -> {
            if (!marksDAO.teaches(lecturerId(), c.getCourseId())) {
                throw new ValidationException("You are not assigned to " + c.getCourseCode() + ".");
            }
            Map<Integer, Double> changes = new LinkedHashMap<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                Undergraduate s = model.getItem(i);
                Object v = model.getValueAt(i, COL_MARK);
                Double m = MarkParser.parse(v == null ? null : v.toString(), s.getTgNo()); // may throw MarksOutOfRangeException
                Double old = saved.get(s.getStudentId());
                boolean changed = m == null ? old != null : !m.equals(old);
                if (changed) changes.put(s.getStudentId(), m);
            }
            if (changes.isEmpty()) {
                throw new ValidationException("There are no changes to save.");
            }
            written[0] = marksDAO.saveMarks(a.getAssessmentId(), changes, user.getUserId());
        });
        if (ok) {
            UI.info(this, written[0] + " mark(s) of " + a.getName() + " saved for " + c.getCourseCode() + ".");
            loadMarks();
        }
    }

    /** Removes the mark of the selected student (after confirmation). */
    private void clearSelected() {
        stopEditing();
        int row = UI.selectedModelRow(table);
        Assessment a = (Assessment) assessmentBox.getSelectedItem();
        if (row < 0 || a == null) {
            UI.warn(this, "Select a student first.");
            return;
        }
        Undergraduate s = model.getItem(row);
        if (!saved.containsKey(s.getStudentId())) {
            model.setValueAt("", row, COL_MARK);
            return;
        }
        if (!UI.confirm(this, "Remove the " + a.getName() + " mark of " + s.getTgNo() + " (" + s.getFullName() + ")?")) {
            return;
        }
        if (safe(() -> marksDAO.clearMark(a.getAssessmentId(), s.getStudentId()))) {
            loadMarks();
        }
    }

    private void stopEditing() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
    }

    /** Colours the status words: Saved = green, Changed = orange, Invalid = red. */
    private static final class StatusColumnRenderer extends UI.StatusRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus,
                                                       int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            String s = String.valueOf(value);
            Color c = s.startsWith("Saved") ? Theme.SUCCESS : s.startsWith("Invalid") ? Theme.DANGER
                    : s.startsWith("Changed") || s.startsWith("Will") ? Theme.WARNING : Theme.TEXT_MUTED;
            if (!sel) l.setForeground(c);
            l.setFont(c == Theme.TEXT_MUTED ? Theme.FONT : Theme.FONT_BOLD);
            return l;
        }
    }

    /** Renders the mark column right-aligned with a light accent background (looks editable). */
    private final class MarkRenderer extends UI.StatusRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus,
                                                       int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            l.setHorizontalAlignment(SwingConstants.RIGHT);
            l.setFont(Theme.FONT_BOLD);
            if (!sel) l.setBackground(Theme.tint(accent, row % 2 == 0 ? 0.93 : 0.89));
            return l;
        }
    }
}
