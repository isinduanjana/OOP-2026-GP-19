package lk.ac.ruh.fot.ams.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * [COMMON PART] Converts one row of a ResultSet into an object.
 * Functional interface -> can be written as a lambda: rs -> new Course(...)
 */
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
