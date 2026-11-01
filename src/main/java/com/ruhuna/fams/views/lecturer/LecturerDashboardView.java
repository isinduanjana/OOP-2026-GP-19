package com.ruhuna.fams.views.lecturer;

import com.ruhuna.fams.models.User;
import com.ruhuna.fams.views.LoginView;
import javax.swing.*;
import java.awt.*;

public class LecturerDashboardView extends JFrame {
    private final User user;

    public LecturerDashboardView(User user) {
        this.user=user;
        setTitle("Lecturer Dashboard - Faculty Academic Management System");
        setSize(1100,700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header=new JPanel(new BorderLayout());
        header.setBackground(new Color(41,128,185));
        header.setBorder(BorderFactory.createEmptyBorder(12,15,12,15));
        JLabel welcome=new JLabel("Faculty Academic System | Welcome, "+user.getFullName());
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("SansSerif",Font.BOLD,16));
        JButton logout=new JButton("Logout");
        header.add(welcome,BorderLayout.WEST);
        header.add(logout,BorderLayout.EAST);
        add(header,BorderLayout.NORTH);

        CardLayout cardLayout=new CardLayout();
        JPanel content=new JPanel(cardLayout);
        content.add(new LecturerProfileView(user),"Profile");
        content.add(new LecturerMaterialsView(user),"Materials");
        content.add(new LecturerMarksView(user),"Marks");
        content.add(new LecturerTableView(user,"Undergraduate Details"),"Students");
        content.add(new LecturerTableView(user,"Eligibility Check"),"Eligibility");
        content.add(new LecturerTableView(user,"Marks, Grades and GPA"),"Results");
        content.add(new LecturerTableView(user,"Attendance Records"),"Attendance");
        content.add(new LecturerTableView(user,"Medical Records"),"Medicals");
        content.add(new LecturerTableView(user,"Notices"),"Notices");
        add(content,BorderLayout.CENTER);

        JPanel menu=new JPanel(new GridLayout(0,1,5,5));
        menu.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        menu.setPreferredSize(new Dimension(205,0));
        addMenu(menu,content,cardLayout,"My Profile","Profile");
        addMenu(menu,content,cardLayout,"Course Materials","Materials");
        addMenu(menu,content,cardLayout,"Upload / View Marks","Marks");
        addMenu(menu,content,cardLayout,"Undergraduate Details","Students");
        addMenu(menu,content,cardLayout,"Eligibility Check","Eligibility");
        addMenu(menu,content,cardLayout,"Marks, Grades and GPA","Results");
        addMenu(menu,content,cardLayout,"Attendance Records","Attendance");
        addMenu(menu,content,cardLayout,"Medical Records","Medicals");
        addMenu(menu,content,cardLayout,"Notices","Notices");
        add(menu,BorderLayout.WEST);

        logout.addActionListener(e->{new LoginView().setVisible(true);dispose();});
        cardLayout.show(content,"Profile");
    }

    private void addMenu(JPanel menu,JPanel content,CardLayout layout,String label,String card) {
        JButton button=new JButton(label);
        button.setFocusPainted(false);
        button.addActionListener(e->layout.show(content,card));
        menu.add(button);
    }
}
