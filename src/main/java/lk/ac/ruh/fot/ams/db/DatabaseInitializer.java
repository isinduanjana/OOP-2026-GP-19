package lk.ac.ruh.fot.ams.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.DatabaseException;

/**
 * [COMMON PART] Creates the database + sample data automatically the first
 * time the program runs (so the project "just works" on a new PC).
 *
 * Scripts: database/01_schema.sql and database/02_seed_data.sql
 * (looked up in the project folder first, then inside the jar).
 */
public class DatabaseInitializer {

    private static final String[] SCRIPTS = {"01_schema.sql", "02_seed_data.sql"};

    /** @return true when the database already exists and has the users table. */
    public boolean isDatabaseReady() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = 'users'";
        try (Connection c = DBConnection.getInstance().openServerConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, AppConfig.getInstance().getDbName());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check the database.", e);
        }
    }

    /** Columns this version of the program needs (added on top of the ER diagram). */
    private static final String[][] REQUIRED_COLUMNS = {
            {"undergraduates", "level"}, {"courses", "level"}, {"courses", "semester"},
            {"attendance_sessions", "hours"}, {"grades", "academic_year"}, {"grades", "remarks"},
            {"departments", "department_code"}, {"medicals", "created_at"}};

    /**
     * @return true when the existing database has every table/column this program uses.
     * An older database (e.g. made by hand from the ER diagram) returns false.
     */
    public boolean isSchemaCompatible() throws DatabaseException {
        try (Connection c = DBConnection.getInstance().openServerConnection()) {
            for (String[] col : REQUIRED_COLUMNS) {
                if (!hasColumn(c, col[0], col[1])) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Could not check the database structure.", e);
        }
    }

    /**
     * Columns added in later versions of the project: {table, column, definition}.
     * They are added automatically to an existing database (no data is lost),
     * so group members do not have to reset their database after an update.
     */
    private static final String[][] UPGRADE_COLUMNS = {
            {"course_materials", "covers_from", "DATE NULL AFTER file_path"},
            {"course_materials", "covers_to", "DATE NULL AFTER covers_from"}};

    /** Adds any missing UPGRADE_COLUMNS with ALTER TABLE (safe to call on every start). */
    public void upgradeSchema() throws DatabaseException {
        String db = AppConfig.getInstance().getDbName();
        try (Connection c = DBConnection.getInstance().openServerConnection();
             Statement st = c.createStatement()) {
            for (String[] col : UPGRADE_COLUMNS) {
                if (!hasColumn(c, col[0], col[1])) {
                    // names come from the constant above (never from user input), so concatenation is safe here
                    st.execute("ALTER TABLE `" + db + "`." + col[0] + " ADD COLUMN " + col[1] + " " + col[2]);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not upgrade the database structure.", e);
        }
    }

    private static boolean hasColumn(Connection c, String table, String column) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_schema = ? AND table_name = ? AND column_name = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, AppConfig.getInstance().getDbName());
            ps.setString(2, table);
            ps.setString(3, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Drops and recreates the database from the SQL scripts. */
    public void createDatabase() throws DatabaseException {
        DBConnection.getInstance().reset();
        try (Connection c = DBConnection.getInstance().openServerConnection();
             Statement st = c.createStatement()) {
            for (String script : SCRIPTS) {
                for (String statement : readStatements(script)) {
                    st.execute(statement);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed while running the database scripts.", e);
        } catch (IOException e) {
            throw new DatabaseException("Could not read the database scripts: " + e.getMessage(), e);
        }
    }

    /** Ensures the DB exists (creates it if allowed by db.autoInit) and is up to date. */
    public void ensureDatabase() throws DatabaseException {
        if (!isDatabaseReady()) {
            if (AppConfig.getInstance().isAutoInit()) {
                createDatabase();
            }
            return;
        }
        upgradeSchema();      // existing database: add columns from newer versions
    }

    /** Reads a script and splits it into single SQL statements. */
    private List<String> readStatements(String script) throws IOException {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(open(script), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                current.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    String stmt = current.toString().trim();
                    statements.add(stmt.substring(0, stmt.length() - 1));
                    current.setLength(0);
                }
            }
        }
        if (current.toString().trim().length() > 0) {
            statements.add(current.toString().trim());
        }
        return statements;
    }

    private InputStream open(String script) throws IOException {
        Path local = Paths.get("database", script);
        if (Files.exists(local)) {
            return Files.newInputStream(local);
        }
        InputStream in = getClass().getResourceAsStream("/database/" + script);
        if (in == null) {
            throw new IOException("Script not found: database/" + script);
        }
        return in;
    }
}
