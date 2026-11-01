package lk.ac.ruh.fot.ams.model;

/** [COMMON PART] A department of the faculty (ICT, ET, BST ...). */
public class Department {

    private int departmentId;
    private String code;
    private String name;

    public Department() {
    }

    public Department(int departmentId, String code, String name) {
        this.departmentId = departmentId;
        this.code = code;
        this.name = name;
    }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Shown in combo boxes. */
    @Override
    public String toString() {
        return code + " - " + name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Department d && d.departmentId == departmentId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(departmentId);
    }
}
