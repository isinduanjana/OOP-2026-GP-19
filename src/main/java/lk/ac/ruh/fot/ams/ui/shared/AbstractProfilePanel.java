package lk.ac.ruh.fot.ams.ui.shared;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import lk.ac.ruh.fot.ams.dao.UserDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.ProfileField;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.Avatar;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.FileUtil;
import lk.ac.ruh.fot.ams.util.PasswordUtil;
import lk.ac.ruh.fot.ams.util.Validator;

/**
 * [COMMON PART] "My Profile" page shared by all roles.
 *
 * Which fields are editable is NOT decided here - it asks the user object:
 *     user.canEdit(ProfileField.EMAIL)
 * Lecturer/TO: everything except username + password;
 * Undergraduate: only contact details + picture (polymorphism).
 *
 * Each member makes a small subclass (AdminProfilePanel, LecturerProfilePanel,
 * TOProfilePanel, StudentProfilePanel) that adds the role-specific details.
 */
public abstract class AbstractProfilePanel extends BasePanel {

    protected final UserDAO userDAO = new UserDAO();

    private final Avatar avatar = new Avatar(118);
    private final JLabel nameLabel = UI.heading("");
    private final JTextField usernameField = UI.textField(24);
    private final JTextField fullNameField = UI.textField(24);
    private final JTextField emailField = UI.textField(24);
    private final JTextField phoneField = UI.textField(24);
    private final JTextArea addressArea = UI.textArea(3);
    private final JPanel detailsBox = new JPanel(new GridBagLayout());

    protected AbstractProfilePanel(User user, Color accent) {
        super(user, accent);
        buildUI();
    }

    /** Role specific, read-only details (label -> value), e.g. "TG number". */
    protected abstract Map<String, String> getRoleDetails() throws AppException;

    @Override
    public String getTitle() {
        return "My Profile";
    }

    @Override
    public String getSubtitle() {
        return "Fields you are not allowed to change are locked";
    }

    private void buildUI() {
        // ---------- left: picture card
        CardPanel left = new CardPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(280, 360));
        JPanel l = new JPanel();
        l.setOpaque(false);
        l.setLayout(new BoxLayout(l, BoxLayout.Y_AXIS));
        avatar.setAlignmentX(CENTER_ALIGNMENT);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel role = UI.badge(user.getRole().getDisplayName(), accent);
        role.setAlignmentX(CENTER_ALIGNMENT);
        JLabel id = UI.muted(user.getIdentityNumber());
        id.setAlignmentX(CENTER_ALIGNMENT);
        l.add(Box.createVerticalStrut(10));
        l.add(avatar);
        l.add(Box.createVerticalStrut(14));
        l.add(nameLabel);
        l.add(Box.createVerticalStrut(6));
        l.add(role);
        l.add(Box.createVerticalStrut(4));
        l.add(id);
        l.add(Box.createVerticalStrut(18));
        if (user.canEdit(ProfileField.PROFILE_PICTURE)) {
            ModernButton pic = UI.secondary("Change picture", "upload");
            pic.setAlignmentX(CENTER_ALIGNMENT);
            pic.addActionListener(e -> changePicture());
            l.add(pic);
            l.add(Box.createVerticalStrut(8));
        }
        if (user.canEdit(ProfileField.PASSWORD)) {
            ModernButton pw = UI.secondary("Change password", "settings");
            pw.setAlignmentX(CENTER_ALIGNMENT);
            pw.addActionListener(e -> changePassword());
            l.add(pw);
        }
        left.add(l, BorderLayout.NORTH);

        // ---------- right: editable form
        CardPanel right = CardPanel.titled("Personal & contact details");
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addrScroll.setPreferredSize(new Dimension(300, 74));
        JPanel form = UI.form(new String[]{"Username", "Full name", "Email", "Phone", "Address"},
                usernameField, fullNameField, emailField, phoneField, addrScroll);
        lock(usernameField, user.canEdit(ProfileField.USERNAME));
        lock(fullNameField, user.canEdit(ProfileField.FULL_NAME));
        lock(emailField, user.canEdit(ProfileField.EMAIL));
        lock(phoneField, user.canEdit(ProfileField.PHONE));
        addressArea.setEditable(user.canEdit(ProfileField.ADDRESS));
        if (!user.canEdit(ProfileField.ADDRESS)) addressArea.setBackground(Theme.BG);
        right.add(form, BorderLayout.CENTER);
        ModernButton save = accentButton("Save changes", "check");
        save.addActionListener(e -> save());
        right.add(UI.rowRight(save), BorderLayout.SOUTH);

        // ---------- bottom: role details
        CardPanel details = CardPanel.titled(user.getRole().getDisplayName() + " details");
        detailsBox.setOpaque(false);
        JPanel detailsTop = new JPanel(new BorderLayout());
        detailsTop.setOpaque(false);
        detailsTop.add(detailsBox, BorderLayout.NORTH);     // keep rows at the top of the card
        details.add(detailsTop, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);
        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(details, BorderLayout.CENTER);
    }

    private static void lock(JTextField f, boolean editable) {
        f.setEditable(editable);
        if (!editable) {
            f.setBackground(Theme.BG);
            f.setToolTipText("You are not allowed to change this field");
            f.putClientProperty("JTextField.trailingIcon",
                    lk.ac.ruh.fot.ams.ui.components.Icons.get("eye", 14, Theme.TEXT_MUTED));
        }
    }

    @Override
    public void refresh() {
        safe(() -> {
            User fresh = userDAO.findById(user.getUserId());
            if (fresh != null) {
                user.setFullName(fresh.getFullName());
                user.setEmail(fresh.getEmail());
                user.setPhone(fresh.getPhone());
                user.setAddress(fresh.getAddress());
                user.setProfilePicture(fresh.getProfilePicture());
            }
            usernameField.setText(user.getUsername());
            fullNameField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
            addressArea.setText(user.getAddress());
            nameLabel.setText(user.getFullName());
            avatar.setUser(user.getInitials(), user.getProfilePicture(), accent);
            fillDetails(getRoleDetails());
        });
    }

    private void fillDetails(Map<String, String> map) {
        detailsBox.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 24);
        c.anchor = GridBagConstraints.WEST;
        int i = 0;
        for (Map.Entry<String, String> e : map.entrySet()) {
            c.gridx = (i % 2) * 2;
            c.gridy = i / 2;
            c.weightx = 0;
            JLabel k = UI.muted(e.getKey());
            detailsBox.add(k, c);
            c.gridx++;
            c.weightx = 1;
            detailsBox.add(UI.label(e.getValue() == null ? "-" : e.getValue()), c);
            i++;
        }
        detailsBox.revalidate();
        detailsBox.repaint();
    }

    private void save() {
        boolean ok = safe(() -> {
            // only fields the user is allowed to edit are read from the form
            if (user.canEdit(ProfileField.FULL_NAME)) {
                user.setFullName(Validator.required(fullNameField.getText(), "Full name"));
            }
            if (user.canEdit(ProfileField.EMAIL)) {
                user.setEmail(Validator.email(emailField.getText()));
            }
            if (user.canEdit(ProfileField.PHONE)) {
                user.setPhone(Validator.phone(phoneField.getText()));
            }
            if (user.canEdit(ProfileField.ADDRESS)) {
                user.setAddress(addressArea.getText().trim());
            }
            userDAO.updateProfile(user);
        });
        if (ok) {
            UI.info(this, "Profile updated successfully.");
            refresh();
            notifyUserChanged();
        }
    }

    private void changePicture() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images (png, jpg, gif)", "png", "jpg", "jpeg", "gif"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        boolean ok = safe(() -> {
            if (!FileUtil.isImage(f)) throw new ValidationException("Please choose a PNG, JPG or GIF image.");
            user.setProfilePicture(FileUtil.copyToUploads(f, FileUtil.PROFILE_PICTURES));
            userDAO.updateProfile(user);
        });
        if (ok) {
            refresh();
            notifyUserChanged();
        }
    }

    private void changePassword() {
        JPasswordField current = UI.passwordField(18);
        JPasswordField next = UI.passwordField(18);
        JPasswordField confirm = UI.passwordField(18);
        JComponent form = UI.form(new String[]{"Current password", "New password", "Confirm"}, current, next, confirm);
        form.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        if (JOptionPane.showConfirmDialog(this, form, "Change password", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        boolean ok = safe(() -> {
            if (!userDAO.verifyPassword(user.getUserId(), PasswordUtil.hash(new String(current.getPassword())))) {
                throw new ValidationException("Current password is wrong.");
            }
            String pw = Validator.password(new String(next.getPassword()));
            if (!pw.equals(new String(confirm.getPassword()))) {
                throw new ValidationException("New passwords do not match.");
            }
            userDAO.changePassword(user.getUserId(), PasswordUtil.hash(pw));
        });
        if (ok) UI.info(this, "Password changed.");
    }
}
