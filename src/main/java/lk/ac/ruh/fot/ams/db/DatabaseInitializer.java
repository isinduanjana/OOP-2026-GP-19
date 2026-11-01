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

    /** Ensures the DB exists; creates it if allowed by db.autoInit. */
    public void ensureDatabase() throws DatabaseException {
        if (!isDatabaseReady() && AppConfig.getInstance().isAutoInit()) {
            createDatabase();
        }
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
