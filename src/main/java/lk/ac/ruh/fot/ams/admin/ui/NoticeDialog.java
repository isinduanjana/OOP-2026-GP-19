package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lk.ac.ruh.fot.ams.admin.dao.AdminNoticeDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Notice;
import lk.ac.ruh.fot.ams.model.enums.NoticeTarget;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Validator;

/**
 * [M1 - ADMIN MODULE] Add / Edit a notice. ACTIVE notices appear on the notice board
 * (common NoticeBoardPanel) of the chosen audience.
 *
 * OOP: Inheritance (AdminFormDialog) + Polymorphism (save()).
 */
public class NoticeDialog extends AdminFormDialog {

    private static final int MAX_TITLE = 100;

    private final AdminNoticeDAO dao;
    private final Notice notice;
    private final int adminUserId;
    private final JTextField titleField = UI.textField(30);
    private final JComboBox<NoticeTarget> targetCombo = new JComboBox<>(NoticeTarget.values());
    private final JRadioButton active = new JRadioButton("Active (visible)");
    private final JRadioButton inactive = new JRadioButton("Inactive (hidden)");
    private final JTextArea descArea = UI.textArea(8);
    private final JLabel counter = UI.muted("");

    public NoticeDialog(Component parent, Color accent, AdminNoticeDAO dao, Notice existing, int adminUserId) {
        super(parent, existing == null ? "New notice" : "Edit notice",
                "Active notices are shown to the selected audience on their notice board", "bell", accent);
        this.dao = dao;
        this.notice = existing == null ? new Notice() : existing;
        this.adminUserId = adminUserId;

        ButtonGroup g = new ButtonGroup();
        g.add(active);
        g.add(inactive);
        active.setOpaque(false);
        inactive.setOpaque(false);
        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        status.setOpaque(false);
        status.add(active);
        status.add(javax.swing.Box.createHorizontalStrut(16));
        status.add(inactive);

        titleField.putClientProperty("JTextField.placeholderText", "Short, clear title");
        JPanel form = UI.form(new String[]{"Title", "Audience", "Status", "Description"},
                titleField, targetCombo, status, AdminUI.areaScroll(descArea, 440, 170));
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(form, BorderLayout.CENTER);
        JPanel cnt = UI.rowRight(counter);
        p.add(cnt, BorderLayout.SOUTH);
        setContent(p);

        titleField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateCounter(); }
            @Override public void removeUpdate(DocumentEvent e) { updateCounter(); }
            @Override public void changedUpdate(DocumentEvent e) { updateCounter(); }
        });

        titleField.setText(notice.getTitle());
        descArea.setText(notice.getDescription());
        targetCombo.setSelectedItem(notice.getTargetRole());
        (("INACTIVE".equals(notice.getStatus())) ? inactive : active).setSelected(true);
        saveButton.setText(existing == null ? "Publish notice" : "Save changes");
        updateCounter();
    }

    private void updateCounter() {
        int n = titleField.getText().length();
        counter.setText("Title: " + n + " / " + MAX_TITLE + " characters");
        counter.setForeground(n > MAX_TITLE ? Theme.DANGER : Theme.TEXT_MUTED);
    }

    @Override
    protected void save() throws AppException {
        String title = Validator.required(titleField.getText(), "Title");
        if (title.length() > MAX_TITLE) {
            throw new ValidationException("Title", "The title can have at most " + MAX_TITLE + " characters.");
        }
        notice.setTitle(title);
        notice.setDescription(Validator.required(descArea.getText(), "Description"));
        notice.setTargetRole((NoticeTarget) targetCombo.getSelectedItem());
        notice.setStatus(active.isSelected() ? "ACTIVE" : "INACTIVE");
        if (notice.getNoticeId() == 0) {
            notice.setCreatedBy(adminUserId);           // created_by = logged-in admin
            notice.setNoticeId(dao.insert(notice));
        } else {
            dao.update(notice);
        }
    }
}
