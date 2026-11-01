package lk.ac.ruh.fot.ams.ui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.db.DBConnection;
import lk.ac.ruh.fot.ams.db.DatabaseInitializer;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [COMMON PART] Lets the user enter the MySQL host / user / password,
 * test the connection, and (re)create the database with sample data.
 */
public class DatabaseSettingsDialog extends JDialog {

    private final JTextField host = UI.textField(18);
    private final JTextField port = UI.textField(6);
    private final JTextField userField = UI.textField(18);
    private final JPasswordField password = UI.passwordField(18);
    private boolean saved;

    public DatabaseSettingsDialog(Window owner) {
        super(owner, "Database settings", ModalityType.APPLICATION_MODAL);
        AppConfig cfg = AppConfig.getInstance();
        host.setText(cfg.getHost());
        port.setText(cfg.getPort());
        userField.setText(cfg.getUser());
        password.setText(cfg.getPassword());

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        root.setBackground(Theme.SURFACE);
        root.add(UI.heading("MySQL connection"), BorderLayout.NORTH);
        JPanel form = UI.form(new String[]{"Host", "Port", "Username", "Password"}, host, port, userField, password);
        root.add(form, BorderLayout.CENTER);

        ModernButton test = UI.secondary("Test", "check");
        ModernButton reset = UI.danger("Reset sample data", "refresh");
        ModernButton save = UI.primary("Save", null);
        test.addActionListener(e -> test());
        reset.addActionListener(e -> resetDatabase());
        save.addActionListener(e -> save());
        root.add(UI.toolbar(UI.row(reset), UI.rowRight(test, save)), BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void apply() {
        AppConfig cfg = AppConfig.getInstance();
        cfg.setHost(host.getText().trim());
        cfg.setPort(port.getText().trim());
        cfg.setUser(userField.getText().trim());
        cfg.setPassword(new String(password.getPassword()));
        DBConnection.getInstance().reset();
    }

    private void test() {
        apply();
        try (var c = DBConnection.getInstance().openServerConnection()) {
            UI.info(this, "Connected to MySQL " + c.getMetaData().getDatabaseProductVersion());
        } catch (AppException e) {
            UI.error(this, e);
        } catch (Exception e) {
            UI.error(this, e.getMessage());
        }
    }

    private void resetDatabase() {
        if (!UI.confirm(this, "This DELETES the database '" + AppConfig.getInstance().getDbName()
                + "' and recreates it with the sample data.\nContinue?")) {
            return;
        }
        apply();
        try {
            new DatabaseInitializer().createDatabase();
            UI.info(this, "Database created with sample data.");
        } catch (AppException e) {
            UI.error(this, e);
        }
    }

    private void save() {
        apply();
        try {
            AppConfig.getInstance().save();
            saved = true;
            dispose();
        } catch (IOException e) {
            UI.error(this, "Could not save db.properties: " + e.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
