package com.ruhuna.fams.views.lecturer;

import com.ruhuna.fams.controllers.LecturerController;
import com.ruhuna.fams.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class LecturerMaterialsView extends JPanel {
    private final User user;
    private final LecturerController controller=new LecturerController();
    private final JComboBox<CourseChoice> courses=new JComboBox<>();
    private final JTextField title=new JTextField(18), path=new JTextField(22);
    private final DefaultTableModel model=new DefaultTableModel(
            new String[]{"ID","Course","Title","File Path","Uploaded"},0);
    private final JTable table=new JTable(model);

    public LecturerMaterialsView(User user) {
        this.user=user;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JLabel heading=new JLabel("Course Learning Materials");
        heading.setFont(new Font("SansSerif",Font.BOLD,18));
        add(heading,BorderLayout.NORTH);
        JPanel form=new JPanel(new GridLayout(3,2,8,8));
        form.add(new JLabel("Course:")); form.add(courses);
        form.add(new JLabel("Material title:")); form.add(title);
        JPanel filePanel=new JPanel(new BorderLayout(5,0));
        filePanel.add(path,BorderLayout.CENTER);
        JButton browse=new JButton("Browse");
        filePanel.add(browse,BorderLayout.EAST);
        form.add(new JLabel("File:")); form.add(filePanel);
        JButton add=new JButton("Add Material"), update=new JButton("Update Selected");
        JButton refresh=new JButton("Refresh");
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(add);actions.add(update);actions.add(refresh);
        JPanel south=new JPanel(new BorderLayout()); south.add(form,BorderLayout.CENTER);south.add(actions,BorderLayout.SOUTH);
        add(south,BorderLayout.SOUTH);
        add(new JScrollPane(table),BorderLayout.CENTER);
        browse.addActionListener(e->chooseFile());
        add.addActionListener(e->save(false));
        update.addActionListener(e->save(true));
        refresh.addActionListener(e->loadMaterials());
        table.getSelectionModel().addListSelectionListener(e->{
            if(!e.getValueIsAdjusting() && table.getSelectedRow()>=0) {
                int row=table.getSelectedRow();
                title.setText(String.valueOf(model.getValueAt(row,2)));
                path.setText(String.valueOf(model.getValueAt(row,3)));
                selectCourseByCode(String.valueOf(model.getValueAt(row,1)));
            }
        });
        loadCourses();
    }

    private void loadCourses() {
        try {
            for(Object[] r:controller.getCourses(user.getUserId()))
                courses.addItem(new CourseChoice((Integer)r[0],String.valueOf(r[1])));
            loadMaterials();
        } catch(SQLException e){error(e);}
    }
    private void chooseFile() {
        JFileChooser chooser=new JFileChooser();
        if(chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
            path.setText(chooser.getSelectedFile().getAbsolutePath());
    }
    private void save(boolean update) {
        CourseChoice c=(CourseChoice)courses.getSelectedItem();
        if(c==null || title.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,"Select a course and enter a material title.");return;
        }
        try {
            if(update) {
                int row=table.getSelectedRow();
                if(row<0){JOptionPane.showMessageDialog(this,"Select a material to update.");return;}
                int id=Integer.parseInt(String.valueOf(model.getValueAt(row,0)));
                controller.updateMaterial(user.getUserId(),id,c.id,title.getText(),path.getText());
            } else controller.addMaterial(user.getUserId(),c.id,title.getText(),path.getText());
            JOptionPane.showMessageDialog(this,"Material saved.");
            title.setText("");path.setText("");loadMaterials();
        } catch(SQLException e){error(e);}
    }
    private void loadMaterials() {
        try {
            model.setRowCount(0);
            List<Object[]> rows=controller.getMaterials(user.getUserId());
            for(Object[] r:rows)model.addRow(r);
        } catch(SQLException e){error(e);}
    }
    private void selectCourseByCode(String code) {
        for(int i=0;i<courses.getItemCount();i++)
            if(courses.getItemAt(i).code.equals(code)){courses.setSelectedIndex(i);break;}
    }
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);}
    private static class CourseChoice {
        int id;String code;CourseChoice(int id,String code){this.id=id;this.code=code;}
        public String toString(){return code;}
    }
}
