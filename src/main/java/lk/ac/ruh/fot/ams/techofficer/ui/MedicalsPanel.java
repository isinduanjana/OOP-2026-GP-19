package lk.ac.ruh.fot.ams.techofficer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lk.ac.ruh.fot.ams.dao.StudentDAO;
import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.techofficer.dao.TOMedicalDAO;
import lk.ac.ruh.fot.ams.techofficer.model.ApproveDecision;
import lk.ac.ruh.fot.ams.techofficer.model.MedicalDecision;
import lk.ac.ruh.fot.ams.techofficer.model.RejectDecision;
import lk.ac.ruh.fot.ams.techofficer.model.ResetDecision;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.FileUtil;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M3 - TECHNICAL OFFICER MODULE] "Add and maintain medical details".
 * Lists every medical with status filter + search; Add / Edit (MedicalDialog),
 * Delete, Approve / Reject / Mark pending, and open the attached document.
 * Every change re-links the medical to the student's absences of that day, so
 * attendance reports know if an absence is covered by an APPROVED medical.
 *
 * Connection to common: extends BasePanel, uses StudentDAO, FileUtil, UI kit;
 * writes through M3's TOMedicalDAO.
 * OOP: Inheritance, Polymorphism (decision buttons are built from a list of
 * MedicalDecision objects - one handler for all of them), Exception handling.
 */
public class MedicalsPanel extends BasePanel {

    private static final String[] FILTERS = {"All", "Pending", "Approved", "Rejected"};

    private final TOMedicalDAO dao = new TOMedicalDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    /** The decisions offered as buttons (polymorphic list). */
    private final List<MedicalDecision> decisions = List.of(new ApproveDecision(), new RejectDecision(), new ResetDecision());
    private final List<ModernButton> decisionButtons = new ArrayList<>();

    private final JComboBox<String> statusFilter = new JComboBox<>(FILTERS);
    private final JTextField search = UI.searchField("Search TG no, name, course, reason");
    private final DataTableModel model = new DataTableModel("TG No", "Name", "Course", "Date", "Reason", "Doc",
            "Status", "Approved by", "Approved at");
    private final JTable table = UI.table(model);

    private final StatCard totalCard;
    private final StatCard pendingCard;
    private final StatCard approvedCard;
    private final StatCard rejectedCard;
    private List<Medical> all = new ArrayList<>();

    public MedicalsPanel(User user, Color accent) {
        super(user, accent);
        totalCard = new StatCard("All medicals", "-", "medical", accent);
        pendingCard = new StatCard("Pending", "-", "clipboard", Theme.WARNING);
        approvedCard = new StatCard("Approved", "-", "check", Theme.SUCCESS);
        rejectedCard = new StatCard("Rejected", "-", "trash", Theme.DANGER);
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(totalCard);
        stats.add(pendingCard);
        stats.add(approvedCard);
        stats.add(rejectedCard);

        statusFilter.setPreferredSize(new Dimension(130, 36));
        statusFilter.addActionListener(e -> render());
        search.setPreferredSize(new Dimension(280, 36));
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { render(); }
            @Override public void removeUpdate(DocumentEvent e) { render(); }
            @Override public void changedUpdate(DocumentEvent e) { render(); }
        });
        ModernButton add = accentButton("Add medical", "plus");
        add.addActionListener(e -> addMedical());

        UI.setColumnWidths(table, 122, 150, 92, 98, 150, 48, 92, 140, 128);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editMedical();
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());

        ModernButton edit = UI.secondary("Edit", "edit");
        edit.addActionListener(e -> editMedical());
        ModernButton delete = UI.danger("Delete", "trash");
        delete.addActionListener(e -> deleteMedical());
        ModernButton open = UI.secondary("Open document", "file");
        open.addActionListener(e -> openDocument());
        for (MedicalDecision d : decisions) {
            ModernButton b = d instanceof ApproveDecision ? UI.success(d.getActionName(), d.getIconName())
                    : d instanceof RejectDecision ? UI.danger(d.getActionName(), d.getIconName())
                    : UI.secondary(d.getActionName(), d.getIconName());
            b.addActionListener(e -> decide(d));
            decisionButtons.add(b);
        }

        CardPanel card = new CardPanel(new BorderLayout(0, 12));
        card.add(UI.toolbar(UI.row(UI.label("Status"), statusFilter, UI.label("Search"), search), UI.rowRight(add)), BorderLayout.NORTH);
        card.add(UI.scroll(table), BorderLayout.CENTER);
        card.add(UI.toolbar(UI.row(edit, delete, open), UI.rowRight(decisionButtons.toArray(new ModernButton[0]))),
                BorderLayout.SOUTH);

        add(stats, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "Medicals";
    }

    @Override
    public String getSubtitle() {
        return "Record medical submissions and approve / reject them - approved medicals cover the absences of that day";
    }

    @Override
    public void refresh() {
        safe(() -> {
            all = dao.findAll();
            int p = 0;
            int a = 0;
            int r = 0;
            for (Medical m : all) {
                switch (m.getStatus()) {
                    case PENDING -> p++;
                    case APPROVED -> a++;
                    case REJECTED -> r++;
                }
            }
            totalCard.setValue(String.valueOf(all.size()));
            pendingCard.setValue(String.valueOf(p));
            approvedCard.setValue(String.valueOf(a));
            rejectedCard.setValue(String.valueOf(r));
            pendingCard.setCaption(p == 0 ? "nothing to review" : "need a decision");
            render();
        });
    }

    /** Applies the status filter + search text to the loaded list. */
    private void render() {
        Medical selected = model.getItem(UI.selectedModelRow(table));
        String f = (String) statusFilter.getSelectedItem();
        String q = search.getText().trim().toLowerCase();
        model.clear();
        for (Medical m : all) {
            if (f != null && !"All".equals(f) && !m.getStatus().name().equalsIgnoreCase(f)) continue;
            String course = m.getCourseCode() == null ? "All courses" : m.getCourseCode();
            String text = (m.getTgNo() + " " + m.getStudentName() + " " + course + " " + m.getReason() + " "
                    + m.getMedicalDate()).toLowerCase();
            if (!q.isEmpty() && !text.contains(q)) continue;
            model.addRow(m, m.getTgNo(), m.getStudentName(), course, Format.date(m.getMedicalDate()),
                    Format.nullToDash(m.getReason()), m.getDocumentPath() == null || m.getDocumentPath().isBlank() ? "-" : "Yes",
                    capital(m.getStatus().name()), Format.nullToDash(m.getApprovedByName()),
                    Format.dateTime(m.getApprovedAt()));
        }
        model.refresh();
        if (selected != null) {
            for (int i = 0; i < model.getRowCount(); i++) {
                Medical m = model.getItem(i);
                if (m.getMedicalId() == selected.getMedicalId()) {
                    int v = table.convertRowIndexToView(i);
                    if (v >= 0) table.setRowSelectionInterval(v, v);
                }
            }
        }
        updateButtons();
    }

    private void updateButtons() {
        Medical m = selected(false);
        for (int i = 0; i < decisions.size(); i++) {
            decisionButtons.get(i).setEnabled(m == null || decisions.get(i).isAllowedFor(m));
        }
    }

    private Medical selected(boolean warn) {
        Medical m = model.getItem(UI.selectedModelRow(table));
        if (m == null && warn) UI.warn(this, "Select a medical in the table first.");
        return m;
    }

    // ------------------------------------------------------------------ actions
    private void addMedical() {
        List<lk.ac.ruh.fot.ams.model.Undergraduate> students = new ArrayList<>();
        if (!safe(() -> students.addAll(studentDAO.findAll()))) return;
        Medical m = new MedicalDialog(this, accent, dao, user, students, null).open();
        if (m != null) {
            refresh();
            linkedInfo(m, "added");
        }
    }

    private void editMedical() {
        Medical m = selected(true);
        if (m == null) return;
        List<lk.ac.ruh.fot.ams.model.Undergraduate> students = new ArrayList<>();
        if (!safe(() -> students.addAll(studentDAO.findAll()))) return;
        Medical saved = new MedicalDialog(this, accent, dao, user, students, m).open();
        if (saved != null) {
            refresh();
            linkedInfo(saved, "updated");
        }
    }

    private void deleteMedical() {
        Medical m = selected(true);
        if (m == null) return;
        if (!UI.confirm(this, "Delete the medical of " + m.getTgNo() + " for " + Format.date(m.getMedicalDate()) + "?\n"
                + "The absences it covered will no longer be covered by a medical.")) {
            return;
        }
        if (safe(() -> dao.delete(m.getMedicalId()))) refresh();
    }

    /** One handler for Approve, Reject and Mark pending (polymorphism). */
    private void decide(MedicalDecision decision) {
        Medical m = selected(true);
        if (m == null) return;
        if (!UI.confirm(this, decision.confirmMessage(m))) return;
        if (safe(() -> dao.applyDecision(m, decision, user.getUserId()))) refresh();
    }

    private void openDocument() {
        Medical m = selected(true);
        if (m == null) return;
        if (m.getDocumentPath() == null || m.getDocumentPath().isBlank()) {
            UI.warn(this, "No document is attached to this medical.");
            return;
        }
        safe(() -> FileUtil.open(m.getDocumentPath()));
    }

    private void linkedInfo(Medical m, String verb) {
        int[] n = new int[1];
        safe(() -> n[0] = dao.countLinkedAbsences(m.getMedicalId()));
        UI.info(this, "Medical " + verb + ". It is linked to " + n[0] + " absence(s) of " + m.getTgNo()
                + " on " + Format.date(m.getMedicalDate()) + ".");
    }

    private static String capital(String s) {
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
