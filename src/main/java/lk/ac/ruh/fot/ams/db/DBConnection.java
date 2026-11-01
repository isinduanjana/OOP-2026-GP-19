package lk.ac.ruh.fot.ams.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.DatabaseException;

/**
 * [COMMON PART] Singleton that owns the one JDBC connection used by the app.
 *
 * Every DAO (in common, M1, M2, M3, M4) gets its connection from here:
 *     Connection c = DBConnection.getInstance().getConnection();
 *
 * OOP: Encapsulation (private constructor + private connection field),
 *      Exception handling (SQLException -> DatabaseException).
 */
public final class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        // private: use getInstance()
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /** Returns an open connection, (re)connecting when needed. */
    public synchronized Connection getConnection() throws DatabaseException {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(3)) {
                AppConfig cfg = AppConfig.getInstance();
                connection = DriverManager.getConnection(cfg.getDatabaseUrl(), cfg.getUser(), cfg.getPassword());
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException("Cannot connect to the MySQL database.", e);
        }
    }

    /** Opens a connection to the MySQL server without selecting a database. */
    public Connection openServerConnection() throws DatabaseException {
        AppConfig cfg = AppConfig.getInstance();
        try {
            return DriverManager.getConnection(cfg.getServerUrl(), cfg.getUser(), cfg.getPassword());
        } catch (SQLException e) {
            throw new DatabaseException("Cannot connect to MySQL server at "
                    + cfg.getHost() + ":" + cfg.getPort() + " as '" + cfg.getUser() + "'.", e);
        }
    }

    /** Called after settings change so the next getConnection() reconnects. */
    public synchronized void reset() {
        close();
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // closing anyway
            }
            connection = null;
        }
    }
}
