package lk.ac.ruh.fot.ams.undergraduate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.enums.StudentType;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.FileUtil;

/**
 * [M4 - UNDERGRADUATE MODULE] Small static helpers used by several student
 * pages (opening files safely, wrapped text, progress bars, type colours).
 * It builds on the common UI / FileUtil classes instead of repeating them.
 *
 * OOP: Encapsulation (private constructor - utility class), exception
 * handling (openFile catches the common AppException).
 */
public final class StudentUI {

    private StudentUI() {
    }

    /**
     * Opens a material / medical document with the OS program. If the file is
     * missing (sample data only stores file names) a friendly message is shown
     * instead of an error.
     */
    public static void openFile(Component parent, String path, String what) {
        if (path == null || path.isBlank()) {
            UI.warn(parent, "No file has been attached to this " + what + ".");
            return;
        }
        if (!FileUtil.exists(path)) {
            UI.warn(parent, "The file for this " + what + " is not available on this computer yet:\n"
                    + Paths.get(path).getFileName()
                    + "\n\nThe sample database only stores file names. Ask your lecturer / TO to upload it again.");
            return;
        }
        try {
            FileUtil.open(path);
        } catch (AppException e) {
            UI.error(parent, e);
        }
    }

    /** Only the file name of a stored path (or "-"). */
    public static String fileName(String path) {
        if (path == null || path.isBlank()) return "-";
        return Paths.get(path).getFileName().toString();
    }

    /** Read-only, wrapping text block that looks like a label. */
    public static JTextArea text(String s) {
        JTextArea a = new JTextArea(s == null || s.isBlank() ? "-" : s);
        a.setEditable(false);
        a.setFocusable(false);
        a.setOpaque(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(Theme.FONT);
        a.setForeground(Theme.TEXT);
        a.setBorder(BorderFactory.createEmptyBorder());
        return a;
    }

    /** Muted centred message for empty lists. */
    public static JLabel empty(String text) {
        JLabel l = UI.muted(text);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
        return l;
    }

    /** Badge colour for each student type. */
    public static Color typeColor(StudentType t) {
        if (t == StudentType.REPEAT) return Theme.WARNING;
        if (t == StudentType.BATCH_MISSED) return Theme.INFO;
        return Theme.SUCCESS;
    }

    /** "Normal student", "Repeat student", "Batch Missed student". */
    public static String typeText(StudentType t) {
        return (t == null ? StudentType.NORMAL : t) + " student";
    }

    /** Thin rounded progress bar (0..100) - red below the 80% line. */
    public static JComponent bar(double percent, Color okColor, double threshold) {
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int h = getHeight();
                g.setColor(Theme.tint(Theme.TEXT_MUTED, 0.82));
                g.fillRoundRect(0, 0, getWidth(), h, h, h);
                double p = Double.isNaN(percent) ? 0 : Math.max(0, Math.min(100, percent));
                int w = (int) Math.round(getWidth() * p / 100.0);
                g.setColor(p + 1e-9 < threshold ? Theme.DANGER : okColor);
                if (w > 0) g.fillRoundRect(0, 0, Math.max(w, h), h, h, h);
                g.dispose();
            }
        };
        c.setPreferredSize(new Dimension(120, 7));
        c.setMinimumSize(new Dimension(20, 7));
        return c;
    }

    /** Translucent white "glass" badge for text on the coloured welcome banner. */
    public static JLabel glassBadge(String text) {
        JLabel l = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(255, 255, 255, 46));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g.dispose();
                super.paintComponent(g0);
            }
        };
        l.setFont(Theme.FONT_SMALL.deriveFont(java.awt.Font.BOLD));
        l.setForeground(Color.WHITE);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return l;
    }

    /** Transparent BorderLayout panel. */
    public static JPanel clear(int hgap, int vgap) {
        JPanel p = new JPanel(new BorderLayout(hgap, vgap));
        p.setOpaque(false);
        return p;
    }
}
