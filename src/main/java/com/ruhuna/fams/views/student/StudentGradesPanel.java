package com.ruhuna.fams.views.student;

import com.ruhuna.fams.config.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentGradesPanel extends JPanel {
    private final int userId;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Course Code","Course Name","Credits","Total Marks","Grade","Grade Point","Stored CGPA"},0) {
        @Override public boolean isCellEditable(int r,int c){return false;}
    };
    public StudentGradesPanel(int userId) {
        this.userId=userId; setLayout(new BorderLayout(8,8));
        JButton refresh=new JButton("Refresh Grades"); refresh.addActionListener(e->load());
        add(refresh,BorderLayout.NORTH); add(new JScrollPane(new JTable(model)),BorderLayout.CENTER); load();
    }
    private void load() {
        model.setRowCount(0);
        String sql="SELECT c.course_code,c.course_name,c.credits,g.total_marks,g.grade,g.grade_point,g.cgpa " +
                "FROM undergraduates ug JOIN grades g ON g.student_id=ug.student_id " +
                "JOIN courses c ON c.course_id=g.course_id WHERE ug.user_id=? ORDER BY c.course_code";
        try(Connection c=DBConnection.getConnection()) {
            if(c==null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            try(PreparedStatement ps=c.prepareStatement(sql)) {
                ps.setInt(1,userId); try(ResultSet rs=ps.executeQuery()) {
                    while(rs.next()) model.addRow(new Object[]{rs.getString("course_code"),rs.getString("course_name"),
                            rs.getInt("credits"),rs.getObject("total_marks"),rs.getString("grade"),
                            rs.getObject("grade_point"),rs.getObject("cgpa")});
                }
            }
        } catch(SQLException ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"Grades Error",JOptionPane.ERROR_MESSAGE); }
    }
}
