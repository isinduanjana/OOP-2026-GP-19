package com.ruhuna.fams.models;

public class TechnicalOfficer {
    private int id;
    private String empId;
    private String name;
    private String email;
    private String assignedLab;


    public TechnicalOfficer() {}

    public TechnicalOfficer(String empId, String name, String email, String assignedLab) {
        this.empId = empId;
        this.name = name;
        this.email = email;
        this.assignedLab = assignedLab;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAssignedLab() { return assignedLab; }
    public void setAssignedLab(String assignedLab) { this.assignedLab = assignedLab; }
}
