package com.ruhuna.fams.views.to;

import javax.swing.*;
import java.awt.*;

public class TOMedicalView extends JPanel {
    public TOMedicalView() {
        setLayout(new GridLayout(6, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        add(new JLabel("Student ID (TG No):"));
        JTextField txtStudent = new JTextField("TG2144");
        txtStudent.setEditable(false);
        add(txtStudent);

        add(new JLabel("Date:"));
        add(new JTextField("2026-10-15"));

        add(new JLabel("Reason:"));
        add(new JTextField("Medical Leave"));

        add(new JLabel("Status:"));
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"PENDING", "APPROVED", "REJECTED"});
        add(cmbStatus);

        JButton btnApprove = new JButton("APPROVE");
        btnApprove.setBackground(new Color(46, 204, 113));
        btnApprove.setForeground(Color.WHITE);

        JButton btnReject = new JButton("REJECT");
        btnReject.setBackground(new Color(231, 76, 60));
        btnReject.setForeground(Color.WHITE);

        add(btnApprove);
        add(btnReject);

        btnApprove.addActionListener(e -> JOptionPane.showMessageDialog(this, "Medical Record APPROVED!"));
        btnReject.addActionListener(e -> JOptionPane.showMessageDialog(this, "Medical Record REJECTED!"));
    }
}
