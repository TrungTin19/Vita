package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class SecurityUtilsTest {
    @Test public void verifiesAndroid7FormatWithUnicodePassword() {
        String stored = "pbkdf2-sha1$1400000$329037db9c40ae48c4e64ef4718e48761615b372b32afe26576757b8a98c6aff";
        assertTrue(SecurityUtils.verifyPassword("Mật khẩu thử nghiệm", "0123456789abcdef", stored));
        assertFalse(SecurityUtils.verifyPassword("wrong", "0123456789abcdef", stored));
    }
    @Test public void verifiesLegacyAndVersionedHashesAndRejectsMalformedInput() throws Exception {
        String salt = "0123456789abcdef";
        String password = "Mật khẩu thử nghiệm";
        byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest((salt + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder legacy = new StringBuilder();
        for (byte value : digest) legacy.append(String.format("%02x", value));
        assertTrue(SecurityUtils.verifyPassword(password, salt, legacy.toString()));
        assertFalse(SecurityUtils.verifyPassword("wrong", salt, legacy.toString()));
        assertTrue(SecurityUtils.isLegacyHash(legacy.toString()));
        String modern = SecurityUtils.hashPassword(password, salt);
        assertTrue(SecurityUtils.verifyPassword(password, salt, modern));
        assertFalse(SecurityUtils.verifyPassword("wrong", salt, modern));
        assertFalse(SecurityUtils.isLegacyHash(modern));
        assertFalse(SecurityUtils.verifyPassword(password, salt, "pbkdf2-sha256$2147483647$bad"));
        assertFalse(SecurityUtils.verifyPassword(null, salt, modern));
    }

    @Test
    public void testSaltGeneration() {
        String salt1 = SecurityUtils.generateSalt();
        String salt2 = SecurityUtils.generateSalt();

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertNotEquals(salt1, salt2);
        assertEquals(32, salt1.length()); // 16 byte biểu diễn dạng hex = 32 ký tự
    }

    @Test
    public void testPasswordHashingDeterministic() {
        String password = "password123";
        String salt = "abcdef0123456789abcdef0123456789";

        String hash1 = SecurityUtils.hashPassword(password, salt);
        String hash2 = SecurityUtils.hashPassword(password, salt);

        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertTrue(hash1.startsWith("pbkdf2-sha256$600000$"));
    }

    @Test
    public void testDifferentSaltsProduceDifferentHashes() {
        String password = "securePassword!@#";
        String salt1 = SecurityUtils.generateSalt();
        String salt2 = SecurityUtils.generateSalt();

        String hash1 = SecurityUtils.hashPassword(password, salt1);
        String hash2 = SecurityUtils.hashPassword(password, salt2);

        assertNotEquals(hash1, hash2);
    }
}
