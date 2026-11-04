package lk.ac.ruh.fot.ams.undergraduate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Assessment;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.CourseMaterial;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.dao.AssessmentDAO;
import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M4 - UNDERGRADUATE MODULE] "My Courses" - the courses the student follows
 * this academic year. Selecting a course shows its description, assessment
 * scheme (weights) and the course materials uploaded by lecturers.
 *
 * Data comes from the COMMON DAOs: CourseDAO.findByStudent / findMaterials and
 * AssessmentDAO.findByCourse (read-only for students).
 *
 * OOP: Inheritance (extends StudentViewPanel), Polymorphism (own
 * loadStudentData()), Encapsulation (private table models + DAOs).
 */
public class StudentCoursesPanel extends StudentViewPanel {

    private final CourseDAO courseDAO = new CourseDAO();
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();

    private final StatCard coursesCard;
    private final StatCard creditsCard;
    private final StatCard materialsCard;

    private final DataTableModel courseModel = new DataTableModel("Code", "Course name", "Credits", "Components",
            "Lecturer(s)", "Level / Sem", "Department");
    private final JTable courseTable = UI.table(courseModel);

    private final DataTableModel schemeModel = new DataTableModel("Assessment", "Type", "Weight %", "Part");
    private final DataTableModel materialModel = new DataTableModel("Title", "Uploaded by", "Uploaded", "File");
    private final JTable materialTable = UI.table(materialModel);

    private final JLabel detailTitle = UI.heading("Select a course");
    private final JLabel detailMeta = UI.muted("Choose a course in the list above to see its details.");
    private final JPanel descriptionHolder = StudentUI.clear(0, 0);
    private final JLabel schemeTotal = UI.muted("");
    private final ModernButton openButton;

    public StudentCoursesPanel(User user, Color accent) {
        super(user, accent);
        coursesCard = new StatCard("Courses", "-", "book", accent);
        creditsCard = new StatCard("Credits", "-", "award", Theme.INFO);
        materialsCard = new StatCard("Materials", "-", "file", Theme.SUCCESS);
        openButton = accentButton("Open", "eye");
        setMinimumPageHeight(1010);     // scroll instead of squeezing the tables
        buildUI();
    }

    @Override
    public String getTitle() {
        return "My Courses";
    }

    @Override
    public String getSubtitle() {
        return "Courses you follow this academic year, their assessment scheme and materials";
    }

    private void buildUI() {
        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(coursesCard);
        stats.add(creditsCard);
        stats.add(materialsCard);

        CardPanel listCard = CardPanel.titled("Enrolled courses");
        UI.setColumnWidths(courseTable, 80, 230, 60, 130, 220, 80, 170);
        courseTable.setRowHeight(36);
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelected();
        });
        javax.swing.JScrollPane listScroll = UI.scroll(courseTable);
        listScroll.setPreferredSize(new Dimension(400, 36 * 6 + 42));   // room for 6 courses
        listCard.add(listScroll, BorderLayout.CENTER);

        // -------- details: description + scheme | materials
        CardPanel detailCard = new CardPanel(new BorderLayout(0, 12));
        JPanel head = StudentUI.clear(0, 2);
        head.add(detailTitle, BorderLayout.NORTH);
        head.add(detailMeta, BorderLayout.SOUTH);
        detailCard.add(head, BorderLayout.NORTH);

        JPanel left = StudentUI.clear(0, 14);
        JPanel descBlock = StudentUI.clear(0, 6);
        descBlock.add(UI.label("Description"), BorderLayout.NORTH);
        descBlock.add(descriptionHolder, BorderLayout.CENTER);
        left.add(descBlock, BorderLayout.NORTH);
        JPanel scheme = StudentUI.clear(0, 8);
        JTable schemeTable = UI.table(schemeModel);
        UI.setColumnWidths(schemeTable, 150, 100, 70, 90);
        javax.swing.JScrollPane schemeScroll = UI.scroll(schemeTable);
        schemeScroll.setPreferredSize(new Dimension(360, 34 * 5 + 42));
        scheme.add(UI.toolbar(UI.label("Assessment scheme"), schemeTotal), BorderLayout.NORTH);
        scheme.add(schemeScroll, BorderLayout.CENTER);
        left.add(scheme, BorderLayout.CENTER);

        JPanel right = StudentUI.clear(0, 8);
        UI.setColumnWidths(materialTable, 230, 140, 110, 160);
        materialTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openMaterial();
            }
        });
        openButton.addActionListener(e -> openMaterial());
        right.add(UI.toolbar(UI.label("Course materials"), UI.rowRight(openButton)), BorderLayout.NORTH);
        javax.swing.JScrollPane matScroll = UI.scroll(materialTable);
        matScroll.setPreferredSize(new Dimension(360, 240));
        right.add(matScroll, BorderLayout.CENTER);
        right.add(UI.muted("Tip: double-click a material to open it."), BorderLayout.SOUTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0;
        c.weightx = 0.45;
        c.insets = new Insets(0, 0, 0, 18);
        body.add(left, c);
        c.gridx = 1;
        c.weightx = 0.55;
        c.insets = new Insets(0, 0, 0, 0);
        body.add(right, c);
        detailCard.add(body, BorderLayout.CENTER);

        JPanel north = StudentUI.clear(0, 14);
        north.add(stats, BorderLayout.NORTH);
        north.add(listCard, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);
        add(detailCard, BorderLayout.CENTER);
    }

    @Override
    protected void loadStudentData() throws AppException {
        Course previous = courseModel.getItem(UI.selectedModelRow(courseTable));
        List<Course> courses = courseDAO.findByStudent(getStudentId());
        courseModel.clear();
        int credits = 0;
        int materials = 0;
        for (Course c : courses) {
            courseModel.addRow(c, c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getComponentLabel(),
                    Format.nullToDash(c.getLecturerNames()), c.getLevelSemesterLabel(),
                    Format.nullToDash(c.getDepartmentName()));
            credits += c.getCredits();
            materials += courseDAO.findMaterials(c.getCourseId()).size();
        }
        courseModel.refresh();
        coursesCard.setValue(String.valueOf(courses.size()));
        coursesCard.setCaption("enrolled this year");
        creditsCard.setValue(String.valueOf(credits));
        creditsCard.setCaption("total credits");
        materialsCard.setValue(String.valueOf(materials));
        materialsCard.setCaption("materials available");

        // keep the previous selection, otherwise select the first course
        int row = 0;
        for (int i = 0; previous != null && i < courses.size(); i++) {
            if (courses.get(i).equals(previous)) row = i;
        }
        courseTable.clearSelection();
        if (!courses.isEmpty()) {
            int view = courseTable.convertRowIndexToView(row);
            courseTable.setRowSelectionInterval(view, view);   // -> selection listener calls showSelected()
        } else {
            showSelected();
        }
    }

    /** Fills the detail card for the selected course. */
    private void showSelected() {
        Course c = courseModel.getItem(UI.selectedModelRow(courseTable));
        schemeModel.clear();
        materialModel.clear();
        descriptionHolder.removeAll();
        if (c == null) {
            detailTitle.setText("Select a course");
            detailMeta.setText("Choose a course in the list above to see its details.");
            schemeTotal.setText("");
            openButton.setEnabled(false);
            descriptionHolder.revalidate();
            return;
        }
        detailTitle.setText(c.getCourseCode() + "  -  " + c.getCourseName());
        detailMeta.setText(c.getCredits() + " credits  •  " + c.getComponentLabel()
                + (c.hasTheory() ? "  •  theory " + c.getTheoryHours() + "h/session" : "")
                + (c.hasPractical() ? "  •  practical " + c.getPracticalHours() + "h/session" : "")
                + "  •  Lecturer(s): " + Format.nullToDash(c.getLecturerNames()));
        descriptionHolder.add(StudentUI.text(c.getDescription()), BorderLayout.CENTER);
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chips.add(UI.badge(c.getLevelSemesterLabel(), accent));
        if (c.hasTheory()) chips.add(UI.badge("Theory", Theme.INFO));
        if (c.hasPractical()) chips.add(UI.badge("Practical", Theme.TO_ACCENT));
        chips.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        descriptionHolder.add(chips, BorderLayout.SOUTH);
        descriptionHolder.revalidate();
        descriptionHolder.repaint();

        safe(() -> {
            double ca = 0;
            double total = 0;
            for (Assessment a : assessmentDAO.findByCourse(c.getCourseId())) {
                boolean isCa = a.getType().isContinuousAssessment();
                schemeModel.addRow(a, a.getName(), a.getType().toString(), a.getPercentage(),
                        isCa ? "CA" : "Final");
                total += a.getPercentage();
                if (isCa) ca += a.getPercentage();
            }
            schemeModel.refresh();
            schemeTotal.setText(schemeModel.getRowCount() == 0 ? "Not published yet"
                    : "CA " + Format.num(ca) + "%  +  Final " + Format.num(total - ca) + "%");

            for (CourseMaterial m : courseDAO.findMaterials(c.getCourseId())) {
                materialModel.addRow(m, m.getTitle(), Format.nullToDash(m.getLecturerName()),
                        Format.dateTime(m.getUploadedAt()), StudentUI.fileName(m.getFilePath()));
            }
            materialModel.refresh();
            openButton.setEnabled(materialModel.getRowCount() > 0);
        });
    }

    private void openMaterial() {
        CourseMaterial m = materialModel.getItem(UI.selectedModelRow(materialTable));
        if (m == null) {
            UI.warn(this, "Please select a course material first.");
            return;
        }
        StudentUI.openFile(this, m.getFilePath(), "course material");
    }
}
