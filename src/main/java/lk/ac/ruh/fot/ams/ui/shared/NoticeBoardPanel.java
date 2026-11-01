package lk.ac.ruh.fot.ams.ui.shared;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lk.ac.ruh.fot.ams.dao.NoticeDAO;
import lk.ac.ruh.fot.ams.model.Notice;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.ScrollablePanel;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [COMMON PART] Read-only notice board ("Can see notices" - Lecturer, TO,
 * Undergraduate). Shows active notices for ALL + the user's own role.
 */
public class NoticeBoardPanel extends BasePanel {

    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final JPanel list = new JPanel();
    private final JTextField search = UI.searchField("Search notices...");
    private final JLabel count = UI.muted("");
    private List<Notice> notices = new ArrayList<>();

    public NoticeBoardPanel(User user, Color accent) {
        super(user, accent);
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        JPanel holder = new ScrollablePanel(new BorderLayout());
        holder.add(list, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(holder);
        sp.setBorder(null);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { render(); }
            @Override public void removeUpdate(DocumentEvent e) { render(); }
            @Override public void changedUpdate(DocumentEvent e) { render(); }
        });
        add(UI.toolbar(UI.row(search), UI.rowRight(count)), BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "Notices";
    }

    @Override
    public String getSubtitle() {
        return "Latest announcements from the faculty";
    }

    @Override
    public void refresh() {
        safe(() -> {
            notices = noticeDAO.findActiveForRole(user.getRole());
            render();
        });
    }

    private void render() {
        list.removeAll();
        String q = search.getText().trim().toLowerCase();
        int shown = 0;
        for (Notice n : notices) {
            if (!q.isEmpty() && !(n.getTitle() + " " + n.getDescription()).toLowerCase().contains(q)) continue;
            list.add(card(n));
            list.add(Box.createVerticalStrut(12));
            shown++;
        }
        if (shown == 0) {
            list.add(UI.muted("No notices to show."));
        }
        count.setText(shown + " notice(s)");
        list.revalidate();
        list.repaint();
    }

    private JPanel card(Notice n) {
        CardPanel c = new CardPanel(new BorderLayout(0, 8));
        JPanel head = new JPanel(new BorderLayout(10, 0));
        head.setOpaque(false);
        JLabel title = UI.heading(n.getTitle());
        head.add(title, BorderLayout.CENTER);
        head.add(UI.badge(n.getTargetRole().toString(), accent), BorderLayout.EAST);
        JTextArea body = new JTextArea(n.getDescription());
        body.setEditable(false);
        body.setOpaque(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(Theme.FONT);
        body.setForeground(Theme.TEXT);
        body.setBorder(BorderFactory.createEmptyBorder());
        JLabel meta = UI.muted("Posted " + Format.dateTime(n.getCreatedAt())
                + (n.getCreatedByName() == null ? "" : "  •  by " + n.getCreatedByName()));
        c.add(head, BorderLayout.NORTH);
        c.add(body, BorderLayout.CENTER);
        c.add(meta, BorderLayout.SOUTH);
        return c;
    }
}
