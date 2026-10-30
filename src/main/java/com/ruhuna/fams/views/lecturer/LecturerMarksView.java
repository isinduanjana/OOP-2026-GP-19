package com.ruhuna.fams.views.lecturer;

import com.ruhuna.fams.controllers.LecturerController;
import com.ruhuna.fams.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class LecturerMarksView extends JPanel {
    private final User user;
    private final LecturerController controller=new LecturerController();
    private final JComboBox<CourseChoice> courses=new JComboBox<>();
    private final JComboBox<IdChoice> assessments=new JComboBox<>();
    private final JComboBox<IdChoice> students=new JComboBox<>();
    private final JTextField markField=new JTextField(8);
    private final DefaultTableModel model=new DefaultTableModel(
            new String[]{"TG Number","Student Name","Assessment","Mark","Maximum"},0);
    private final JTable table=new JTable(model);

    public LecturerMarksView(User user) {
        this.user=user;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JLabel title=new JLabel("Marks Management");
        title.setFont(new Font("SansSerif",Font.BOLD,18));
        add(title,BorderLayout.NORTH);

        JPanel form=new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Course:")); form.add(courses);
        form.add(new JLabel("Assessment:")); form.add(assessments);
        form.add(new JLabel("Student:")); form.add(students);
        form.add(new JLabel("Mark:")); form.add(markField);
        JButton save=new JButton("Save Mark");
        JButton refresh=new JButton("Refresh");
        form.add(save); form.add(refresh);
        add(form,BorderLayout.SOUTH);
        add(new JScrollPane(table),BorderLayout.CENTER);

        courses.addActionListener(e->loadCourseData());
        save.addActionListener(e->saveMark());
        refresh.addActionListener(e->loadMarks());
        loadCourses();
    }

    private void loadCourses() {
        try {
            for(Object[] row:controller.getCourses(user.getUserId()))
                courses.addItem(new CourseChoice((Integer)row[0],row[1]+" - "+row[2]));
            loadCourseData();
        } catch(SQLException e){error(e);}
    }

    private void loadCourseData() {
        assessments.removeAllItems(); students.removeAllItems();
        CourseChoice course=(CourseChoice)courses.getSelectedItem();
        if(course==null){model.setRowCount(0);return;}
        try {
            for(Object[] row:controller.getAssessments(course.id))
                assessments.addItem(new IdChoice((Integer)row[0],row[1]+" (max "+row[2]+")"));
            for(Object[] row:controller.getEnrolledStudents(course.id))
                students.addItem(new IdChoice((Integer)row[0],row[1]+" - "+row[2]));
            loadMarks();
        } catch(SQLException e){error(e);}
    }

    private void saveMark() {
        CourseChoice c=(CourseChoice)courses.getSelectedItem();
        IdChoice a=(IdChoice)assessments.getSelectedItem();
        IdChoice s=(IdChoice)students.getSelectedItem();
        if(c==null||a==null||s==null){JOptionPane.showMessageDialog(this,"Select a course, assessment and student.");return;}
        try {
            double mark=Double.parseDouble(markField.getText().trim());
            controller.saveMark(user.getUserId(),a.id,s.id,mark);
            JOptionPane.showMessageDialog(this,"Mark saved successfully.");
            markField.setText(""); loadMarks();
        } catch(NumberFormatException e){JOptionPane.showMessageDialog(this,"Enter a valid number for the mark.");}
          catch(SQLException e){error(e);}
    }

    private void loadMarks() {
        CourseChoice c=(CourseChoice)courses.getSelectedItem();
        if(c==null)return;
        try {
            List<Object[]> rows=controller.getMarks(c.id);
            model.setRowCount(0);
            for(Object[] row:rows) model.addRow(row);
        } catch(SQLException e){error(e);}
    }
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    private static class CourseChoice {
        int id;String label;CourseChoice(int id,String label){this.id=id;this.label=label;}
        public String toString(){return label;}
    }
    private static class IdChoice {
        int id;String label;IdChoice(int id,String label){this.id=id;this.label=label;}
        public String toString(){return label;}
    }
}
