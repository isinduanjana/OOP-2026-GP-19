package lk.ac.ruh.fot.ams.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * [COMMON PART] Reads / saves the database settings from "db.properties"
 * (in the folder the program is started from).
 *
 * OOP: Encapsulation - the Properties object is private; other classes only
 * use the typed getters. Singleton - one shared configuration object.
 */
public final class AppConfig {

    public static final String APP_NAME = "FOT Academic Management System";
    public static final String UNIVERSITY = "University of Ruhuna";
    public static final String FACULTY = "Faculty of Technology";
    /** Current academic year / semester used for enrollments and results. */
    public static final String CURRENT_ACADEMIC_YEAR = "2025/2026";
    public static final int CURRENT_LEVEL = 2;
    public static final int CURRENT_SEMESTER = 1;
    /** Eligibility rules from the ICT2132 handout. */
    public static final double MIN_ATTENDANCE_PERCENT = 80.0;
    public static final double MIN_CA_PERCENT = 40.0;
    public static final int SESSIONS_PER_COMPONENT = 15;
    public static final int HOURS_PER_SESSION = 2;

    private static final Path FILE = Paths.get("db.properties");
    private static AppConfig instance;

    private final Properties props = new Properties();

    private AppConfig() {
        // defaults (used if db.properties is missing)
        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "3306");
        props.setProperty("db.name", "faculty_academic_system");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", "");
        props.setProperty("db.autoInit", "true");
        load();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void load() {
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Could not read db.properties, using defaults: " + e.getMessage());
            }
        }
    }

    /** Saves the current settings back to db.properties. */
    public void save() throws IOException {
        try (OutputStream out = Files.newOutputStream(FILE)) {
            props.store(out, "FOT Academic Management System - database settings");
        }
    }

    public String getHost()      { return props.getProperty("db.host").trim(); }
    public String getPort()      { return props.getProperty("db.port").trim(); }
    public String getDbName()    { return props.getProperty("db.name").trim(); }
    public String getUser()      { return props.getProperty("db.user").trim(); }
    public String getPassword()  { return props.getProperty("db.password", ""); }
    public boolean isAutoInit()  { return Boolean.parseBoolean(props.getProperty("db.autoInit", "true").trim()); }

    public void setHost(String v)     { props.setProperty("db.host", v); }
    public void setPort(String v)     { props.setProperty("db.port", v); }
    public void setUser(String v)     { props.setProperty("db.user", v); }
    public void setPassword(String v) { props.setProperty("db.password", v == null ? "" : v); }

    private static final String PARAMS =
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo&characterEncoding=UTF-8";

    /** JDBC URL pointing at the server only (used to create the database). */
    public String getServerUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + PARAMS;
    }

    /** JDBC URL pointing at our database. */
    public String getDatabaseUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDbName() + PARAMS;
    }
}
