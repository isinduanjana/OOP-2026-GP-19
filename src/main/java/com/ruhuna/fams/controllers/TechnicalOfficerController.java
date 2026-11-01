package com.ruhuna.fams.controllers;

import com.ruhuna.fams.config.DBConnection;
import com.ruhuna.fams.models.TechnicalOfficer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TechnicalOfficerController {

    public List<Object[]> getAttendanceRecords() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT a.attendance_id, " +
                     "COALESCE(u.tg_no, CONCAT('TG', a.student_id)) AS tg_number, " +
                     "a.status, a.medical_id " +
                     "FROM attendance a " +
                     "LEFT JOIN undergraduates u ON a.student_id = u.student_id " +
                     "ORDER BY a.attendance_id DESC LIMIT 50";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("attendance_id"),
                    rs.getString("tg_number"),
                    rs.getString("status"),
                    rs.getObject("medical_id") != null ? rs.getInt("medical_id") : "None"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getDetailedAttendance() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT a.attendance_id, " +
                     "COALESCE(u.tg_no, CONCAT('TG', a.student_id)) AS tg_no, " +
                     "COALESCE(p.full_name, 'N/A') AS student_name, " +
                     "COALESCE(c.course_code, 'N/A') AS course_code, " +
                     "COALESCE(s.component, 'THEORY') AS component, " +
                     "COALESCE(s.session_date, CURRENT_DATE) AS session_date, " +
                     "a.status, " +
                     "CASE WHEN a.medical_id IS NOT NULL THEN 'Medical Attached' ELSE 'None' END AS medical_info " +
                     "FROM attendance a " +
                     "LEFT JOIN undergraduates u ON a.student_id = u.student_id " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                     "LEFT JOIN attendance_sessions s ON a.session_id = s.session_id " +
                     "LEFT JOIN courses c ON s.course_id = c.course_id " +
                     "ORDER BY a.attendance_id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("attendance_id"),
                    rs.getString("tg_no"),
                    rs.getString("student_name"),
                    rs.getString("course_code"),
                    rs.getString("component"),
                    rs.getString("session_date"),
                    rs.getString("status"),
                    rs.getString("medical_info")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveAttendance(int sessionId, int studentId, String status) {
        String sql = "INSERT INTO attendance (session_id, student_id, status) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status = VALUES(status)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            pstmt.setInt(2, studentId);
            pstmt.setString(3, status);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAttendanceStatus(int attendanceId, String status) {
        String sql = "UPDATE attendance SET status = ? WHERE attendance_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, attendanceId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAttendance(int attendanceId) {
        String sql = "DELETE FROM attendance WHERE attendance_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, attendanceId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> getAllSessions() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT s.session_id, c.course_code, c.course_name, s.session_code, " +
                     "s.component, s.session_number, s.session_date " +
                     "FROM attendance_sessions s " +
                     "JOIN courses c ON s.course_id = c.course_id " +
                     "ORDER BY s.session_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("session_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getString("session_code"),
                    rs.getString("component"),
                    rs.getInt("session_number"),
                    rs.getString("session_date")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addAttendanceSession(int courseId, String sessionCode, String component,
                                        int sessionNumber, int totalSessions, String sessionDate) {
        String sql = "INSERT INTO attendance_sessions (course_id, session_code, component, " +
                     "session_number, total_sessions, session_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            pstmt.setString(2, sessionCode);
            pstmt.setString(3, component);
            pstmt.setInt(4, sessionNumber);
            pstmt.setInt(5, totalSessions);
            pstmt.setDate(6, Date.valueOf(sessionDate));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> getAllMedicals() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT m.medical_id, " +
                     "COALESCE(u.tg_no, CONCAT('TG', m.student_id)) AS tg_no, " +
                     "COALESCE(p.full_name, 'Unknown Student') AS student_name, " +
                     "COALESCE(c.course_code, 'N/A') AS course_code, " +
                     "m.medical_date, m.reason, m.status, " +
                     "COALESCE(m.document_path, 'No Document') AS doc_path " +
                     "FROM medicals m " +
                     "LEFT JOIN undergraduates u ON m.student_id = u.student_id " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                     "LEFT JOIN courses c ON m.course_id = c.course_id " +
                     "ORDER BY m.medical_id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("medical_id"),
                    rs.getString("tg_no"),
                    rs.getString("student_name"),
                    rs.getString("course_code"),
                    rs.getString("medical_date"),
                    rs.getString("reason"),
                    rs.getString("status"),
                    rs.getString("doc_path")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateMedicalStatus(int medicalId, String status, int approvedByUserId) {
        String sql = "UPDATE medicals SET status = ?, approved_by = ?, approved_at = NOW() WHERE medical_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            if (approvedByUserId > 0) {
                pstmt.setInt(2, approvedByUserId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setInt(3, medicalId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean approveMedical(int medicalId, int approvedByUserId) {
        return updateMedicalStatus(medicalId, "APPROVED", approvedByUserId);
    }

    public boolean rejectMedical(int medicalId, int approvedByUserId) {
        return updateMedicalStatus(medicalId, "REJECTED", approvedByUserId);
    }

    public TechnicalOfficer getTechnicalOfficerByUserId(int userId) {
        String sql = "SELECT t.to_id, t.employee_no, p.full_name, p.email, d.department_name " +
                     "FROM tech_officers t " +
                     "LEFT JOIN user_profiles p ON t.user_id = p.user_id " +
                     "LEFT JOIN departments d ON t.department_id = d.department_id " +
                     "WHERE t.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    TechnicalOfficer to = new TechnicalOfficer(
                        rs.getString("employee_no"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("department_name")
                    );
                    to.setId(rs.getInt("to_id"));
                    return to;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String fallbackSql = "SELECT u.user_id, u.username, p.full_name, p.email " +
                             "FROM users u " +
                             "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                             "WHERE u.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(fallbackSql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("full_name") != null ? rs.getString("full_name") : rs.getString("username");
                    TechnicalOfficer to = new TechnicalOfficer(
                        "TO-" + rs.getInt("user_id"),
                        name,
                        rs.getString("email"),
                        "Main Computer Lab"
                    );
                    to.setId(rs.getInt("user_id"));
                    return to;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateProfile(int userId, String fullName, String email, String phone, String address) {
        String sql = "INSERT INTO user_profiles (user_id, full_name, email, phone, address) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "full_name = VALUES(full_name), " +
                     "email = VALUES(email), " +
                     "phone = VALUES(phone), " +
                     "address = VALUES(address)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, fullName);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setString(5, address);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> getTimetables() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT t.timetable_id, c.course_code, c.course_name, t.day, " +
                     "t.start_time, t.end_time, t.room, t.component " +
                     "FROM timetables t " +
                     "JOIN courses c ON t.course_id = c.course_id " +
                     "ORDER BY t.day, t.start_time";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("timetable_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getString("day"),
                    rs.getString("start_time") + " - " + rs.getString("end_time"),
                    rs.getString("room"),
                    rs.getString("component")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getNotices() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT title, description, created_at FROM notices " +
                     "WHERE target_role IN ('ALL', 'TO') AND status = 'ACTIVE' " +
                     "ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getAllStudents() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.student_id, u.tg_no, COALESCE(p.full_name, u.tg_no) AS full_name, u.batch " +
                     "FROM undergraduates u " +
                     "LEFT JOIN user_profiles p ON u.user_id = p.user_id " +
                     "ORDER BY u.tg_no";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("student_id"),
                    rs.getString("tg_no"),
                    rs.getString("full_name"),
                    rs.getString("batch")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getAllCourses() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT course_id, course_code, course_name FROM courses ORDER BY course_code";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getAttendanceCount() {
        return getCount("SELECT COUNT(*) FROM attendance");
    }

    public int getPendingMedicalCount() {
        return getCount("SELECT COUNT(*) FROM medicals WHERE status = 'PENDING'");
    }

    public int getTotalStudentsCount() {
        return getCount("SELECT COUNT(*) FROM undergraduates");
    }

    public int getTotalSessionsCount() {
        return getCount("SELECT COUNT(*) FROM attendance_sessions");
    }

    private int getCount(String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
