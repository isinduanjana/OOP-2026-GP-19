package lk.ac.ruh.fot.ams.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lk.ac.ruh.fot.ams.db.DBConnection;
import lk.ac.ruh.fot.ams.exception.DatabaseException;

/**
 * [COMMON PART] Abstract parent of EVERY DAO class in the project
 * (common, M1, M2, M3 and M4 DAOs all "extends BaseDAO").
 *
 * It hides all JDBC boiler-plate (PreparedStatement, binding parameters,
 * closing resources, converting SQLException) so a DAO method is one line:
 *
 *     return queryList("SELECT ... WHERE course_id=?", this::mapCourse, courseId);
 *
 * OOP: Abstraction + Inheritance (protected helper methods are inherited),
 *      Encapsulation (bind() is private), Exception handling.
 */
public abstract class BaseDAO {

    /** Work executed inside a database transaction. */
    @FunctionalInterface
    protected interface TransactionWork {
        void run(Connection connection) throws SQLException, DatabaseException;
    }

    protected Connection getConnection() throws DatabaseException {
        return DBConnection.getInstance().getConnection();
    }

    /** SELECT returning many rows. */
    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) throws DatabaseException {
        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not load data from the database.", e);
        }
    }

    /** SELECT returning one row, or null when nothing matches. */
    protected <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) throws DatabaseException {
        List<T> list = queryList(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    /** SELECT returning a single number (COUNT, AVG ...). */
    protected double queryNumber(String sql, Object... params) throws DatabaseException {
        Double d = queryOne(sql, rs -> rs.getDouble(1), params);
        return d == null ? 0 : d;
    }

    /** INSERT / UPDATE / DELETE. Returns affected rows. */
    protected int executeUpdate(String sql, Object... params) throws DatabaseException {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the changes.", e);
        }
    }

    /** INSERT returning the generated AUTO_INCREMENT key. */
    protected int executeInsert(String sql, Object... params) throws DatabaseException {
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the new record.", e);
        }
    }

    /** Runs several statements as one transaction (all succeed or all roll back). */
    protected void inTransaction(TransactionWork work) throws DatabaseException {
        Connection c = getConnection();
        try {
            c.setAutoCommit(false);
            work.run(c);
            c.commit();
        } catch (SQLException e) {
            rollback(c);
            throw new DatabaseException("The operation failed and was cancelled.", e);
        } catch (DatabaseException e) {
            rollback(c);
            throw e;
        } finally {
            try {
                c.setAutoCommit(true);
            } catch (SQLException ignored) {
                // connection will be recreated if broken
            }
        }
    }

    /** Helper for use inside inTransaction: prepared insert returning the key. */
    protected static int insert(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    /** Helper for use inside inTransaction: prepared update. */
    protected static int update(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    private static void rollback(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignored) {
            // nothing more we can do
        }
    }

    /** Binds Java values (incl. java.time and enums) to the ? placeholders. */
    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            int idx = i + 1;
            if (p == null) {
                ps.setNull(idx, Types.NULL);
            } else if (p instanceof LocalDate d) {
                ps.setDate(idx, Date.valueOf(d));
            } else if (p instanceof LocalTime t) {
                ps.setTime(idx, Time.valueOf(t));
            } else if (p instanceof LocalDateTime dt) {
                ps.setTimestamp(idx, Timestamp.valueOf(dt));
            } else if (p instanceof Enum<?> e) {
                ps.setString(idx, e.name());
            } else if (p instanceof Boolean b) {
                ps.setBoolean(idx, b);
            } else {
                ps.setObject(idx, p);
            }
        }
    }

    // ------------------------------------------------------------ null-safe readers
    protected static Integer getInteger(ResultSet rs, String column) throws SQLException {
        int v = rs.getInt(column);
        return rs.wasNull() ? null : v;
    }

    protected static Double getDoubleOrNull(ResultSet rs, String column) throws SQLException {
        double v = rs.getDouble(column);
        return rs.wasNull() ? null : v;
    }

    protected static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column);
        return d == null ? null : d.toLocalDate();
    }

    protected static LocalTime getTime(ResultSet rs, String column) throws SQLException {
        Time t = rs.getTime(column);
        return t == null ? null : t.toLocalTime();
    }

    protected static LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toLocalDateTime();
    }
}
