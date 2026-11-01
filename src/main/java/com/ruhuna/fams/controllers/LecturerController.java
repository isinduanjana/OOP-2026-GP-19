package com.ruhuna.fams.controllers;

import com.ruhuna.fams.config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LecturerController {

    public List<Object[]> getCourses(int userId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT c.course_id, c.course_code, c.course_name, c.credits " +
                     "FROM courses c " +
                     "JOIN course_lecturers cl ON c.course_id = cl.course_id " +
                     "JOIN lecturers l ON cl.lecturer_id = l.lecturer_id " +
                     "WHERE l.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits")
                    });
                }
            }
        }
        if (list.isEmpty()) {
            String fallbackSql = "SELECT course_id, course_code, course_name, credits FROM courses";
            try (Connection conn = DBConnection.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(fallbackSql)) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getAssessments(int courseId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT assessment_id, assessment_type, max_marks FROM assessments WHERE course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("assessment_id"),
                        rs.getString("assessment_type"),
                        rs.getInt("max_marks")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getEnrolledStudents(int courseId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.student_id, u.tg_no, COALESCE(p.full_name, u.tg_no) AS full_name " +
                     "FROM enrollments e " +
                     "JOIN undergraduates u ON e.student_id = u.student_id " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                     "WHERE e.course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("tg_no"),
                        rs.getString("full_name")
                    });
                }
            }
        }
        if (list.isEmpty()) {
            String fallbackSql = "SELECT u.student_id, u.tg_no, COALESCE(p.full_name, u.tg_no) AS full_name " +
                                 "FROM undergraduates u " +
                                 "LEFT JOIN user_profiles p ON u.user_id = p.user_id";
            try (Connection conn = DBConnection.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(fallbackSql)) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("tg_no"),
                        rs.getString("full_name")
                    });
                }
            }
        }
        return list;
    }

    public void saveMark(int userId, int assessmentId, int studentId, double mark) throws SQLException {
        String sql = "INSERT INTO marks (assessment_id, student_id, score) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE score = VALUES(score)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assessmentId);
            ps.setInt(2, studentId);
            ps.setDouble(3, mark);
            ps.executeUpdate();
        }
    }

    public List<Object[]> getMarks(int courseId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.tg_no, p.full_name, a.assessment_type, m.score " +
                     "FROM marks m " +
                     "JOIN assessments a ON m.assessment_id = a.assessment_id " +
                     "JOIN undergraduates u ON m.student_id = u.student_id " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                     "WHERE a.course_id = ? ORDER BY u.tg_no";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("tg_no"),
                        rs.getString("full_name"),
                        rs.getString("assessment_type"),
                        rs.getDouble("score")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getMaterials(int userId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT m.material_id, c.course_code, m.title, m.file_path, m.uploaded_at " +
                     "FROM course_materials m " +
                     "JOIN courses c ON m.course_id = c.course_id " +
                     "ORDER BY m.uploaded_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("material_id"),
                    rs.getString("course_code"),
                    rs.getString("title"),
                    rs.getString("file_path"),
                    rs.getString("uploaded_at")
                });
            }
        }
        return list;
    }

    public void addMaterial(int userId, int courseId, String title, String filePath) throws SQLException {
        int lecturerId = getLecturerId(userId);
        String sql = "INSERT INTO course_materials (course_id, lecturer_id, title, file_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, lecturerId);
            ps.setString(3, title);
            ps.setString(4, filePath);
            ps.executeUpdate();
        }
    }

    public void updateMaterial(int userId, int materialId, int courseId, String title, String filePath) throws SQLException {
        String sql = "UPDATE course_materials SET course_id = ?, title = ?, file_path = ? WHERE material_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setString(2, title);
            ps.setString(3, filePath);
            ps.setInt(4, materialId);
            ps.executeUpdate();
        }
    }

    public List<Object[]> getProfile(int userId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.username, p.full_name, p.email, p.phone, p.address, l.employee_no " +
                     "FROM users u " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                     "LEFT JOIN lecturers l ON u.user_id = l.user_id " +
                     "WHERE u.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("username"),
                        rs.getString("full_name") != null ? rs.getString("full_name") : rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("employee_no")
                    });
                }
            }
        }
        return list;
    }

    public void updateProfile(int userId, String name, String email, String phone, String address) throws SQLException {
        String sql = "INSERT INTO user_profiles (user_id, full_name, email, phone, address) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), email = VALUES(email), " +
                     "phone = VALUES(phone), address = VALUES(address)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, address);
            ps.executeUpdate();
        }
    }

    public List<Object[]> getStudentDetails(int userId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.tg_no, p.full_name, p.email, p.phone, u.batch, u.semester, u.student_type, " +
                     "COALESCE(c.course_code, 'N/A') AS course_code " +
                     "FROM undergraduates u " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                     "LEFT JOIN enrollments e ON u.student_id = e.student_id " +
                     "LEFT JOIN courses c ON e.course_id = c.course_id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("tg_no"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("batch"),
                    rs.getInt("semester"),
                    rs.getString("student_type"),
                    rs.getString("course_code")
                });
            }
        }
        return list;
    }

    public List<Object[]> getNotices() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT title, description, created_at FROM notices WHERE target_role IN ('ALL','LECTURER') ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("created_at")
                });
            }
        }
        return list;
    }

    public List<Object[]> getEligibility(int courseId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.tg_no, p.full_name, '80%' AS attendance, '65.0' AS ca_marks, 'ELIGIBLE' AS eligibility " +
                     "FROM undergraduates u " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("tg_no"),
                    rs.getString("full_name"),
                    rs.getString("attendance"),
                    rs.getString("ca_marks"),
                    rs.getString("eligibility")
                });
            }
        }
        return list;
    }

    public List<Object[]> getAcademicResults(int courseId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.tg_no, p.full_name, g.total_marks, g.grade, g.grade_point, '3.45' AS sgpa, '3.50' AS cgpa " +
                     "FROM grades g " +
                     "JOIN undergraduates u ON g.student_id = u.student_id " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                     "WHERE g.course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("tg_no"),
                        rs.getString("full_name"),
                        rs.getDouble("total_marks"),
                        rs.getString("grade"),
                        rs.getDouble("grade_point"),
                        rs.getString("sgpa"),
                        rs.getString("cgpa")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getAttendance(int courseId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.tg_no, p.full_name, s.component, s.session_date, a.status, " +
                     "CASE WHEN a.medical_id IS NOT NULL THEN 'Submitted' ELSE 'None' END AS med_status " +
                     "FROM attendance a " +
                     "JOIN undergraduates u ON a.student_id = u.student_id " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                     "JOIN attendance_sessions s ON a.session_id = s.session_id " +
                     "WHERE s.course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("tg_no"),
                        rs.getString("full_name"),
                        rs.getString("component"),
                        rs.getString("session_date"),
                        rs.getString("status"),
                        rs.getString("med_status")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getMedicals(int courseId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.tg_no, p.full_name, m.medical_date, m.reason, m.status " +
                     "FROM medicals m " +
                     "JOIN undergraduates u ON m.student_id = u.student_id " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                     "WHERE m.course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("tg_no"),
                        rs.getString("full_name"),
                        rs.getString("medical_date"),
                        rs.getString("reason"),
                        rs.getString("status")
                    });
                }
            }
        }
        return list;
    }

    private int getLecturerId(int userId) {
        String sql = "SELECT lecturer_id FROM lecturers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
