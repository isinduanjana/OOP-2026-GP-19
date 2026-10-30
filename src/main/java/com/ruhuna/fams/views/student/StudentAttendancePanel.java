package com.ruhuna.fams.views.student;

import com.ruhuna.fams.config.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*; 

public class StudentAttendancePanel extends JPanel {
    private final int userId;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Course Code","Course Name","Date","Component","Session","Status","Medical Record"},0) {
        @Override public boolean isCellEditable(int r,int c){return false;}
    };
    public StudentAttendancePanel(int userId) {
        this.userId=userId; setLayout(new BorderLayout(8,8));
        JButton refresh=new JButton("Refresh Attendance"); refresh.addActionListener(e->load());
        add(refresh,BorderLayout.NORTH); add(new JScrollPane(new JTable(model)),BorderLayout.CENTER); load();
    }
    private void load() {
        model.setRowCount(0);
        String sql="SELECT c.course_code,c.course_name,s.session_date,s.component,s.session_number,a.status, " +
                "COALESCE(CAST(a.medical_id AS CHAR),'—') AS medical_ref " +
                "FROM undergraduates ug JOIN attendance a ON a.student_id=ug.student_id " +
                "JOIN attendance_sessions s ON s.session_id=a.session_id " +
                "JOIN courses c ON c.course_id=s.course_id WHERE ug.user_id=? ORDER BY s.session_date DESC,s.session_number DESC";
        try (Connection c=DBConnection.getConnection()) {
            if(c==null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            try(PreparedStatement ps=c.prepareStatement(sql)) {
                ps.setInt(1,userId); try(ResultSet rs=ps.executeQuery()) {
                    while(rs.next()) model.addRow(new Object[]{rs.getString("course_code"),rs.getString("course_name"),
                            rs.getDate("session_date"),rs.getString("component"),rs.getInt("session_number"),
                            rs.getString("status"),rs.getString("medical_ref")});
                }
            }
        } catch(SQLException ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"Attendance Error",JOptionPane.ERROR_MESSAGE); }
    }
}
