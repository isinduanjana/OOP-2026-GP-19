package lk.ac.ruh.fot.ams.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * [COMMON PART] Passwords are never stored as plain text; we store the
 * SHA-256 hash. (Utility class: private constructor, static methods.)
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in Java
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String plain, String hash) {
        return hash != null && hash.equalsIgnoreCase(hash(plain));
    }
}
