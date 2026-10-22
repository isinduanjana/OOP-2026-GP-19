package com.ruhuna.fams.controllers;

import com.ruhuna.fams.config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserController {

    public List<Object[]> getAllUsers() {
        List<Object[]> usersList = new ArrayList<>();
        String sql = "SELECT u.user_id, p.full_name, u.username, u.role " +
                "FROM users u LEFT JOIN user_profiles p ON u.user_id = p.user_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usersList.add(new Object[]{
                        String.format("%02d", rs.getInt("user_id")),
                        rs.getString("full_name") != null ? rs.getString("full_name") : "N/A",
                        rs.getString("username"),
                        rs.getString("role"),
                        "Edit / Delete"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usersList;
    }

    public boolean addUser(String fullName, String username, String password, String email, String role, int deptId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert into users table
            String userSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement pstmtUser = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            pstmtUser.setString(1, username);
            pstmtUser.setString(2, password);
            pstmtUser.setString(3, role);
            pstmtUser.executeUpdate();

            ResultSet rs = pstmtUser.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) userId = rs.getInt(1);

            // 2. Insert into user_profiles table
            String profileSql = "INSERT INTO user_profiles (user_id, full_name, email) VALUES (?, ?, ?)";
            PreparedStatement pstmtProfile = conn.prepareStatement(profileSql);
            pstmtProfile.setInt(1, userId);
            pstmtProfile.setString(2, fullName);
            pstmtProfile.setString(3, email);
            pstmtProfile.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        }
    }
}