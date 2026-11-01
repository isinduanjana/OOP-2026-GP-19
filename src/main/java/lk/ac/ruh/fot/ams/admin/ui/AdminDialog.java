package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.Icons;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [M1 - ADMIN MODULE] Abstract base of every admin pop-up window: a modal JDialog with
 * a coloured header (icon, title, subtitle), a body and a button row.
 *
 *   AdminDialog (abstract)
 *     |-- AdminFormDialog (abstract, Save / Cancel) -> UserDialog, CourseDialog, NoticeDialog,
 *     |                                                 TimetableDialog, ResetPasswordDialog
 *     |-- AssessmentDialog, EnrollmentDialog, WeekViewDialog (Close button)
 *
 * OOP: Abstraction + Inheritance (extends JDialog, subclasses only add their content),
 *      Encapsulation (the layout panels are private; subclasses use setContent / addButton).
 */
public abstract class AdminDialog extends JDialog {

    protected final Color accent;
    private final JPanel body = new JPanel(new BorderLayout());
    private final JPanel buttons = UI.rowRight();
    private final JLabel subtitleLabel;

    protected AdminDialog(Component parent, String title, String subtitle, String icon, Color accent) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent), title, ModalityType.APPLICATION_MODAL);
        this.accent = accent;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));

        JComponent iconBox = new JComponent() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.tint(accent, 0.86));
                g.fillRoundRect(0, 0, 42, 42, 12, 12);
                Icons.get(icon, 22, accent).paintIcon(this, g, 10, 10);
                g.dispose();
            }
        };
        iconBox.setPreferredSize(new Dimension(42, 42));
        subtitleLabel = UI.muted(subtitle);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(UI.heading(title));
        titles.add(Box.createVerticalStrut(3));
        titles.add(subtitleLabel);
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(iconBox, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 14, 0)));

        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 0, 0, 0)));

        body.setOpaque(false);
        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);

        // ESC closes the dialog
        getRootPane().registerKeyboardAction(e -> closeDialog(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** The subclass puts its form / table here. */
    protected void setContent(JComponent content) {
        body.removeAll();
        body.add(content, BorderLayout.CENTER);
    }

    protected void addButton(JButton b) {
        buttons.add(b);
    }

    protected void setSubtitle(String text) {
        subtitleLabel.setText(text);
    }

    /** Called by ESC; subclasses may ask before closing (polymorphism). */
    protected void closeDialog() {
        dispose();
    }

    /** Packs, sizes (at least 460 px wide), centres and shows the modal dialog (blocks until closed). */
    public void open() {
        pack();
        int w = Math.max(460, getWidth());
        setMinimumSize(new Dimension(w, getHeight()));
        setSize(w, getHeight());
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }
}
