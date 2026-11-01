package lk.ac.ruh.fot.ams.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;

/**
 * [COMMON PART] Small line icons drawn with Java2D (no image files needed,
 * crisp at any size). Usage:  Icons.get("users", 18, Color.WHITE)
 *
 * Available names: home, users, user, book, bell, calendar, chart, check,
 * medical, logout, upload, grade, clipboard, plus, edit, trash, refresh,
 * file, search, award, settings, eye, cap
 */
public final class Icons {

    private Icons() {
    }

    public static Icon get(String name, int size, Color color) {
        return new VectorIcon(name, size, color);
    }

    /** OOP: implements the Swing Icon interface (abstraction / polymorphism). */
    private static final class VectorIcon implements Icon {
        private final String name;
        private final int size;
        private final Color color;

        VectorIcon(String name, int size, Color color) {
            this.name = name;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g0, int x, int y) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(x, y);
            double s = size / 24.0;          // icons are designed on a 24x24 grid
            g.scale(s, s);
            g.setColor(color);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            draw(g);
            g.dispose();
        }

        private void draw(Graphics2D g) {
            switch (name) {
                case "home" -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(3, 11); p.lineTo(12, 3); p.lineTo(21, 11);
                    g.draw(p);
                    g.draw(new RoundRectangle2D.Double(5.5, 10, 13, 10.5, 2, 2));
                    g.draw(new Line2D.Double(10, 20.5, 10, 15)); g.draw(new Line2D.Double(14, 20.5, 14, 15));
                    g.draw(new Line2D.Double(10, 15, 14, 15));
                }
                case "users" -> {
                    g.draw(new Ellipse2D.Double(5, 4, 7, 7));
                    g.draw(new Arc2D.Double(2, 13, 13, 12, 0, 180, Arc2D.OPEN));
                    g.draw(new Ellipse2D.Double(14, 5, 5.5, 5.5));
                    g.draw(new Arc2D.Double(14, 13.5, 8.5, 9, 0, 180, Arc2D.OPEN));
                }
                case "user" -> {
                    g.draw(new Ellipse2D.Double(8, 3.5, 8, 8));
                    g.draw(new Arc2D.Double(4, 14, 16, 14, 0, 180, Arc2D.OPEN));
                }
                case "book" -> {
                    g.draw(new RoundRectangle2D.Double(4, 3, 16, 18, 2, 2));
                    g.draw(new Line2D.Double(8, 3, 8, 21));
                    g.draw(new Line2D.Double(11, 8, 17, 8)); g.draw(new Line2D.Double(11, 12, 17, 12));
                }
                case "bell" -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(6, 17); p.lineTo(6, 11); p.curveTo(6, 4.5, 18, 4.5, 18, 11); p.lineTo(18, 17);
                    g.draw(p);
                    g.draw(new Line2D.Double(4, 17, 20, 17));
                    g.draw(new Arc2D.Double(9.5, 17.5, 5, 4, 180, 180, Arc2D.OPEN));
                }
                case "calendar" -> {
                    g.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15.5, 3, 3));
                    g.draw(new Line2D.Double(3.5, 10, 20.5, 10));
                    g.draw(new Line2D.Double(8, 3, 8, 7)); g.draw(new Line2D.Double(16, 3, 16, 7));
                    g.fill(new Ellipse2D.Double(7, 13, 2.4, 2.4)); g.fill(new Ellipse2D.Double(11, 13, 2.4, 2.4));
                    g.fill(new Ellipse2D.Double(15, 13, 2.4, 2.4));
                }
                case "chart" -> {
                    g.draw(new Line2D.Double(4, 20, 20, 20));
                    g.draw(new Line2D.Double(7, 20, 7, 13)); g.draw(new Line2D.Double(12, 20, 12, 6));
                    g.draw(new Line2D.Double(17, 20, 17, 10));
                }
                case "check" -> {
                    g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                    Path2D p = new Path2D.Double();
                    p.moveTo(8, 12); p.lineTo(11, 15); p.lineTo(16.5, 9);
                    g.draw(p);
                }
                case "medical" -> {
                    g.draw(new RoundRectangle2D.Double(3.5, 3.5, 17, 17, 5, 5));
                    g.draw(new Line2D.Double(12, 8, 12, 16)); g.draw(new Line2D.Double(8, 12, 16, 12));
                }
                case "logout" -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(14, 4); p.lineTo(6, 4); p.lineTo(6, 20); p.lineTo(14, 20);
                    g.draw(p);
                    g.draw(new Line2D.Double(10, 12, 20, 12));
                    Path2D a = new Path2D.Double();
                    a.moveTo(16.5, 8.5); a.lineTo(20, 12); a.lineTo(16.5, 15.5);
                    g.draw(a);
                }
                case "upload" -> {
                    g.draw(new Line2D.Double(12, 15, 12, 4));
                    Path2D a = new Path2D.Double();
                    a.moveTo(7.5, 8.5); a.lineTo(12, 4); a.lineTo(16.5, 8.5);
                    g.draw(a);
                    Path2D b = new Path2D.Double();
                    b.moveTo(4, 15); b.lineTo(4, 20); b.lineTo(20, 20); b.lineTo(20, 15);
                    g.draw(b);
                }
                case "grade", "award" -> {
                    g.draw(new Ellipse2D.Double(6, 2.5, 12, 12));
                    Path2D p = new Path2D.Double();
                    p.moveTo(8.5, 13.5); p.lineTo(7, 21.5); p.lineTo(12, 18.5); p.lineTo(17, 21.5); p.lineTo(15.5, 13.5);
                    g.draw(p);
                }
                case "clipboard" -> {
                    g.draw(new RoundRectangle2D.Double(5, 4.5, 14, 16.5, 3, 3));
                    g.draw(new RoundRectangle2D.Double(9, 2.5, 6, 4, 2, 2));
                    g.draw(new Line2D.Double(8.5, 11, 15.5, 11)); g.draw(new Line2D.Double(8.5, 15, 13.5, 15));
                }
                case "plus" -> {
                    g.draw(new Line2D.Double(12, 5, 12, 19)); g.draw(new Line2D.Double(5, 12, 19, 12));
                }
                case "edit" -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(4, 20); p.lineTo(5, 15.5); p.lineTo(16, 4.5); p.lineTo(19.5, 8); p.lineTo(8.5, 19); p.closePath();
                    g.draw(p);
                }
                case "trash" -> {
                    g.draw(new Line2D.Double(4, 6.5, 20, 6.5));
                    g.draw(new RoundRectangle2D.Double(6.5, 6.5, 11, 14, 2, 2));
                    g.draw(new Line2D.Double(9.5, 3.5, 14.5, 3.5));
                    g.draw(new Line2D.Double(10, 10, 10, 17)); g.draw(new Line2D.Double(14, 10, 14, 17));
                }
                case "refresh" -> {
                    g.draw(new Arc2D.Double(4, 4, 16, 16, 30, 280, Arc2D.OPEN));
                    Path2D a = new Path2D.Double();
                    a.moveTo(19.5, 3.5); a.lineTo(19.5, 8.5); a.lineTo(14.5, 8.5);
                    g.draw(a);
                }
                case "file" -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(14, 3); p.lineTo(6, 3); p.lineTo(6, 21); p.lineTo(18, 21); p.lineTo(18, 7); p.closePath();
                    g.draw(p);
                    g.draw(new Line2D.Double(14, 3, 14, 7)); g.draw(new Line2D.Double(14, 7, 18, 7));
                }
                case "search" -> {
                    g.draw(new Ellipse2D.Double(4, 4, 12, 12));
                    g.draw(new Line2D.Double(14.5, 14.5, 20, 20));
                }
                case "settings" -> {
                    g.draw(new Ellipse2D.Double(8.5, 8.5, 7, 7));
                    for (int i = 0; i < 8; i++) {
                        double ang = Math.toRadians(i * 45);
                        g.draw(new Line2D.Double(12 + Math.cos(ang) * 6.5, 12 + Math.sin(ang) * 6.5,
                                12 + Math.cos(ang) * 9, 12 + Math.sin(ang) * 9));
                    }
                }
                case "eye" -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(2.5, 12); p.curveTo(7, 4.5, 17, 4.5, 21.5, 12); p.curveTo(17, 19.5, 7, 19.5, 2.5, 12);
                    g.draw(p);
                    g.draw(new Ellipse2D.Double(9, 9, 6, 6));
                }
                case "cap" -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(2, 9.5); p.lineTo(12, 4.5); p.lineTo(22, 9.5); p.lineTo(12, 14.5); p.closePath();
                    g.draw(p);
                    Path2D q = new Path2D.Double();
                    q.moveTo(6, 11.5); q.lineTo(6, 16.5); q.curveTo(9, 19.5, 15, 19.5, 18, 16.5); q.lineTo(18, 11.5);
                    g.draw(q);
                    g.draw(new Line2D.Double(22, 9.5, 22, 15));
                }
                default -> g.draw(new Ellipse2D.Double(5, 5, 14, 14));
            }
        }
    }
}
