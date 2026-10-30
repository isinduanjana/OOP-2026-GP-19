package com.ruhuna.fams.views.student;

import com.ruhuna.fams.config.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Calculates GPA from the existing grades + enrollments + courses schema. */
public class StudentGpaPanel extends JPanel {
    private final int userId;
    private final JComboBox<String> academicYear = new JComboBox<>();
    private final JComboBox<Integer> semester = new JComboBox<>();
    private final JLabel sgpaLabel = new JLabel("SGPA: —");
    private final JLabel cgpaLabel = new JLabel("CGPA: —");
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Academic Year","Semester","Credits","Weighted Grade Points","GPA"},0) {
        @Override public boolean isCellEditable(int r,int c){return false;}
    };
    public StudentGpaPanel(int userId) {
        this.userId=userId; setLayout(new BorderLayout(10,10));
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Academic Year:")); top.add(academicYear); top.add(new JLabel("Semester:")); top.add(semester);
        JButton calc=new JButton("Calculate SGPA / CGPA"); top.add(calc); top.add(sgpaLabel); top.add(cgpaLabel);
        add(top,BorderLayout.NORTH); add(new JScrollPane(new JTable(model)),BorderLayout.CENTER);
        academicYear.addActionListener(e->loadSemesters()); semester.addActionListener(e->calculate()); calc.addActionListener(e->calculate());
        loadAcademicYears();
    }
    private void loadAcademicYears() {
        academicYear.removeAllItems();
        String sql="SELECT DISTINCT e.academic_year FROM enrollments e JOIN undergraduates ug ON ug.student_id=e.student_id WHERE ug.user_id=? ORDER BY e.academic_year";
        try(Connection c=DBConnection.getConnection()) {
            if(c==null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            try(PreparedStatement ps=c.prepareStatement(sql)) {
                ps.setInt(1,userId); try(ResultSet rs=ps.executeQuery()) { while(rs.next()) academicYear.addItem(rs.getString(1)); }
            }
            loadSemesters();
        } catch(SQLException ex) { error(ex); }
    }
    private void loadSemesters() {
        String year=(String)academicYear.getSelectedItem(); if(year==null) return;
        Object old=semester.getSelectedItem(); semester.removeAllItems();
        String sql="SELECT DISTINCT e.semester FROM enrollments e JOIN undergraduates ug ON ug.student_id=e.student_id WHERE ug.user_id=? AND e.academic_year=? ORDER BY e.semester";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)) {
            if(c==null) throw new SQLException("Could not connect to database.");
            ps.setInt(1,userId); ps.setString(2,year); try(ResultSet rs=ps.executeQuery()) { while(rs.next()) semester.addItem(rs.getInt(1)); }
            if(old instanceof Integer && containsSemester((Integer)old)) semester.setSelectedItem(old);
            calculate();
        } catch(SQLException ex) { error(ex); }
    }
    private boolean containsSemester(Integer v) { for(int i=0;i<semester.getItemCount();i++) if(semester.getItemAt(i).equals(v)) return true; return false; }
    private void calculate() {
        String year=(String)academicYear.getSelectedItem(); Integer sem=(Integer)semester.getSelectedItem();
        if(year==null || sem==null) { model.setRowCount(0); sgpaLabel.setText("SGPA: N/A"); cgpaLabel.setText("CGPA: N/A"); return; }
        model.setRowCount(0); BigDecimal totalWeighted=BigDecimal.ZERO; int totalCredits=0; BigDecimal selectedWeighted=BigDecimal.ZERO; int selectedCredits=0;
        String sql="SELECT e.academic_year,e.semester,c.credits,g.grade_point FROM undergraduates ug " +
                "JOIN enrollments e ON e.student_id=ug.student_id AND e.status IN ('ENROLLED','COMPLETED') " +
                "JOIN courses c ON c.course_id=e.course_id LEFT JOIN grades g ON g.student_id=e.student_id AND g.course_id=e.course_id " +
                "WHERE ug.user_id=? ORDER BY e.academic_year,e.semester,c.course_code";
        Map<String,BigDecimal[]> groups=new LinkedHashMap<>();
        try(Connection c=DBConnection.getConnection()) {
            if(c==null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            try(PreparedStatement ps=c.prepareStatement(sql)) {
                ps.setInt(1,userId); try(ResultSet rs=ps.executeQuery()) {
                    while(rs.next()) {
                        String y=rs.getString("academic_year"); int s=rs.getInt("semester"); int cr=rs.getInt("credits"); BigDecimal gp=rs.getBigDecimal("grade_point");
                        if(gp==null || cr<=0) continue; // No published grade: do not include it in GPA.
                        BigDecimal weighted=gp.multiply(BigDecimal.valueOf(cr));
                        String key=y+"|"+s; BigDecimal[] pair=groups.computeIfAbsent(key,k->new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO});
                        pair[0]=pair[0].add(BigDecimal.valueOf(cr)); pair[1]=pair[1].add(weighted);
                        totalCredits+=cr; totalWeighted=totalWeighted.add(weighted);
                        if(y.equals(year) && s==sem) { selectedCredits+=cr; selectedWeighted=selectedWeighted.add(weighted); }
                    }
                }
            }
            for(Map.Entry<String,BigDecimal[]> entry:groups.entrySet()) {
                String[] parts=entry.getKey().split("\\|"); BigDecimal credits=entry.getValue()[0], weighted=entry.getValue()[1];
                BigDecimal gpa=weighted.divide(credits,2,RoundingMode.HALF_UP);
                model.addRow(new Object[]{parts[0],parts[1],credits.intValue(),weighted.setScale(2,RoundingMode.HALF_UP),gpa});
            }
            sgpaLabel.setText("SGPA: "+(selectedCredits==0?"N/A":selectedWeighted.divide(BigDecimal.valueOf(selectedCredits),2,RoundingMode.HALF_UP)));
            cgpaLabel.setText("CGPA: "+(totalCredits==0?"N/A":totalWeighted.divide(BigDecimal.valueOf(totalCredits),2,RoundingMode.HALF_UP)));
        } catch(SQLException ex) { error(ex); }
    }
    private void error(Exception ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"GPA Calculation Error",JOptionPane.ERROR_MESSAGE); }
}
