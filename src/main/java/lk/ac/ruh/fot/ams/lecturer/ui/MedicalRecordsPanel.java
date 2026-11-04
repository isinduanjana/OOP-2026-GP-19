package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lk.ac.ruh.fot.ams.dao.MedicalDAO;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.FileUtil;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M2 - LECTURER MODULE] "Medical Records" page - "Can see attendance and medical records
 * of undergraduates". READ-ONLY for the lecturer (approval is done by the TO in M3).
 * Data comes from the common MedicalDAO.findByCourse for each of my courses.
 *
 * OOP: Inheritance (LecturerPage -> BasePanel), Encapsulation, use of enums (MedicalStatus).
 */
public class MedicalRecordsPanel extends LecturerPage {

    private static final String ALL_COURSES = "All my courses";
    private static final String ALL_STATUS = "All statuses";

    private final MedicalDAO medicalDAO = new MedicalDAO();
    private final JComboBox<Object> courseBox = new JComboBox<>();
    private final JComboBox<Object> statusBox = new JComboBox<>(new Object[]{ALL_STATUS,
            MedicalStatus.APPROVED, MedicalStatus.PENDING, MedicalStatus.REJECTED});
    private final JTextField search = UI.searchField("Search TG number / name / reason...");
    private final JLabel count = UI.muted("");
    private final DataTableModel model = new DataTableModel("TG No", "Name", "Course", "Date", "Reason", "Status",
            "Approved by", "Approved on", "Document");
    private final JTable table = UI.table(model);

    private final StatCard totalCard;
    private final StatCard approvedCard;
    private final StatCard pendingCard;
    private final StatCard rejectedCard;
    private boolean loading;

    public MedicalRecordsPanel(User user, Color accent) {
        super(user, accent);
        totalCard = new StatCard("Medical records", "-", "medical", accent);
        approvedCard = new StatCard("Approved", "-", "check", Theme.SUCCESS);
        pendingCard = new StatCard("Pending", "-", "clipboard", Theme.WARNING);
        rejectedCard = new StatCard("Rejected", "-", "trash", Theme.DANGER);

        courseBox.setPreferredSize(new Dimension(300, 34));
        statusBox.setPreferredSize(new Dimension(150, 34));
        search.setPreferredSize(new Dimension(220, 36));
        courseBox.addActionListener(e -> { if (!loading) load(); });
        statusBox.addActionListener(e -> { if (!loading) load(); });
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });
        ModernButton open = UI.secondary("Open document", "file");
        open.addActionListener(e -> openDocument());


        CardPanel card = new CardPanel(new BorderLayout(0, 12));
        card.add(UI.toolbar(UI.row(UI.label("Course"), courseBox, UI.label("Status"), statusBox,
                UI.label("Search"), search), UI.rowRight(count)), BorderLayout.NORTH);
        card.add(fitOrScroll(table, 115, 145, 80, 95, 160, 85, 135, 95, 70), BorderLayout.CENTER);
        card.add(UI.toolbar(UI.muted("Read-only: medicals are recorded and approved by the Technical Officer. "
                + "\"Whole day\" = covers every session on that date."), UI.rowRight(open)), BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(400, 420));

        add(statRow(totalCard, approvedCard, pendingCard, rejectedCard), BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "Medical Records";
    }

    @Override
    public String getSubtitle() {
        return "Medical submissions of students in your courses (read-only)";
    }

    @Override
    public void refresh() {
        safe(() -> {
            loading = true;
            List<Object> items = new ArrayList<>();
            items.add(ALL_COURSES);
            items.addAll(myCourses());
            fillCombo(courseBox, items);
            loading = false;
        });
        loading = false;
        load();
    }

    private void load() {
        safe(() -> {
            List<Course> courses = new ArrayList<>();
            Object sel = courseBox.getSelectedItem();
            if (sel instanceof Course c) courses.add(c);
            else courses.addAll(myCourses());
            // merge the medicals of the chosen courses (a whole-day medical can appear for several courses)
            Map<Integer, Medical> merged = new LinkedHashMap<>();
            for (Course c : courses) {
                for (Medical m : medicalDAO.findByCourse(c.getCourseId())) merged.putIfAbsent(m.getMedicalId(), m);
            }
            List<Medical> list = new ArrayList<>(merged.values());
            list.sort((a, b) -> b.getMedicalDate().compareTo(a.getMedicalDate()));

            Object st = statusBox.getSelectedItem();
            model.clear();
            int approved = 0;
            int pending = 0;
            int rejected = 0;
            for (Medical m : list) {
                switch (m.getStatus()) {
                    case APPROVED -> approved++;
                    case PENDING -> pending++;
                    default -> rejected++;
                }
                if (st instanceof MedicalStatus ms && m.getStatus() != ms) continue;
                model.addRow(m, m.getTgNo(), m.getStudentName(),
                        m.getCourseCode() == null ? "Whole day" : m.getCourseCode(), Format.date(m.getMedicalDate()),
                        Format.nullToDash(m.getReason()), statusText(m.getStatus()),
                        Format.nullToDash(m.getApprovedByName()),
                        Format.date(m.getApprovedAt() == null ? null : m.getApprovedAt().toLocalDate()),
                        m.getDocumentPath() == null ? "-" : "Attached");
            }
            model.refresh();
            totalCard.setValue(String.valueOf(list.size()));
            totalCard.setCaption(sel instanceof Course c ? c.getCourseCode() : "all my courses");
            approvedCard.setValue(String.valueOf(approved));
            approvedCard.setCaption("counted as attended");
            pendingCard.setValue(String.valueOf(pending));
            pendingCard.setCaption("waiting for the TO");
            rejectedCard.setValue(String.valueOf(rejected));
            rejectedCard.setCaption("not accepted");
            filter();
        });
    }

    private void filter() {
        UI.filter(table, search.getText());
        count.setText(table.getRowCount() + " record(s)");
    }

    private static String statusText(MedicalStatus s) {
        return s.name().charAt(0) + s.name().substring(1).toLowerCase();   // "Approved" -> auto-coloured
    }

    private void openDocument() {
        Medical m = model.getItem(UI.selectedModelRow(table));
        if (m == null) {
            UI.warn(this, "Select a medical record first.");
            return;
        }
        safe(() -> FileUtil.open(m.getDocumentPath()));
    }
}
