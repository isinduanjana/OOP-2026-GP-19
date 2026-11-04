package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.Avatar;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;


public class StudentDetailsDialog extends JDialog {

    private final Undergraduate student;
    private final DataTableModel courses = new DataTableModel("Code", "Course", "Credits", "Level / Sem", "Lecturers");
    private final CourseDAO courseDAO = new CourseDAO();

    public StudentDetailsDialog(Component parent, Undergraduate s, Color accent) {
        super(SwingUtilities.getWindowAncestor(parent), "Undergraduate - " + s.getTgNo(), ModalityType.APPLICATION_MODAL);
        this.student = s;

        Avatar av = new Avatar(84);
        av.setUser(s.getInitials(), s.getProfilePicture(), accent);
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        javax.swing.JComponent[] parts = {
                UI.title(s.getFullName()),
                UI.muted(s.getTgNo() + "   •   username " + s.getUsername()),
                badges(s, accent)};
        for (javax.swing.JComponent c : parts) {
            c.setAlignmentX(LEFT_ALIGNMENT);       // BoxLayout: keep every line left-aligned
            names.add(c);
            names.add(Box.createVerticalStrut(6));
        }
        head.add(av, BorderLayout.WEST);
        head.add(names, BorderLayout.CENTER);

        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        String[][] rows = {
                {"Batch", s.getBatch()}, {"Level / Semester", "Level " + s.getLevel() + "  Semester " + s.getSemester()},
                {"Department", Format.nullToDash(s.getDepartmentName())}, {"Email", Format.nullToDash(s.getEmail())},
                {"Phone", Format.nullToDash(s.getPhone())}, {"Address", Format.nullToDash(s.getAddress())}};
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 0, 4, 18);
        for (int i = 0; i < rows.length; i++) {
            g.gridx = (i % 2) * 2;
            g.gridy = i / 2;
            g.weightx = 0;
            info.add(UI.muted(rows[i][0]), g);
            g.gridx++;
            g.weightx = 1;
            info.add(UI.label(rows[i][1]), g);
        }

        JTable table = UI.table(courses);
        UI.setColumnWidths(table, 80, 260, 60, 80, 220);
        JPanel coursesBox = new JPanel(new BorderLayout(0, 8));
        coursesBox.setOpaque(false);
        coursesBox.add(UI.heading("Enrolled courses (" + lk.ac.ruh.fot.ams.config.AppConfig.CURRENT_ACADEMIC_YEAR + ")"),
                BorderLayout.NORTH);
        coursesBox.add(UI.scroll(table), BorderLayout.CENTER);
        coursesBox.setPreferredSize(new Dimension(720, 230));

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(info, BorderLayout.NORTH);
        center.add(coursesBox, BorderLayout.CENTER);

        ModernButton close = UI.secondary("Close", null);
        close.addActionListener(e -> dispose());

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(22, 22, 18, 22));
        root.add(head, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(UI.rowRight(close), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private static JPanel badges(Undergraduate s, Color accent) {
        JPanel p = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(UI.badge(s.getStudentType().toString(), accent));
        p.add(Box.createHorizontalStrut(8));
        p.add(UI.badge(s.isActive() ? "Active" : "Inactive", s.isActive() ? Theme.SUCCESS : Theme.DANGER));
        return p;
    }

    /** Loads the enrolled courses. @return false if the DB call failed (an error was shown). */
    public boolean load() {
        try {
            courses.clear();
            for (Course c : courseDAO.findByStudent(student.getStudentId())) {
                courses.addRow(c, c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getLevelSemesterLabel(),
                        Format.nullToDash(c.getLecturerNames()));
            }
            courses.refresh();
            pack();
            setLocationRelativeTo(getOwner());
            return true;
        } catch (AppException e) {
            UI.error(getOwner(), e);
            return false;
        }
    }
}
