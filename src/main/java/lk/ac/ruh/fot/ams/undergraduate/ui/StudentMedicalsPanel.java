package lk.ac.ruh.fot.ams.undergraduate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import lk.ac.ruh.fot.ams.dao.MedicalDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M4 - UNDERGRADUATE MODULE] "My Medicals" - READ-ONLY list of the student's
 * medical submissions and their approval status. (Students cannot add
 * medicals - the Technical Officer records them in M3.)
 *
 * Uses the COMMON MedicalDAO.findByStudent(...).
 *
 * OOP: Inheritance (extends StudentViewPanel), Polymorphism (own
 * loadStudentData()), Encapsulation (private model / cards).
 */
public class StudentMedicalsPanel extends StudentViewPanel {

    private final MedicalDAO medicalDAO = new MedicalDAO();

    private final StatCard totalCard;
    private final StatCard approvedCard;
    private final StatCard pendingCard;
    private final StatCard rejectedCard;

    private final DataTableModel model = new DataTableModel("Date", "Course", "Reason", "Status", "Approved by",
            "Approved at", "Document");
    private final JTable table = UI.table(model);
    private final JLabel countLabel = UI.muted("");

    public StudentMedicalsPanel(User user, Color accent) {
        super(user, accent);
        totalCard = new StatCard("Submitted", "-", "medical", accent);
        approvedCard = new StatCard("Approved", "-", "check", Theme.SUCCESS);
        pendingCard = new StatCard("Pending", "-", "clipboard", Theme.WARNING);
        rejectedCard = new StatCard("Rejected", "-", "trash", Theme.DANGER);
        buildUI();
    }

    @Override
    public String getTitle() {
        return "My Medicals";
    }

    @Override
    public String getSubtitle() {
        return "Medical certificates recorded by the Technical Officer and their approval status";
    }

    private void buildUI() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(totalCard);
        stats.add(approvedCard);
        stats.add(pendingCard);
        stats.add(rejectedCard);

        CardPanel card = new CardPanel(new BorderLayout(0, 12));
        ModernButton open = accentButton("Open document", "file");
        open.addActionListener(e -> openDocument());
        card.add(UI.toolbar(UI.row(UI.heading("Medical history"), countLabel), UI.rowRight(open)), BorderLayout.NORTH);
        UI.setColumnWidths(table, 105, 100, 250, 90, 170, 140, 150);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openDocument();
            }
        });
        JScrollPane sp = UI.scroll(table);
        sp.setPreferredSize(new Dimension(400, 360));
        card.add(sp, BorderLayout.CENTER);

        CardPanel info = new CardPanel(new BorderLayout(12, 0));
        info.add(new JLabel(lk.ac.ruh.fot.ams.ui.components.Icons.get("bell", 22, accent)), BorderLayout.WEST);
        info.add(StudentUI.text("How medicals work: hand over your medical certificate to the Technical Officer of "
                + "your department. Once it is APPROVED, the absent sessions on that date are counted as attended "
                + "for the 80% eligibility rule. Pending or rejected medicals are not counted. "
                + "A medical without a course applies to all your classes on that day."), BorderLayout.CENTER);

        JPanel north = StudentUI.clear(0, 14);
        north.add(stats, BorderLayout.NORTH);
        north.add(info, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    @Override
    protected void loadStudentData() throws AppException {
        List<Medical> list = medicalDAO.findByStudent(getStudentId());
        model.clear();
        int approved = 0;
        int pending = 0;
        int rejected = 0;
        for (Medical m : list) {
            model.addRow(m, Format.date(m.getMedicalDate()),
                    m.getCourseCode() == null ? "All courses" : m.getCourseCode(),
                    Format.nullToDash(m.getReason()), statusText(m.getStatus()),
                    Format.nullToDash(m.getApprovedByName()), Format.dateTime(m.getApprovedAt()),
                    documentText(m.getDocumentPath()));
            switch (m.getStatus()) {
                case APPROVED -> approved++;
                case PENDING -> pending++;
                case REJECTED -> rejected++;
                default -> { }
            }
        }
        model.refresh();
        countLabel.setText(list.isEmpty() ? "No medical records - you have not missed classes due to illness"
                : list.size() + " record(s)");
        totalCard.setValue(String.valueOf(list.size()));
        totalCard.setCaption("medical records");
        approvedCard.setValue(String.valueOf(approved));
        approvedCard.setCaption("counted for attendance");
        pendingCard.setValue(String.valueOf(pending));
        pendingCard.setCaption("waiting for approval");
        rejectedCard.setValue(String.valueOf(rejected));
        rejectedCard.setCaption("not counted");
    }

    /**
     * Short text for the document column, e.g. "PDF document". (The file name itself
     * contains the word "medical", which the common table renderer would colour.)
     */
    private static String documentText(String path) {
        if (path == null || path.isBlank()) return "Not attached";
        String name = StudentUI.fileName(path);
        int dot = name.lastIndexOf('.');
        String ext = dot < 0 ? "" : name.substring(dot + 1).toUpperCase() + " ";
        return ext + "document";
    }

    /** "Approved" / "Pending" / "Rejected" - the common table colours these words. */
    private static String statusText(MedicalStatus s) {
        String n = s.name();
        return n.charAt(0) + n.substring(1).toLowerCase();
    }

    private void openDocument() {
        Medical m = model.getItem(UI.selectedModelRow(table));
        if (m == null) {
            UI.warn(this, "Please select a medical record first.");
            return;
        }
        StudentUI.openFile(this, m.getDocumentPath(), "medical");
    }
}
