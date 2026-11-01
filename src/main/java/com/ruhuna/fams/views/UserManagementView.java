package com.ruhuna.fams.views;

import com.ruhuna.fams.controllers.UserController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private UserController userController;

    public UserManagementView() {
        userController = new UserController();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Header Section
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("User Management");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));

        JButton btnAddUser = new JButton("+ Add User");
        btnAddUser.setBackground(new Color(46, 204, 113));
        btnAddUser.setForeground(Color.WHITE);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(btnAddUser, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Search Section
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search User: "));
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("SEARCH");
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        // Table Section
        String[] columns = {"ID", "Name", "Username", "Role", "Action"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Load Users Data
        loadUserData();

        // Add User Button Click Event (Wireframe 04 Dialog)
        btnAddUser.addActionListener(e -> showAddUserDialog());
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        List<Object[]> users = userController.getAllUsers();
        for (Object[] row : users) {
            tableModel.addRow(row);
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "ADD USER", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(7, 2, 10, 10));

        JTextField txtName = new JTextField();
        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JTextField txtEmail = new JTextField();
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"ADMIN", "LECTURER", "TO", "UNDERGRADUATE"});
        JComboBox<String> cmbDept = new JComboBox<>(new String[]{"1 - ICT", "2 - ET", "3 - BST"});

        dialog.add(new JLabel(" Full Name:"));
        dialog.add(txtName);
        dialog.add(new JLabel(" Username:"));
        dialog.add(txtUsername);
        dialog.add(new JLabel(" Password:"));
        dialog.add(txtPassword);
        dialog.add(new JLabel(" Email:"));
        dialog.add(txtEmail);
        dialog.add(new JLabel(" Role:"));
        dialog.add(cmbRole);
        dialog.add(new JLabel(" Department:"));
        dialog.add(cmbDept);

        JButton btnSave = new JButton("SAVE");
        JButton btnCancel = new JButton("CANCEL");

        dialog.add(btnSave);
        dialog.add(btnCancel);

        btnSave.addActionListener(e -> {
            boolean success = userController.addUser(
                    txtName.getText(),
                    txtUsername.getText(),
                    new String(txtPassword.getPassword()),
                    txtEmail.getText(),
                    cmbRole.getSelectedItem().toString(),
                    cmbDept.getSelectedIndex() + 1
            );
            if (success) {
                JOptionPane.showMessageDialog(dialog, "User added successfully!");
                loadUserData();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add user!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
}