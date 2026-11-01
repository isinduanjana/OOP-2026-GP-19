package lk.ac.ruh.fot.ams.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import lk.ac.ruh.fot.ams.ui.Theme;

/**
 * [COMMON PART] Rounded flat button with hover effect.
 * OOP: Inheritance (extends JButton) + Polymorphism (overrides paintComponent).
 */
public class ModernButton extends JButton {

    /** Visual style of the button. */
    public enum Style { PRIMARY, SECONDARY, DANGER, SUCCESS, GHOST }

    private final Style style;
    private Color base;
    private boolean hover;

    public ModernButton(String text, String iconName, Style style) {
        super(text);
        this.style = style;
        this.base = colorFor(style);
        setFont(Theme.FONT_BOLD);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setForeground(style == Style.SECONDARY || style == Style.GHOST ? Theme.TEXT : Color.WHITE);
        if (iconName != null) {
            setIcon(Icons.get(iconName, 16, getForeground()));
            setIconTextGap(8);
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    public ModernButton(String text, Style style) {
        this(text, null, style);
    }

    /** Lets a dashboard recolour primary buttons with its accent colour. */
    public void setBaseColor(Color c) {
        this.base = c;
        repaint();
    }

    private static Color colorFor(Style s) {
        return switch (s) {
            case PRIMARY -> Theme.PRIMARY;
            case DANGER -> Theme.DANGER;
            case SUCCESS -> Theme.SUCCESS;
            case SECONDARY -> Theme.SURFACE;
            case GHOST -> Theme.BG;
        };
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 36));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = base;
        if (!isEnabled()) {
            fill = Theme.tint(base, 0.5);
        } else if (getModel().isPressed()) {
            fill = Theme.darker(base, 0.15);
        } else if (hover) {
            fill = (style == Style.SECONDARY || style == Style.GHOST) ? Theme.tint(Theme.PRIMARY, 0.9) : Theme.darker(base, 0.08);
        }
        g.setColor(fill);
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        if (style == Style.SECONDARY) {
            g.setColor(Theme.BORDER);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        }
        g.dispose();
        super.paintComponent(g0);
    }
}
