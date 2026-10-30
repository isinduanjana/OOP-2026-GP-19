package com.ruhuna.fams.views.student;

import com.ruhuna.fams.config.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class StudentProfilePanel extends JPanel {
    private final int userId;
    private final JTextField username = new JTextField(24);
    private final JTextField tgNo = new JTextField(24);
    private final JTextField batch = new JTextField(24);
    private final JTextField semester = new JTextField(24);
    private final JTextField fullName = new JTextField(24);
    private final JTextField email = new JTextField(24);
    private final JTextField phone = new JTextField(24);
    private final JTextArea address = new JTextArea(3, 24);

    public StudentProfilePanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(12, 12));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8); g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, g, 0, "Username (read only):", username);
        addRow(form, g, 1, "Registration / TG No (read only):", tgNo);
        addRow(form, g, 2, "Batch (read only):", batch);
        addRow(form, g, 3, "Current Semester (read only):", semester);
        addRow(form, g, 4, "Full Name:", fullName);
        addRow(form, g, 5, "Email:", email);
        addRow(form, g, 6, "Phone:", phone);
        addRow(form, g, 7, "Address:", new JScrollPane(address));
        username.setEditable(false); tgNo.setEditable(false); batch.setEditable(false); semester.setEditable(false);
        JButton refresh = new JButton("Reload Profile");
        JButton save = new JButton("Update Profile");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actions.add(refresh); actions.add(save);
        add(new JLabel("  My Student Profile", SwingConstants.LEFT), BorderLayout.NORTH);
        add(form, BorderLayout.CENTER); add(actions, BorderLayout.SOUTH);
        refresh.addActionListener(e -> loadProfile());
        save.addActionListener(e -> updateProfile());
        loadProfile();
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, Component field) {
        g.gridx=0; g.gridy=row; g.weightx=0; p.add(new JLabel(label),g);
        g.gridx=1; g.weightx=1; p.add(field,g);
    }

    private void loadProfile() {
        String sql = "SELECT u.username, up.full_name, up.email, up.phone, up.address, " +
                "ug.tg_no, ug.batch, ug.semester FROM users u " +
                "LEFT JOIN user_profiles up ON up.user_id=u.user_id " +
                "LEFT JOIN undergraduates ug ON ug.user_id=u.user_id WHERE u.user_id=?";
        try (Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            if (c == null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            ps.setInt(1,userId);
            try (ResultSet rs=ps.executeQuery()) {
                if (rs.next()) {
                    username.setText(value(rs,"username")); fullName.setText(value(rs,"full_name"));
                    email.setText(value(rs,"email")); phone.setText(value(rs,"phone"));
                    address.setText(value(rs,"address")); tgNo.setText(value(rs,"tg_no"));
                    batch.setText(value(rs,"batch")); semester.setText(value(rs,"semester"));
                } else message("Student profile was not found.");
            }
        } catch (SQLException ex) { error(ex); }
    }

    private void updateProfile() {
        if (fullName.getText().trim().isEmpty()) { message("Full name is required."); return; }
        String em=email.getText().trim();
        if (!em.isEmpty() && !em.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            message("Enter a valid email address."); return;
        }
        String sql = "INSERT INTO user_profiles(user_id,full_name,email,phone,address) VALUES(?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), email=VALUES(email), phone=VALUES(phone), address=VALUES(address)";
        try (Connection c=DBConnection.getConnection()) {
            if (c == null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            try (PreparedStatement ps=c.prepareStatement(sql)) {
                ps.setInt(1,userId); ps.setString(2,fullName.getText().trim());
                ps.setString(3,em.isEmpty()?null:em);
                ps.setString(4,phone.getText().trim().isEmpty()?null:phone.getText().trim());
                ps.setString(5,address.getText().trim().isEmpty()?null:address.getText().trim());
                ps.executeUpdate(); message("Profile updated successfully.");
            }
        } catch (SQLException ex) { error(ex); }
    }
    private String value(ResultSet rs, String col) throws SQLException {
        Object v=rs.getObject(col); return v==null?"":String.valueOf(v);
    }
    private void message(String s) { JOptionPane.showMessageDialog(this,s); }
    private void error(Exception ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE); }
}
