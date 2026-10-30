package com.ruhuna.fams.views;

import com.ruhuna.fams.controllers.NoticeController;
import com.ruhuna.fams.models.User;
import javax.swing.*;
import java.awt.*;

public class NoticeManagementView extends JPanel {
    private JTextField txtTitle;
    private JComboBox<String> cmbRole;
    private JTextArea txtDescription;
    private NoticeController noticeController;
    private User currentUser;

    public NoticeManagementView(User user) {
        this.currentUser = user;
        noticeController = new NoticeController();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblHeader = new JLabel("Notice Management", SwingConstants.CENTER);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(lblHeader, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        txtTitle = new JTextField(30);
        gbc.gridx = 1;
        formPanel.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Target Role:"), gbc);
        cmbRole = new JComboBox<>(new String[]{"ALL", "LECTURER", "TO", "UNDERGRADUATE"});
        gbc.gridx = 1;
        formPanel.add(cmbRole, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Description:"), gbc);
        txtDescription = new JTextArea(6, 30);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(txtDescription), gbc);

        JButton btnPublish = new JButton("PUBLISH");
        btnPublish.setBackground(new Color(155, 89, 182));
        btnPublish.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(btnPublish, gbc);

        add(formPanel, BorderLayout.CENTER);

        btnPublish.addActionListener(e -> {
            boolean success = noticeController.publishNotice(
                txtTitle.getText(),
                txtDescription.getText(),
                cmbRole.getSelectedItem().toString(),
                currentUser.getUserId()
            );
            if (success) {
                JOptionPane.showMessageDialog(this, "Notice published successfully!");
                txtTitle.setText("");
                txtDescription.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to publish notice!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
