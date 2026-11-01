package lk.ac.ruh.fot.ams.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import lk.ac.ruh.fot.ams.ui.Theme;

/**
 * [COMMON PART] Round profile picture. Shows the picture file if it exists,
 * otherwise the person's initials on a coloured circle.
 */
public class Avatar extends JComponent {

    private Image image;
    private String initials = "?";
    private Color color = Theme.PRIMARY;

    public Avatar(int size) {
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setOpaque(false);
    }

    public void setUser(String initials, String picturePath, Color color) {
        this.initials = initials == null ? "?" : initials;
        this.color = color;
        this.image = null;
        if (picturePath != null && !picturePath.isBlank()) {
            try {
                File f = new File(picturePath);
                if (f.isFile()) {
                    image = ImageIO.read(f);
                }
            } catch (Exception ignored) {
                image = null;   // fall back to initials
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int s = Math.min(getWidth(), getHeight());
        Ellipse2D circle = new Ellipse2D.Double(0, 0, s - 1, s - 1);
        if (image != null) {
            g.setClip(circle);
            int iw = image.getWidth(null);
            int ih = image.getHeight(null);
            double scale = Math.max(s / (double) iw, s / (double) ih);
            int dw = (int) (iw * scale);
            int dh = (int) (ih * scale);
            g.drawImage(image, (s - dw) / 2, (s - dh) / 2, dw, dh, null);
            g.setClip(null);
        } else {
            g.setColor(color);
            g.fill(circle);
            g.setColor(Color.WHITE);
            g.setFont(Theme.FONT_BOLD.deriveFont((float) (s * 0.38)));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(initials, (s - fm.stringWidth(initials)) / 2, (s - fm.getHeight()) / 2 + fm.getAscent());
        }
        g.setColor(new Color(255, 255, 255, 120));
        g.draw(circle);
        g.dispose();
    }
}
