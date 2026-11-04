package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lk.ac.ruh.fot.ams.dao.StudentDAO;
import lk.ac.ruh.fot.ams.lecturer.dao.LecturerCourseDAO;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.StudentType;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M2 - LECTURER MODULE] "Undergraduates" page - "Can see undergraduate details".
 * Shows students of my courses (or all undergraduates), with search; double-click
 * opens StudentDetailsDialog.
 *
 * OOP: Inheritance (LecturerPage -> BasePanel), Encapsulation, reuse of the common
 * StudentDAO and of the polymorphic UserDAO.mapUser (rows come back as Undergraduate objects).
 */
public class UndergraduatesPanel extends LecturerPage {

    private static final String MINE = "Students in my courses";
    private static final String ALL = "All undergraduates";

    private final StudentDAO studentDAO = new StudentDAO();
    private final LecturerCourseDAO lecturerCourseDAO = new LecturerCourseDAO();
    private final JComboBox<String> scopeBox = new JComboBox<>(new String[]{MINE, ALL});
    private final JTextField search = UI.searchField("Search by TG number, name, email...");
    private final JLabel count = UI.muted("");
    private final DataTableModel model = new DataTableModel("TG No", "Name", "Batch", "Level / Sem", "Type",
            "Email", "Phone", "Department", "Status");
    private final JTable table = UI.table(model);

    private final StatCard totalCard;
    private final StatCard normalCard;
    private final StatCard repeatCard;
    private final StatCard missedCard;

    public UndergraduatesPanel(User user, Color accent) {
        super(user, accent);
        totalCard = new StatCard("Undergraduates", "-", "users", accent);
        normalCard = new StatCard("Normal", "-", "user", Theme.SUCCESS);
        repeatCard = new StatCard("Repeat", "-", "refresh", Theme.WARNING);
        missedCard = new StatCard("Batch missed", "-", "calendar", Theme.INFO);

        scopeBox.setPreferredSize(new Dimension(220, 34));
        search.setPreferredSize(new Dimension(300, 36));
        ModernButton view = accentButton("View details", "eye");
        view.addActionListener(e -> openDetails());


        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openDetails();
            }
        });
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        scopeBox.addActionListener(e -> refresh());

        CardPanel tableCard = new CardPanel(new BorderLayout(0, 12));
        tableCard.add(UI.toolbar(UI.row(UI.label("Show"), scopeBox, UI.label("Search"), search),
                UI.rowRight(count, view)), BorderLayout.NORTH);
        tableCard.add(fitOrScroll(table, 115, 160, 55, 70, 95, 190, 100, 150, 65), BorderLayout.CENTER);
        tableCard.add(UI.muted("Tip: double-click a student to see the full profile and enrolled courses."),
                BorderLayout.SOUTH);
        tableCard.setPreferredSize(new Dimension(400, 420));

        add(statRow(totalCard, normalCard, repeatCard, missedCard), BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "Undergraduates";
    }

    @Override
    public String getSubtitle() {
        return "Profiles of undergraduates (double-click for details and enrolled courses)";
    }

    @Override
    public void refresh() {
        safe(() -> {
            List<Undergraduate> list = ALL.equals(scopeBox.getSelectedItem())
                    ? studentDAO.findAll() : lecturerCourseDAO.findMyStudents(lecturerId());
            model.clear();
            int normal = 0;
            int repeat = 0;
            int missed = 0;
            for (Undergraduate s : list) {
                model.addRow(s, s.getTgNo(), s.getFullName(), s.getBatch(), "L" + s.getLevel() + " S" + s.getSemester(),
                        s.getStudentType().toString(), Format.nullToDash(s.getEmail()), Format.nullToDash(s.getPhone()),
                        Format.nullToDash(s.getDepartmentName()), s.isActive() ? "Active" : "Inactive");
                if (s.getStudentType() == StudentType.REPEAT) repeat++;
                else if (s.getStudentType() == StudentType.BATCH_MISSED) missed++;
                else normal++;
            }
            model.refresh();
            totalCard.setValue(String.valueOf(list.size()));
            totalCard.setCaption(scopeBox.getSelectedItem().toString().toLowerCase());
            normalCard.setValue(String.valueOf(normal));
            normalCard.setCaption("regular students");
            repeatCard.setValue(String.valueOf(repeat));
            repeatCard.setCaption("repeating courses");
            missedCard.setValue(String.valueOf(missed));
            missedCard.setCaption("joined a later batch");
            applyFilter();
        });
    }

    private void applyFilter() {
        UI.filter(table, search.getText());
        count.setText(table.getRowCount() + " shown");
    }

    private void openDetails() {
        Undergraduate s = model.getItem(UI.selectedModelRow(table));
        if (s == null) {
            UI.warn(this, "Select a student first.");
            return;
        }
        StudentDetailsDialog d = new StudentDetailsDialog(this, s, accent);
        if (d.load()) d.setVisible(true);
    }
}
