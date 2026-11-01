package lk.ac.ruh.fot.ams.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;

import javax.swing.JComponent;

import lk.ac.ruh.fot.ams.ui.Theme;

/**
 * [COMMON PART] Circular progress indicator, e.g. attendance 86% or GPA 3.4/4.
 */
public class ProgressRing extends JComponent {

    private double value;
    private double max = 100;
    private String centerText = "";
    private String caption = "";
    private Color color = Theme.PRIMARY;

    public ProgressRing(int size) {
        setPreferredSize(new Dimension(size, size + 22));
        setOpaque(false);
    }

    public void setValue(double value, double max, String centerText, String caption, Color color) {
        this.value = value;
        this.max = max;
        this.centerText = centerText;
        this.caption = caption;
        this.color = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int size = Math.min(getWidth(), getHeight() - 22) - 12;
        int x = (getWidth() - size) / 2;
        int y = 6;
        g.setStroke(new BasicStroke(11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Theme.tint(color, 0.85));
        g.draw(new Arc2D.Double(x, y, size, size, 0, 360, Arc2D.OPEN));
        g.setColor(color);
        double extent = max == 0 ? 0 : -360 * Math.max(0, Math.min(value, max)) / max;
        g.draw(new Arc2D.Double(x, y, size, size, 90, extent, Arc2D.OPEN));
        g.setFont(Theme.FONT_HEADING.deriveFont(18f));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Theme.TEXT);
        g.drawString(centerText, getWidth() / 2 - fm.stringWidth(centerText) / 2, y + size / 2 + fm.getAscent() / 2 - 2);
        g.setFont(Theme.FONT_SMALL);
        fm = g.getFontMetrics();
        g.setColor(Theme.TEXT_MUTED);
        g.drawString(caption, getWidth() / 2 - fm.stringWidth(caption) / 2, y + size + 18);
        g.dispose();
    }
}
