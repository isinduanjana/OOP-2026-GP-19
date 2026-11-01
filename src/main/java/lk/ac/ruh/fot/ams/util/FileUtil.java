package lk.ac.ruh.fot.ams.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import lk.ac.ruh.fot.ams.exception.AppException;

/**
 * [COMMON PART] Copies uploaded files (profile pictures, course materials,
 * medical documents) into the project's "uploads" folder and opens them.
 */
public final class FileUtil {

    public static final String PROFILE_PICTURES = "profile_pictures";
    public static final String MATERIALS = "materials";
    public static final String MEDICALS = "medicals";

    private static final Path UPLOADS = Paths.get("uploads");

    private FileUtil() {
    }

    /**
     * Copies a file into uploads/&lt;folder&gt; with a unique name.
     * @return relative path stored in the database, e.g. uploads/materials/1712_notes.pdf
     */
    public static String copyToUploads(File source, String folder) throws AppException {
        if (source == null || !source.isFile()) {
            throw new AppException("Please choose a valid file.");
        }
        try {
            Path dir = UPLOADS.resolve(folder);
            Files.createDirectories(dir);
            String safeName = source.getName().replaceAll("[^A-Za-z0-9._-]", "_");
            Path target = dir.resolve(System.currentTimeMillis() + "_" + safeName);
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString().replace('\\', '/');
        } catch (IOException e) {
            throw new AppException("Could not copy the file: " + e.getMessage(), e);
        }
    }

    public static boolean exists(String path) {
        return path != null && !path.isBlank() && Files.exists(Paths.get(path));
    }

    /** Opens a file with the operating system's default program. */
    public static void open(String path) throws AppException {
        if (!exists(path)) {
            throw new AppException("File not found: " + path
                    + "\n(Sample data only stores the file name - upload a real file to open it.)");
        }
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new AppException("Opening files is not supported on this computer.");
            }
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            throw new AppException("Could not open the file: " + e.getMessage(), e);
        }
    }

    public static boolean isImage(File f) {
        String n = f.getName().toLowerCase();
        return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif");
    }
}
