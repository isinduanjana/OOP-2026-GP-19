package com.ruhuna.fams.views.lecturer;

import com.ruhuna.fams.controllers.LecturerController;
import com.ruhuna.fams.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class LecturerTableView extends JPanel {
    private final User user;
    private final String type;
    private final LecturerController controller = new LecturerController();
    private final JComboBox<CourseChoice> courseBox = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel();
    private final JTable table = new JTable(model);

    public LecturerTableView(User user, String type) {
        this.user=user; this.type=type;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JLabel title=new JLabel(type);
        title.setFont(new Font("SansSerif",Font.BOLD,18));
        JPanel top=new JPanel(new BorderLayout());
        top.add(title,BorderLayout.WEST);
        JButton refresh=new JButton("Refresh");
        top.add(refresh,BorderLayout.EAST);
        if (!type.equals("Notices") && !type.equals("Undergraduate Details")) {
            top.add(courseBox,BorderLayout.CENTER);
        }
        add(top,BorderLayout.NORTH);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table),BorderLayout.CENTER);
        refresh.addActionListener(e->loadData());
        courseBox.addActionListener(e->loadData());
        loadCourses();
        loadData();
    }

    private void loadCourses() {
        try {
            for(Object[] row:controller.getCourses(user.getUserId())) {
                courseBox.addItem(new CourseChoice((Integer)row[0],row[1]+" - "+row[2]));
            }
        } catch(SQLException e) { showError(e); }
    }

    private void loadData() {
        try {
            List<Object[]> rows;
            String[] columns;
            if(type.equals("Undergraduate Details")) {
                columns=new String[]{"TG Number","Student Name","Email","Phone","Batch","Semester","Student Type","Course"};
                rows=controller.getStudentDetails(user.getUserId());
            } else if(type.equals("Notices")) {
                columns=new String[]{"Title","Description","Published"};
                rows=controller.getNotices();
            } else {
                CourseChoice choice=(CourseChoice)courseBox.getSelectedItem();
                if(choice==null) { model.setRowCount(0); return; }
                if(type.equals("Eligibility Check")) {
                    columns=new String[]{"TG Number","Student Name","Attendance","CA Marks","Eligibility"};
                    rows=controller.getEligibility(choice.id);
                } else if(type.equals("Marks, Grades and GPA")) {
                    columns=new String[]{"TG Number","Student Name","Final Marks","Grade","Grade Point","SGPA","CGPA"};
                    rows=controller.getAcademicResults(choice.id);
                } else if(type.equals("Attendance Records")) {
                    columns=new String[]{"TG Number","Student Name","Component","Date","Attendance","Medical Status"};
                    rows=controller.getAttendance(choice.id);
                } else if(type.equals("Medical Records")) {
                    columns=new String[]{"TG Number","Student Name","Date","Reason","Status"};
                    rows=controller.getMedicals(choice.id);
                } else {
                    columns=new String[]{"Course Code","Course Name","Credits"};
                    rows=controller.getCourses(user.getUserId());
                    for(int i=0;i<rows.size();i++) {
                        Object[] old=rows.get(i);
                        rows.set(i,new Object[]{old[1],old[2],old[3]});
                    }
                }
            }
            model.setDataVector(rows.toArray(new Object[0][]),columns);
        } catch(SQLException e) { showError(e); }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this,e.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);
    }

    private static class CourseChoice {
        int id; String label;
        CourseChoice(int id,String label){this.id=id;this.label=label;}
        public String toString(){return label;}
    }
}
