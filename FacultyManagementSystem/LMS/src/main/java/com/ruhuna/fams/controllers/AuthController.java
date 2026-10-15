package com.ruhuna.fams.controllers;

import com.ruhuna.fams.config.DBConnection;
import com.ruhuna.fams.models.User;
import java.sql.*;

public class AuthController {

    public User login(String username, String password) {
        String sql = "SELECT u.user_id, u.username, u.role, p.full_name " +
                "FROM users u " +
                "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                "WHERE u.username = ? AND u.password = ? AND u.status = 'ACTIVE'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("full_name") != null ? rs.getString("full_name") : rs.getString("username")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}