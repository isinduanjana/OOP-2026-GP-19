package lk.ac.ruh.fot.ams.dao;

import java.util.List;

import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Department;

/** [COMMON PART] Departments of the faculty. */
public class DepartmentDAO extends BaseDAO {

    public List<Department> findAll() throws DatabaseException {
        return queryList("SELECT department_id, department_code, department_name FROM departments ORDER BY department_id",
                rs -> new Department(rs.getInt(1), rs.getString(2), rs.getString(3)));
    }
}
