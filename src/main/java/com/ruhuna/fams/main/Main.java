package com.ruhuna.fams.main;

import com.ruhuna.fams.config.DBConnection;
import com.ruhuna.fams.views.LoginView;
import javax.swing.SwingUtilities;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Faculty Academic Management System (Full 4-Member Project) ---");
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            System.out.println("Database Connection Successful!");
        } else {
            System.out.println("Database Connection Failed!");
        }

        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}
