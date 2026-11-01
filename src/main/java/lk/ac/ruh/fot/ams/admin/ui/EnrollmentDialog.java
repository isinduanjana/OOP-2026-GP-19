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
import javax.swing.JTable;

import lk.ac.ruh.fot.ams.admin.dao.AdminEnrollmentDAO;
import lk.ac.ruh.fot.ams.admin.model.Enrollment;
import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [M1 - ADMIN MODULE] Shows the students enrolled in a course and lets the admin
 * enrol another undergraduate, withdraw a student (status WITHDRAWN) or remove the row.
 *
 * OOP: Inheritance (AdminDialog), Exception handling (DuplicateRecordException on double enrolment).
 */
public class EnrollmentDialog extends AdminDialog {

    private final AdminEnrollmentDAO dao = new AdminEnrollmentDAO();
    private final Course course;
    private final DataTableModel model = new DataTableModel("TG number", "Student name", "Type", "Academic year",
            "Semester", "Status");
    private final JTable table = UI.table(model);
    private final JComboBox<Undergraduate> studentCombo = new JComboBox<>();
    private final JLabel summary = UI.muted("");
    private final ModernButton withdrawBtn = UI.secondary("Withdraw", "eye");
    private final ModernButton removeBtn = UI.danger("Remove", "trash");
    private final JComboBox<String> yearFilter = new JComboBox<>(new String[]{"This year (" + AppConfig.CURRENT_ACADEMIC_YEAR + ")", "All years"});

    public EnrollmentDialog(Component parent, Color accent, Course course) {
        super(parent, "Enrollments - " + course.getCourseCode(), course.getCourseName(), "users", accent);
        this.course = course;
        UI.setColumnWidths(table, 120, 200, 110, 110, 80, 90);

        ModernButton enrollBtn = UI.primary("Enroll", "plus");
        enrollBtn.setBaseColor(accent);
        studentCombo.setPreferredSize(new Dimension(330, 36));
        CardPanel addCard = new CardPanel(new BorderLayout(10, 0));
        addCard.add(UI.row(UI.label("Student"), studentCombo, enrollBtn), BorderLayout.WEST);

        JScrollPane sp = UI.scroll(table);
        sp.setPreferredSize(new Dimension(760, 300));
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        content.add(UI.toolbar(UI.row(yearFilter, withdrawBtn, removeBtn), UI.rowRight(summary)), BorderLayout.NORTH);
        content.add(sp, BorderLayout.CENTER);
        content.add(addCard, BorderLayout.SOUTH);
        setContent(content);

        ModernButton close = UI.secondary("Close", null);
        close.addActionListener(e -> dispose());
        addButton(close);

        enrollBtn.addActionListener(e -> enroll());
        withdrawBtn.addActionListener(e -> toggleWithdraw());
        removeBtn.addActionListener(e -> remove());
        yearFilter.addActionListener(e -> reload());
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        reload();
    }

    private Enrollment selected() {
        return model.getItem(UI.selectedModelRow(table));
    }

    private void reload() {
        try {
            boolean onlyThisYear = yearFilter.getSelectedIndex() == 0;
            model.clear();
            int enrolled = 0;
            int withdrawn = 0;
            List<Enrollment> list = dao.findByCourse(course.getCourseId());
            for (Enrollment e : list) {
                if (onlyThisYear && !AppConfig.CURRENT_ACADEMIC_YEAR.equals(e.getAcademicYear())) continue;
                model.addRow(e, e.getTgNo(), e.getStudentName(), e.getStudentType().toString(), e.getAcademicYear(),
                        e.getSemester(), e.getStatusLabel());
                if (e.isWithdrawn()) withdrawn++; else enrolled++;
            }
            model.refresh();
            summary.setText(enrolled + " enrolled" + (withdrawn > 0 ? "  ·  " + withdrawn + " withdrawn" : ""));
            studentCombo.removeAllItems();
            for (Undergraduate u : dao.findNotEnrolled(course.getCourseId())) studentCombo.addItem(u);
            updateButtons();
        } catch (AppException e) {
            UI.error(this, e);
        }
    }

    private void updateButtons() {
        Enrollment e = selected();
        withdrawBtn.setEnabled(e != null && !"COMPLETED".equals(e.getStatus()));
        removeBtn.setEnabled(e != null);
        withdrawBtn.setText(e != null && e.isWithdrawn() ? "Re-enroll" : "Withdraw");
    }

    private void enroll() {
        Undergraduate s = (Undergraduate) studentCombo.getSelectedItem();
        if (s == null) {
            UI.warn(this, "All undergraduates are already enrolled in this course.");
            return;
        }
        try {
            dao.enroll(s, course);
            reload();
        } catch (AppException ex) {
            UI.warn(this, ex.getUserMessage());
        }
    }

    private void toggleWithdraw() {
        Enrollment e = selected();
        if (e == null) return;
        String newStatus = e.isWithdrawn() ? "ENROLLED" : "WITHDRAWN";
        String question = e.isWithdrawn() ? "Enroll " + e.getTgNo() + " again?"
                : "Withdraw " + e.getTgNo() + " (" + e.getStudentName() + ") from " + course.getCourseCode() + "?\n"
                + "The student will no longer appear in attendance and marks lists of this course.";
        if (!UI.confirm(this, question)) return;
        try {
            dao.setStatus(e.getEnrollmentId(), newStatus);
            reload();
        } catch (AppException ex) {
            UI.error(this, ex);
        }
    }

    private void remove() {
        Enrollment e = selected();
        if (e == null) return;
        if (!UI.confirm(this, "Remove the enrolment record of " + e.getTgNo() + " (" + e.getAcademicYear() + ")?\n"
                + "Use 'Withdraw' instead if you want to keep the history.")) return;
        try {
            dao.delete(e.getEnrollmentId());
            reload();
        } catch (AppException ex) {
            UI.error(this, ex);
        }
    }
}
