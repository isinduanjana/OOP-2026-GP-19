package lk.ac.ruh.fot.ams.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lk.ac.ruh.fot.ams.ui.Theme;

/**
 * [COMMON PART] White rounded "card" with a soft shadow. Most screens put
 * their tables and forms inside cards.
 */
public class CardPanel extends JPanel {

    private static final int ARC = 18;

    public CardPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
    }

    public CardPanel() {
        this(new BorderLayout(0, 12));
    }

    /** Card with a bold title on top. */
    public static CardPanel titled(String title) {
        CardPanel card = new CardPanel(new BorderLayout(0, 12));
        JLabel l = new JLabel(title);
        l.setFont(Theme.FONT_HEADING);
        l.setForeground(Theme.TEXT);
        card.add(l, BorderLayout.NORTH);
        return card;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // soft shadow
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(15, 23, 42, 6 - i));
            g.fillRoundRect(i, i + 2, getWidth() - 2 * i - 1, getHeight() - 2 * i - 1, ARC + 4, ARC + 4);
        }
        g.setColor(Theme.SURFACE);
        g.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 4, ARC, ARC);
        g.setColor(Theme.BORDER);
        g.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 4, ARC, ARC);
        g.dispose();
        super.paintComponent(g0);
    }
}
