package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import lk.ac.ruh.fot.ams.admin.dao.AdminAssessmentDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Assessment;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.enums.AssessmentType;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Validator;

/**
 * [M1 - ADMIN MODULE] "Manage assessments" window of a course: the admin defines the
 * evaluation criteria (Quiz 1 10%, Assignment 10%, Mid 20%, Final 60% ...).
 * All marks are out of 100 (max_marks fixed); the weights should add up to 100%.
 *
 * OOP: Inheritance (AdminDialog), Exception handling (WeightLimitExceededException
 *      thrown by AdminAssessmentDAO.save is caught and shown).
 */
public class AssessmentDialog extends AdminDialog {

    private final AdminAssessmentDAO dao = new AdminAssessmentDAO();
    private final Course course;
    private final DataTableModel model = new DataTableModel("Assessment", "Type", "Part", "Weight %", "Max marks", "Marks entered");
    private final JTable table = UI.table(model);
    private final JTextField nameField = UI.textField(16);
    private final JComboBox<AssessmentType> typeCombo = new JComboBox<>(AssessmentType.values());
    private final JSpinner weightSpin = new JSpinner(new SpinnerNumberModel(10.0, 0.5, 100.0, 0.5));
    private final JLabel totalLabel = new JLabel();
    private final JLabel caLabel = UI.muted("");
    private final ModernButton addBtn;
    private final ModernButton updateBtn = UI.secondary("Update selected", "edit");
    private final ModernButton deleteBtn = UI.danger("Delete", "trash");

    public AssessmentDialog(Component parent, Color accent, Course course) {
        super(parent, "Assessments - " + course.getCourseCode(),
                course.getCourseName() + "  ·  marks are entered out of 100, weights must total 100%", "clipboard", accent);
        this.course = course;
        addBtn = UI.primary("Add", "plus");
        addBtn.setBaseColor(accent);
        UI.setColumnWidths(table, 190, 120, 90, 80, 80, 110);
        model.setColumnClasses(String.class, String.class, String.class, Double.class, Integer.class, Integer.class);

        // ---- form row
        nameField.putClientProperty("JTextField.placeholderText", "e.g. Quiz 3");
        weightSpin.setPreferredSize(new Dimension(90, 36));
        typeCombo.setPreferredSize(new Dimension(140, 36));
        JPanel formRow = UI.row(UI.label("Name"), nameField, UI.label("Type"), typeCombo, UI.label("Weight %"), weightSpin);
        CardPanel formCard = new CardPanel(new BorderLayout(0, 10));
        formCard.add(formRow, BorderLayout.NORTH);
        formCard.add(UI.toolbar(UI.row(addBtn, updateBtn, deleteBtn), null), BorderLayout.SOUTH);

        totalLabel.setFont(Theme.FONT_HEADING);
        JPanel totals = UI.toolbar(UI.row(UI.label("Total weight:"), totalLabel), UI.rowRight(caLabel));

        JScrollPane sp = UI.scroll(table);
        sp.setPreferredSize(new Dimension(680, 230));
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        content.add(sp, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout(0, 12));
        south.setOpaque(false);
        south.add(totals, BorderLayout.NORTH);
        south.add(formCard, BorderLayout.CENTER);
        content.add(south, BorderLayout.SOUTH);
        setContent(content);

        ModernButton close = UI.secondary("Close", null);
        close.addActionListener(e -> closeDialog());
        addButton(close);

        addBtn.addActionListener(e -> saveAssessment(null));
        updateBtn.addActionListener(e -> {
            Assessment a = selected();
            if (a == null) UI.warn(this, "Select an assessment in the table first."); else saveAssessment(a);
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelected();
        });
        reload();
    }

    private Assessment selected() {
        return model.getItem(UI.selectedModelRow(table));
    }

    private void reload() {
        try {
            List<Assessment> list = dao.findByCourse(course.getCourseId());
            model.clear();
            double total = 0;
            double ca = 0;
            for (Assessment a : list) {
                model.addRow(a, a.getName(), a.getType().toString(),
                        a.getType().isContinuousAssessment() ? "CA" : "Final exam",
                        a.getPercentage(), a.getMaxMarks(), dao.countMarks(a.getAssessmentId()));
                total += a.getPercentage();
                if (a.getType().isContinuousAssessment()) ca += a.getPercentage();
            }
            model.refresh();
            boolean ok = Math.abs(total - 100) < 0.001;
            totalLabel.setText(String.format("%.1f%%", total) + (ok ? "  ✓" : "  (should be 100%)"));
            totalLabel.setForeground(ok ? Theme.SUCCESS : Theme.WARNING);
            caLabel.setText(String.format("CA part %.1f%%  ·  Final part %.1f%%  ·  %d assessment(s)", ca, total - ca,
                    list.size()));
            double left = 100 - total;
            if (selected() == null) weightSpin.setValue(left >= 0.5 ? Math.min(100.0, left) : 5.0);
            updateBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        } catch (AppException e) {
            UI.error(this, e);
        }
    }

    private void showSelected() {
        Assessment a = selected();
        updateBtn.setEnabled(a != null);
        deleteBtn.setEnabled(a != null);
        if (a != null) {
            nameField.setText(a.getName());
            typeCombo.setSelectedItem(a.getType());
            weightSpin.setValue(a.getPercentage());
        }
    }

    /** a == null -> new assessment. */
    private void saveAssessment(Assessment a) {
        try {
            Assessment item = a == null ? new Assessment() : a;
            item.setCourseId(course.getCourseId());
            item.setName(Validator.required(nameField.getText(), "Assessment name"));
            if (item.getName().length() > 50) throw new ValidationException("Name", "Name can have at most 50 characters.");
            item.setType((AssessmentType) typeCombo.getSelectedItem());
            item.setPercentage(((Number) weightSpin.getValue()).doubleValue());
            item.setMaxMarks(100);
            if (item.getType() == AssessmentType.FINAL && a == null) {
                for (int r = 0; r < model.getRowCount(); r++) {
                    Assessment x = model.getItem(r);
                    if (x.getType() == AssessmentType.FINAL) {
                        throw new ValidationException("Type", "This course already has a final exam (" + x.getName() + ").");
                    }
                }
            }
            dao.save(item);           // may throw WeightLimitExceededException
            table.clearSelection();
            nameField.setText("");
            reload();
        } catch (ValidationException e) {
            UI.warn(this, e.getUserMessage());
            if (a != null) reload();      // undo the unsaved change shown in the table object
        } catch (AppException e) {
            UI.error(this, e);
        }
    }

    private void deleteSelected() {
        Assessment a = selected();
        if (a == null) return;
        try {
            int marks = dao.countMarks(a.getAssessmentId());
            String extra = marks > 0 ? "\n" + marks + " mark(s) already entered for it will also be deleted!" : "";
            if (UI.confirm(this, "Delete assessment \"" + a.getName() + "\"?" + extra)) {
                dao.delete(a.getAssessmentId());
                table.clearSelection();
                reload();
            }
        } catch (AppException e) {
            UI.error(this, e);
        }
    }

    /** Warn before closing when the weights do not add up to 100%. */
    @Override
    protected void closeDialog() {
        double total = 0;
        for (int r = 0; r < model.getRowCount(); r++) total += ((Assessment) model.getItem(r)).getPercentage();
        if (model.getRowCount() > 0 && Math.abs(total - 100) > 0.001
                && !UI.confirm(this, String.format("The weights add up to %.1f%%, not 100%%.%n"
                        + "Results of this course will be wrong until this is fixed.%n%nClose anyway?", total))) {
            return;
        }
        dispose();
    }
}
