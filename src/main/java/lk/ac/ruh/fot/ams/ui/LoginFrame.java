package lk.ac.ruh.fot.ams.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.db.DatabaseInitializer;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.service.AuthService;
import lk.ac.ruh.fot.ams.ui.components.Icons;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [COMMON PART] Login screen for all four roles. After a successful login
 * DashboardFactory opens the dashboard of the user's role (M1/M2/M3/M4).
 */
public class LoginFrame extends JFrame {

    private final JTextField username = UI.textField(22);
    private final JPasswordField password = UI.passwordField(22);
    private final ModernButton loginButton = UI.primary("Sign in", null);
    private final JLabel status = new JLabel(" ");
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        super(AppConfig.APP_NAME + " - Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1040, 640);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildBrandPanel());
        root.add(buildFormPanel());
        setContentPane(root);
        getRootPane().setDefaultButton(loginButton);
        checkDatabase();
    }

    private JComponent buildBrandPanel() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new GradientPaint(0, 0, new Color(0x1E3A8A), getWidth(), getHeight(), new Color(0x4F46E5)));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(255, 255, 255, 18));
                g.fillOval(getWidth() - 260, -120, 420, 420);
                g.fillOval(-140, getHeight() - 220, 360, 360);
                g.dispose();
            }
        };
        p.setBorder(BorderFactory.createEmptyBorder(48, 48, 40, 48));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel cap = new JLabel(Icons.get("cap", 54, Color.WHITE));
        JLabel uni = new JLabel(AppConfig.UNIVERSITY);
        uni.setFont(Theme.FONT_HEADING.deriveFont(18f));
        uni.setForeground(new Color(0xC7D2FE));
        JLabel fac = new JLabel(AppConfig.FACULTY);
        fac.setFont(Theme.FONT_TITLE.deriveFont(30f));
        fac.setForeground(Color.WHITE);
        JLabel sys = new JLabel("<html>Academic Management System</html>");
        sys.setFont(Theme.FONT.deriveFont(17f));
        sys.setForeground(new Color(0xE0E7FF));
        top.add(cap);
        top.add(Box.createVerticalStrut(18));
        top.add(uni);
        top.add(fac);
        top.add(Box.createVerticalStrut(6));
        top.add(sys);

        JPanel features = new JPanel();
        features.setOpaque(false);
        features.setLayout(new BoxLayout(features, BoxLayout.Y_AXIS));
        String[][] f = {
                {"users", "User profiles for admins, lecturers, TOs and students"},
                {"clipboard", "Attendance with medical tracking (80% rule)"},
                {"grade", "CA eligibility, final marks, grades, SGPA & CGPA"},
                {"calendar", "Timetables, notices and course materials"}};
        for (String[] item : f) {
            JLabel l = new JLabel(item[1], Icons.get(item[0], 18, new Color(0xC7D2FE)), JLabel.LEFT);
            l.setIconTextGap(12);
            l.setForeground(new Color(0xE0E7FF));
            l.setFont(Theme.FONT);
            l.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            features.add(l);
        }
        JLabel foot = new JLabel("ICT2132 Object Oriented Programming Practicum • Mini Project");
        foot.setFont(Theme.FONT_SMALL);
        foot.setForeground(new Color(0xA5B4FC));
        JPanel bottom = new JPanel(new BorderLayout(0, 24));
        bottom.setOpaque(false);
        bottom.add(features, BorderLayout.CENTER);
        bottom.add(foot, BorderLayout.SOUTH);

        p.add(top, BorderLayout.NORTH);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private JComponent buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.SURFACE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(340, 420));

        JLabel welcome = UI.title("Welcome back");
        JLabel hint = UI.muted("Sign in with your university account");
        username.putClientProperty("JTextField.placeholderText", "e.g. admin, lec.perera, tg1001");
        username.putClientProperty("JTextField.leadingIcon", Icons.get("user", 16, Theme.TEXT_MUTED));
        password.putClientProperty("JTextField.placeholderText", "Password");
        for (JComponent c : new JComponent[]{username, password, loginButton}) {
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            c.setAlignmentX(LEFT_ALIGNMENT);
        }
        welcome.setAlignmentX(LEFT_ALIGNMENT);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        JLabel userLbl = UI.label("Username");
        JLabel passLbl = UI.label("Password");
        userLbl.setAlignmentX(LEFT_ALIGNMENT);
        passLbl.setAlignmentX(LEFT_ALIGNMENT);
        status.setFont(Theme.FONT_SMALL);
        status.setAlignmentX(LEFT_ALIGNMENT);

        JLabel settings = new JLabel("Database settings", Icons.get("settings", 14, Theme.PRIMARY), JLabel.LEFT);
        settings.setForeground(Theme.PRIMARY);
        settings.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        settings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settings.setAlignmentX(LEFT_ALIGNMENT);
        settings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DatabaseSettingsDialog d = new DatabaseSettingsDialog(LoginFrame.this);
                d.setVisible(true);
                checkDatabase();
            }
        });



        form.add(welcome);
        form.add(Box.createVerticalStrut(4));
        form.add(hint);
        form.add(Box.createVerticalStrut(28));
        form.add(userLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(username);
        form.add(Box.createVerticalStrut(16));
        form.add(passLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(password);
        form.add(Box.createVerticalStrut(24));
        form.add(loginButton);
        form.add(Box.createVerticalStrut(14));
        form.add(status);
        form.add(Box.createVerticalStrut(20));
        form.add(settings);


        loginButton.addActionListener(e -> login());
        outer.add(form);
        return outer;
    }

    /** Checks / creates the database in the background so the window stays responsive. */
    private void checkDatabase() {
        setStatus("Connecting to the database...", Theme.TEXT_MUTED);
        loginButton.setEnabled(false);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    DatabaseInitializer init = new DatabaseInitializer();
                    boolean existed = init.isDatabaseReady();
                    init.ensureDatabase();
                    return existed ? "OK" : "CREATED";
                } catch (AppException e) {
                    return "ERR:" + e.getUserMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    loginButton.setEnabled(true);
                    if (r.startsWith("ERR:")) {
                        setStatus("<html>" + r.substring(4).replace("\n", "<br>")
                                + "<br>Click 'Database settings' to fix.</html>", Theme.DANGER);
                    } else if ("CREATED".equals(r)) {
                        setStatus("Database created with sample data. You can sign in.", Theme.SUCCESS);
                    } else {
                        setStatus("● Connected to MySQL", Theme.SUCCESS);
                    }
                } catch (Exception ex) {
                    loginButton.setEnabled(true);
                    setStatus("Database check failed: " + ex.getMessage(), Theme.DANGER);
                }
            }
        }.execute();
    }

    private void setStatus(String text, Color color) {
        status.setText(text);
        status.setForeground(color);
    }

    private void login() {
        try {
            User user = authService.login(username.getText(), new String(password.getPassword()));
            BaseDashboard dashboard = DashboardFactory.create(user);   // polymorphic dashboard
            dashboard.setVisible(true);
            dispose();
        } catch (AppException e) {
            setStatus(e.getUserMessage().split("\n")[0], Theme.DANGER);
            UI.error(this, e);
            password.setText("");
        }
    }
}
