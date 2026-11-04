package lk.ac.ruh.fot.ams.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.DatabaseException;


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


    public Connection openServerConnection() throws DatabaseException {
        AppConfig cfg = AppConfig.getInstance();
        try {
            return DriverManager.getConnection(cfg.getServerUrl(), cfg.getUser(), cfg.getPassword());
        } catch (SQLException e) {
            throw new DatabaseException("Cannot connect to MySQL server at "
                    + cfg.getHost() + ":" + cfg.getPort() + " as '" + cfg.getUser() + "'.", e);
        }
    }


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
