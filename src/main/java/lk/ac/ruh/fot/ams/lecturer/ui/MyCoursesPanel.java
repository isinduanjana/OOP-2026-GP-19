package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import lk.ac.ruh.fot.ams.lecturer.dao.CourseMaterialDAO;
import lk.ac.ruh.fot.ams.lecturer.dao.LecturerCourseDAO;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.CourseMaterial;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.FileUtil;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M2 - LECTURER MODULE] "My Courses &amp; Materials" page - "Can modify and add materials to courses".
 *   top    : table of my courses (common CourseDAO.findByLecturer)
 *   bottom : details of the selected course (+ Edit description) and its materials
 *            with Add / Edit / Delete / Open (CourseMaterialDAO + common FileUtil).
 *
 * OOP: Inheritance (LecturerPage -> BasePanel), Composition (uses DAO objects and
 * MaterialDialog), Exception handling via safe().
 */
public class MyCoursesPanel extends LecturerPage {

    private final CourseMaterialDAO materialDAO = new CourseMaterialDAO();
    private final LecturerCourseDAO lecturerCourseDAO = new LecturerCourseDAO();

    private final DataTableModel courseModel = new DataTableModel("Code", "Course name", "Credits", "Level / Sem",
            "Components", "Students", "Lecturers");
    private final JTable courseTable = UI.table(courseModel);
    private final DataTableModel materialModel = new DataTableModel("Title", "Covers", "File", "Uploaded",
            "By", "On disk");
    private final JTable materialTable = UI.table(materialModel);

    private final JPanel detailsGrid = new JPanel(new GridBagLayout());
    private final JTextArea description = new JTextArea(3, 20);
    private final CardPanel detailsCard = new CardPanel();
    private final javax.swing.JLabel detailsTitle = UI.heading("Course details");
    private final CardPanel materialsCard = new CardPanel();
    private final javax.swing.JLabel materialsTitle = UI.heading("Materials");

    public MyCoursesPanel(User user, Color accent) {
        super(user, accent);
        UI.setColumnWidths(courseTable, 80, 280, 60, 90, 140, 70, 260);
        CardPanel coursesCard = CardPanel.titled("Courses I teach");
        coursesCard.add(UI.scroll(courseTable), BorderLayout.CENTER);
        coursesCard.setPreferredSize(new Dimension(400, 250));

        // ---- details
        detailsGrid.setOpaque(false);
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setOpaque(false);
        description.setFont(Theme.FONT);
        description.setForeground(Theme.TEXT);
        description.setBorder(BorderFactory.createEmptyBorder());
        JPanel det = new JPanel(new BorderLayout(0, 10));
        det.setOpaque(false);
        det.add(detailsGrid, BorderLayout.NORTH);
        JScrollPane dsp = new JScrollPane(description);
        dsp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        dsp.getViewport().setBackground(Theme.TABLE_STRIPE);
        det.add(dsp, BorderLayout.CENTER);
        ModernButton editDesc = UI.secondary("Edit description", "edit");
        editDesc.addActionListener(e -> editDescription());
        detailsCard.add(detailsTitle, BorderLayout.NORTH);
        detailsCard.add(det, BorderLayout.CENTER);
        detailsCard.add(UI.rowRight(editDesc), BorderLayout.SOUTH);
        detailsCard.setPreferredSize(new Dimension(460, 330));

        // ---- materials
        UI.setColumnWidths(materialTable, 150, 120, 110, 95, 90, 55);
        materialTable.getColumnModel().getColumn(1).setMinWidth(118);   // "Covers" dates always fully visible
        ModernButton add = accentButton("Add material", "plus");
        add.addActionListener(e -> addMaterial());
        ModernButton edit = UI.secondary("Edit", "edit");
        edit.addActionListener(e -> editMaterial());
        ModernButton del = UI.danger("Delete", "trash");
        del.addActionListener(e -> deleteMaterial());
        ModernButton open = UI.secondary("Open", "eye");
        open.addActionListener(e -> openMaterial());
        materialsCard.add(materialsTitle, BorderLayout.NORTH);
        materialsCard.add(UI.scroll(materialTable), BorderLayout.CENTER);
        materialsCard.add(UI.rowRight(open, edit, del, add), BorderLayout.SOUTH);
        materialTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openMaterial();
            }
        });

        JPanel bottom = new JPanel(new BorderLayout(14, 0));
        bottom.setOpaque(false);
        bottom.add(detailsCard, BorderLayout.WEST);
        bottom.add(materialsCard, BorderLayout.CENTER);
        bottom.setPreferredSize(new Dimension(400, 330));

        add(coursesCard, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);

        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelected();
        });
    }

    @Override
    public String getTitle() {
        return "My Courses & Materials";
    }

    @Override
    public String getSubtitle() {
        return "Course details, description and learning materials of the courses you teach";
    }

    @Override
    public void refresh() {
        Course before = selectedCourse();
        safe(() -> {
            List<Course> courses = myCourses();
            Map<Integer, Integer> counts = lecturerCourseDAO.studentCounts(lecturerId());
            courseModel.clear();
            for (Course c : courses) {
                courseModel.addRow(c, c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getLevelSemesterLabel(),
                        c.getComponentLabel(), counts.getOrDefault(c.getCourseId(), 0),
                        Format.nullToDash(c.getLecturerNames()));
            }
            courseModel.refresh();
            int select = 0;
            for (int i = 0; i < courses.size(); i++) {
                if (courses.get(i).equals(before)) select = i;
            }
            if (!courses.isEmpty()) {
                int view = courseTable.convertRowIndexToView(select);
                courseTable.setRowSelectionInterval(view, view);
            }
        });
        showSelected();
    }

    private Course selectedCourse() {
        return courseModel.getItem(UI.selectedModelRow(courseTable));
    }

    /** Fills the details card and the materials table for the selected course. */
    private void showSelected() {
        Course c = selectedCourse();
        detailsGrid.removeAll();
        materialModel.clear();
        if (c == null) {
            description.setText("Select a course above.");
            detailsTitle.setText("Course details");
        } else {
            GridBagConstraints g = new GridBagConstraints();
            g.anchor = GridBagConstraints.WEST;
            g.insets = new Insets(3, 0, 3, 14);
            String[][] rows = {
                    {"Course", c.getCourseName()},
                    {"Credits", c.getCredits() + " credits   •   Level " + c.getLevel() + " Semester " + c.getSemester()},
                    {"Hours / session", "Theory " + c.getTheoryHours() + " h  •  Practical " + c.getPracticalHours() + " h"},
                    {"Department", Format.nullToDash(c.getDepartmentName())},
                    {"Lecturers", Format.nullToDash(c.getLecturerNames())}};
            for (int i = 0; i < rows.length; i++) {
                g.gridy = i;
                g.gridx = 0;
                g.weightx = 0;
                g.fill = GridBagConstraints.NONE;
                detailsGrid.add(UI.muted(rows[i][0]), g);
                g.gridx = 1;
                g.weightx = 1;
                g.fill = GridBagConstraints.HORIZONTAL;
                javax.swing.JLabel v = UI.label(rows[i][1]);
                v.setToolTipText(rows[i][1]);
                v.setMinimumSize(new Dimension(10, v.getPreferredSize().height));
                v.setPreferredSize(new Dimension(10, v.getPreferredSize().height));
                detailsGrid.add(v, g);
            }
            detailsTitle.setText("Course details - " + c.getCourseCode());
            description.setText(c.getDescription() == null || c.getDescription().isBlank()
                    ? "No description yet." : c.getDescription());
            description.setCaretPosition(0);
            safe(() -> {
                for (CourseMaterial m : materialDAO.findByCourse(c.getCourseId())) {
                    materialModel.addRow(m, m.getTitle(), m.getCoverageShort(), MaterialDialog.shortName(m.getFilePath()),
                            Format.dateTime(m.getUploadedAt()), Format.nullToDash(m.getLecturerName()),
                            FileUtil.exists(m.getFilePath()) ? "Yes" : "Missing");
                }
            });
        }
        materialModel.refresh();
        materialsTitle.setText(
                c == null ? "Materials" : "Materials of " + c.getCourseCode() + " (" + materialModel.getRowCount() + ")");
        detailsGrid.revalidate();
        detailsGrid.repaint();
    }

    private CourseMaterial selectedMaterial() {
        CourseMaterial m = materialModel.getItem(UI.selectedModelRow(materialTable));
        if (m == null) UI.warn(this, "Select a material first.");
        return m;
    }

    private void addMaterial() {
        Course c = selectedCourse();
        if (c == null) {
            UI.warn(this, "Select a course first.");
            return;
        }
        CourseMaterial m = new MaterialDialog(this, c, null, lecturerId()).showDialog();
        if (m != null && safe(() -> materialDAO.insert(m))) {
            UI.info(this, "Material \"" + m.getTitle() + "\" added to " + c.getCourseCode() + ".");
            showSelected();
        }
    }

    private void editMaterial() {
        Course c = selectedCourse();
        CourseMaterial m = selectedMaterial();
        if (c == null || m == null) return;
        CourseMaterial edited = new MaterialDialog(this, c, m, lecturerId()).showDialog();
        if (edited != null && safe(() -> materialDAO.update(edited))) {
            showSelected();
        }
    }

    private void deleteMaterial() {
        CourseMaterial m = selectedMaterial();
        if (m == null) return;
        if (UI.confirm(this, "Delete the material \"" + m.getTitle() + "\"?")
                && safe(() -> materialDAO.delete(m.getMaterialId()))) {
            showSelected();
        }
    }

    private void openMaterial() {
        CourseMaterial m = selectedMaterial();
        if (m != null) safe(() -> FileUtil.open(m.getFilePath()));
    }

    /** Lecturer may change the course description (handout: modify courses). */
    private void editDescription() {
        Course c = selectedCourse();
        if (c == null) return;
        JTextArea area = UI.textArea(8);
        area.setText(c.getDescription() == null ? "" : c.getDescription());
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(460, 180));
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.add(UI.muted("Description of " + c + " shown to students"), BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, p, "Edit course description", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        String text = area.getText().trim();
        if (safe(() -> lecturerCourseDAO.updateDescription(c.getCourseId(), lecturerId(), text))) {
            UI.info(this, "Description of " + c.getCourseCode() + " updated.");
            refresh();
        }
    }
}
