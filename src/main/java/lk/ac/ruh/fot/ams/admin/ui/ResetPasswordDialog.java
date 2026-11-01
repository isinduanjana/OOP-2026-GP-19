package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JPasswordField;

import lk.ac.ruh.fot.ams.admin.dao.AdminUserDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Validator;

/**
 * [M1 - ADMIN MODULE] Lets the admin set a new password for any account
 * (e.g. when a student forgot it). Stored as a SHA-256 hash (common PasswordUtil).
 *
 * OOP: Inheritance (AdminFormDialog subclass) + Polymorphism (own save()).
 */
public class ResetPasswordDialog extends AdminFormDialog {

    private final AdminUserDAO dao;
    private final User target;
    private final JPasswordField pw = UI.passwordField(20);
    private final JPasswordField confirm = UI.passwordField(20);

    public ResetPasswordDialog(Component parent, Color accent, AdminUserDAO dao, User target) {
        super(parent, "Reset password", "New password for " + target.getFullName() + " (" + target.getUsername() + ")",
                "settings", accent);
        this.dao = dao;
        this.target = target;
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.add(UI.form(new String[]{"New password", "Confirm password"}, pw, confirm), BorderLayout.NORTH);
        p.add(AdminUI.hint("At least 4 characters. Tell the user to change it from 'My Profile' after logging in."),
                BorderLayout.CENTER);
        setContent(p);
        saveButton.setText("Reset password");
    }

    @Override
    protected void save() throws AppException {
        String a = Validator.password(new String(pw.getPassword()));
        if (!a.equals(new String(confirm.getPassword()))) {
            throw new ValidationException("Password", "The two passwords do not match.");
        }
        dao.resetPassword(target.getUserId(), a);
    }
}
