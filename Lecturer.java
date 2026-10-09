package com.ruhuna.fams.models;

public class Lecturer {
    private int lecturerId;
    private int userId;
    private String employeeNo;
    private String name;
    private String username;
    private String email;
    private String phone;
    private String address;

    public Lecturer(int lecturerId, int userId, String employeeNo, String name, String username, String email, String phone, String address) {
        this.lecturerId = lecturerId;
        this.userId = userId;
        this.employeeNo = employeeNo;
        this.name = name;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public int getLecturerId() { return lecturerId; }
    public int getUserId() { return userId; }
    public String getEmployeeNo() { return employeeNo; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
}