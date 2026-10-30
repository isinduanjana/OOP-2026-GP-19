package com.ruhuna.fams.views.lecturer;

import com.ruhuna.fams.controllers.LecturerController;
import com.ruhuna.fams.models.User;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LecturerProfileView extends JPanel {
    private final User user;
    private final LecturerController controller=new LecturerController();
    private final JTextField name=new JTextField(25), email=new JTextField(25), phone=new JTextField(25);
    private final JTextArea address=new JTextArea(4,25);
    private final JLabel username=new JLabel(), employeeNo=new JLabel();

    public LecturerProfileView(User user) {
        this.user=user;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(25,35,25,35));
        JLabel title=new JLabel("My Profile");
        title.setFont(new Font("SansSerif",Font.BOLD,20));
        add(title,BorderLayout.NORTH);
        JPanel form=new JPanel(new GridLayout(6,2,10,10));
        form.add(new JLabel("Username (cannot be changed):")); form.add(username);
        form.add(new JLabel("Employee Number:")); form.add(employeeNo);
        form.add(new JLabel("Full Name:")); form.add(name);
        form.add(new JLabel("Email:")); form.add(email);
        form.add(new JLabel("Phone:")); form.add(phone);
        form.add(new JLabel("Address:")); form.add(new JScrollPane(address));
        add(form,BorderLayout.CENTER);
        JButton save=new JButton("Update Profile");
        add(save,BorderLayout.SOUTH);
        save.addActionListener(e->saveProfile());
        loadProfile();
    }

    private void loadProfile() {
        try {
            java.util.List<Object[]> rows=controller.getProfile(user.getUserId());
            if(rows.isEmpty()) throw new SQLException("Profile not found.");
            Object[] r=rows.get(0);
            username.setText(String.valueOf(r[0])); name.setText(String.valueOf(r[1]));
            email.setText(r[2]==null?"":String.valueOf(r[2]));
            phone.setText(r[3]==null?"":String.valueOf(r[3]));
            address.setText(r[4]==null?"":String.valueOf(r[4]));
            employeeNo.setText(r[5]==null?"":String.valueOf(r[5]));
        } catch(SQLException e){error(e);}
    }

    private void saveProfile() {
        if(name.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Full name is required.");return;}
        try {
            controller.updateProfile(user.getUserId(),name.getText(),email.getText(),phone.getText(),address.getText());
            JOptionPane.showMessageDialog(this,"Profile updated. Username and password were not changed.");
        } catch(SQLException e){error(e);}
    }
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);}
}
