package vn.edu.tdmu.vita.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityUtils {

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) return null;
        try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return "pbkdf2-sha256$600000$" + derive(password, salt, "PBKDF2WithHmacSHA256", 600000);
        } catch (NoSuchAlgorithmException e) {
            // Android 7 chưa có PBKDF2-HMAC-SHA256 trong API chuẩn.
            return "pbkdf2-sha1$1400000$" + derive(password, salt, "PBKDF2WithHmacSHA1", 1400000);
        }
    }

    public static boolean verifyPassword(String password, String salt, String stored) {
        if (password == null || salt == null || stored == null) return false;
        String computed;
        if (isLegacyHash(stored)) {
            try {
                computed = bytesToHex(MessageDigest.getInstance("SHA-256")
                        .digest((salt + password).getBytes(StandardCharsets.UTF_8)));
            } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        } else if (stored.matches("pbkdf2-sha256\\$600000\\$[0-9a-f]{64}")) {
            computed = "pbkdf2-sha256$600000$" + derive(password, salt, "PBKDF2WithHmacSHA256", 600000);
        } else if (stored.matches("pbkdf2-sha1\\$1400000\\$[0-9a-f]{64}")) {
            computed = "pbkdf2-sha1$1400000$" + derive(password, salt, "PBKDF2WithHmacSHA1", 1400000);
        } else return false;
        return MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8), computed.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isLegacyHash(String hash) {
        return hash != null && hash.matches("[0-9a-f]{64}");
    }

    private static String derive(String password, String salt, String algorithm, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), iterations, 256);
        try {
            return bytesToHex(SecretKeyFactory.getInstance(algorithm).generateSecret(spec).getEncoded());
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("Không thể bảo vệ mật khẩu trên thiết bị này", e);
        } finally { spec.clearPassword(); }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
