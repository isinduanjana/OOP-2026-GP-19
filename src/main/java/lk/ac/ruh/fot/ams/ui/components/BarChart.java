package lk.ac.ruh.fot.ams.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import lk.ac.ruh.fot.ams.ui.Theme;

/**
 * [COMMON PART] Simple vertical bar chart (values 0..max) with an optional
 * threshold line, e.g. the 80% attendance line. Bars under the threshold
 * are red, bars on/over it use the accent colour.
 */
public class BarChart extends JComponent {

    private final List<String> labels = new ArrayList<>();
    private final List<Double> values = new ArrayList<>();
    private double max = 100;
    private double threshold = -1;
    private String thresholdLabel = "";
    private Color accent = Theme.PRIMARY;
    private String emptyText = "No data to display";

    public BarChart() {
        setPreferredSize(new Dimension(400, 240));
        setOpaque(false);
    }

    public void setData(List<String> labels, List<Double> values) {
        this.labels.clear();
        this.values.clear();
        this.labels.addAll(labels);
        this.values.addAll(values);
        repaint();
    }

    public void setMax(double max) { this.max = max; repaint(); }
    public void setAccent(Color accent) { this.accent = accent; repaint(); }
    public void setEmptyText(String t) { this.emptyText = t; }

    public void setThreshold(double threshold, String label) {
        this.threshold = threshold;
        this.thresholdLabel = label;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int left = 40;
        int bottom = 34;
        int top = 18;
        int right = 10;
        int plotH = h - top - bottom;
        int plotW = w - left - right;
        g.setFont(Theme.FONT_SMALL);
        FontMetrics fm = g.getFontMetrics();

        if (values.isEmpty() || plotW <= 0 || plotH <= 0) {
            g.setColor(Theme.TEXT_MUTED);
            g.drawString(emptyText, (w - fm.stringWidth(emptyText)) / 2, h / 2);
            g.dispose();
            return;
        }

        // grid lines
        for (int i = 0; i <= 4; i++) {
            int y = top + plotH - (int) (plotH * i / 4.0);
            g.setColor(Theme.BORDER);
            g.drawLine(left, y, w - right, y);
            g.setColor(Theme.TEXT_MUTED);
            String txt = String.valueOf((int) (max * i / 4));
            g.drawString(txt, left - 8 - fm.stringWidth(txt), y + 4);
        }

        int n = values.size();
        double slot = plotW / (double) n;
        int barW = (int) Math.max(4, Math.min(42, slot * 0.6));
        for (int i = 0; i < n; i++) {
            double v = values.get(i) == null || values.get(i).isNaN() ? 0 : values.get(i);
            int bh = (int) (plotH * Math.min(v, max) / max);
            int x = left + (int) (slot * i + (slot - barW) / 2);
            int y = top + plotH - bh;
            Color c = (threshold >= 0 && v + 1e-9 < threshold) ? Theme.DANGER : accent;
            g.setColor(c);
            g.fillRoundRect(x, y, barW, bh, 8, 8);
            if (bh > 8) g.fillRect(x, y + bh - 4, barW, 4);
            // value on top
            if (slot > 26) {
                String vt = String.valueOf(Math.round(v));
                g.setColor(Theme.TEXT);
                g.drawString(vt, x + (barW - fm.stringWidth(vt)) / 2, y - 4);
            }
            // label under
            String lbl = labels.get(i);
            if (fm.stringWidth(lbl) > slot - 2) {
                while (lbl.length() > 2 && fm.stringWidth(lbl + "…") > slot - 2) lbl = lbl.substring(1);
            }
            g.setColor(Theme.TEXT_MUTED);
            g.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, h - bottom + 18);
        }

        if (threshold >= 0) {
            int y = top + plotH - (int) (plotH * threshold / max);
            g.setColor(Theme.DANGER);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 4f}, 0f));
            g.drawLine(left, y, w - right, y);
            // label in a small white box at the right end, so bar values never hide it
            int lw = fm.stringWidth(thresholdLabel) + 10;
            int lx = w - right - lw;
            g.setStroke(new BasicStroke(1f));
            g.setColor(Theme.SURFACE);
            g.fillRoundRect(lx, y - fm.getHeight() - 4, lw, fm.getHeight() + 2, 8, 8);
            g.setColor(Theme.DANGER);
            g.drawRoundRect(lx, y - fm.getHeight() - 4, lw, fm.getHeight() + 2, 8, 8);
            g.drawString(thresholdLabel, lx + 5, y - 6);
        }
        g.dispose();
    }
}
