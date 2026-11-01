package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lk.ac.ruh.fot.ams.admin.dao.AdminNoticeDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Notice;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.NoticeTarget;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M1 - ADMIN MODULE] "Notice Management" page: create, edit, hide/show and delete notices.
 * A preview card on the right shows how the selected notice reads.
 *
 * OOP: Inheritance + Polymorphism - CrudPanel&lt;Notice&gt; subclass (overrides the abstract
 *      methods and the optional hooks onSelectionChanged / accept).
 */
public class NoticeManagementPanel extends CrudPanel<Notice> {

    private final AdminNoticeDAO dao = new AdminNoticeDAO();
    private final JComboBox<String> targetFilter = new JComboBox<>();
    private final ModernButton toggleButton = UI.secondary("Hide", "eye");
    private final JLabel previewTitle = UI.heading(" ");
    private final JLabel previewMeta = UI.muted(" ");
    private final JLabel previewBadge = new JLabel();
    private final JPanel badgeHolder = UI.row();
    private final JTextArea previewText = UI.textArea(10);

    public NoticeManagementPanel(User user, Color accent) {
        super(user, accent, "notice");
        targetFilter.addItem("All audiences");
        for (NoticeTarget t : NoticeTarget.values()) targetFilter.addItem(t.toString());
        targetFilter.setPreferredSize(new Dimension(170, 36));
        targetFilter.addActionListener(e -> applyFilter());
        addFilter(targetFilter);
        toggleButton.addActionListener(e -> toggle());
        addAction(toggleButton, true);
        setSidePanel(buildPreview());
        addButton.setText("New notice");
    }

    private JPanel buildPreview() {
        CardPanel card = CardPanel.titled("Preview");
        card.setPreferredSize(new Dimension(330, 10));
        previewText.setEditable(false);
        previewText.setOpaque(false);
        previewText.setBorder(BorderFactory.createEmptyBorder());
        previewText.setForeground(Theme.TEXT);
        previewTitle.setText("<html>Select a notice to preview it</html>");
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new javax.swing.BoxLayout(head, javax.swing.BoxLayout.Y_AXIS));
        previewTitle.setAlignmentX(LEFT_ALIGNMENT);
        badgeHolder.setAlignmentX(LEFT_ALIGNMENT);
        previewMeta.setAlignmentX(LEFT_ALIGNMENT);
        head.add(previewTitle);
        head.add(javax.swing.Box.createVerticalStrut(8));
        head.add(badgeHolder);
        head.add(javax.swing.Box.createVerticalStrut(8));
        head.add(previewMeta);
        JScrollPane sp = new JScrollPane(previewText);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        JPanel inner = new JPanel(new BorderLayout(0, 12));
        inner.setOpaque(false);
        inner.add(head, BorderLayout.NORTH);
        inner.add(sp, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    @Override
    public String getTitle() {
        return "Notice Management";
    }

    @Override
    public String getSubtitle() {
        return "Publish notices for everyone, lecturers, technical officers or undergraduates";
    }

    @Override
    protected String[] columns() {
        return new String[]{"Title", "Audience", "Status", "Created at", "Created by"};
    }

    @Override
    protected int[] columnWidths() {
        return new int[]{320, 130, 80, 130, 140};
    }

    @Override
    protected List<Notice> loadItems() throws AppException {
        return dao.findAll();
    }

    @Override
    protected boolean accept(Notice n) {
        int i = targetFilter.getSelectedIndex();
        return i <= 0 || n.getTargetRole() == NoticeTarget.values()[i - 1];
    }

    @Override
    protected Object[] toRow(Notice n) {
        return new Object[]{n.getTitle(), n.getTargetRole().toString(), AdminUI.statusWord(n.getStatus()),
                Format.dateTime(n.getCreatedAt()), Format.nullToDash(n.getCreatedByName())};
    }

    @Override
    protected boolean openEditor(Notice n) throws AppException {
        Notice fresh = n == null ? null : dao.findById(n.getNoticeId());
        return new NoticeDialog(this, accent, dao, fresh, user.getUserId()).showDialog();
    }

    @Override
    protected void deleteItem(Notice n) throws AppException {
        dao.delete(n.getNoticeId());
    }

    @Override
    protected String describe(Notice n) {
        String t = n.getTitle();
        return t.length() > 50 ? t.substring(0, 48) + "…" : t;
    }

    @Override
    protected int idOf(Notice n) {
        return n.getNoticeId();
    }

    @Override
    protected void onSelectionChanged(Notice n) {
        toggleButton.setText(n != null && !"ACTIVE".equals(n.getStatus()) ? "Show" : "Hide");
        badgeHolder.removeAll();
        if (n == null) {
            previewTitle.setText("<html>Select a notice to preview it</html>");
            previewMeta.setText(" ");
            previewText.setText("");
        } else {
            previewTitle.setText("<html><body style='width:240px'>" + escape(n.getTitle()) + "</body></html>");
            badgeHolder.add(UI.badge(n.getTargetRole().toString(), accent));
            boolean act = "ACTIVE".equals(n.getStatus());
            badgeHolder.add(UI.badge(act ? "Active" : "Inactive", act ? Theme.SUCCESS : Theme.DANGER));
            previewMeta.setText(Format.dateTime(n.getCreatedAt()) + "  ·  by " + Format.nullToDash(n.getCreatedByName()));
            previewText.setText(n.getDescription());
            previewText.setCaretPosition(0);
        }
        badgeHolder.revalidate();
        badgeHolder.repaint();
    }

    private void toggle() {
        Notice n = getSelected();
        if (n == null) return;
        safe(() -> {
            String status = dao.toggleStatus(n);
            refresh();
            flash("\"" + describe(n) + "\" is now " + ("ACTIVE".equals(status) ? "visible" : "hidden") + ".");
        });
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
