package lk.ac.ruh.fot.ams.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lk.ac.ruh.fot.ams.ui.Theme;

/**
 * [COMMON PART] Dashboard tile: icon + big number + caption.
 * Example: new StatCard("Lecturers", "6", "users", Theme.LECTURER_ACCENT)
 */
public class StatCard extends CardPanel {

    private final JLabel valueLabel = new JLabel();
    private final JLabel captionLabel = new JLabel();

    public StatCard(String title, String value, String iconName, Color accent) {
        super(new BorderLayout(14, 0));
        setPreferredSize(new Dimension(220, 104));

        JComponent iconBox = new JComponent() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.tint(accent, 0.86));
                g.fillRoundRect(0, 0, 48, 48, 14, 14);
                Icons.get(iconName, 24, accent).paintIcon(this, g, 12, 12);
                g.dispose();
            }
        };
        iconBox.setPreferredSize(new Dimension(48, 48));
        JPanel iconWrap = new JPanel(new BorderLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(iconBox, BorderLayout.CENTER);
        iconWrap.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.FONT_SMALL.deriveFont(java.awt.Font.BOLD, 11f));
        titleLabel.setForeground(Theme.TEXT_MUTED);
        valueLabel.setText(value);
        valueLabel.setFont(Theme.FONT_STAT);
        valueLabel.setForeground(Theme.TEXT);
        captionLabel.setFont(Theme.FONT_SMALL);
        captionLabel.setForeground(Theme.TEXT_MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(titleLabel);
        text.add(valueLabel);
        text.add(captionLabel);

        add(iconWrap, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setCaption(String caption) {
        captionLabel.setText(caption);
    }
}
