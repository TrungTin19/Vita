package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class SecurityUtilsTest {

    @Test
    public void testSaltGeneration() {
        String salt1 = SecurityUtils.generateSalt();
        String salt2 = SecurityUtils.generateSalt();

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertNotEquals(salt1, salt2);
        assertEquals(32, salt1.length()); // 16 bytes hex = 32 chars
    }

    @Test
    public void testPasswordHashingDeterministic() {
        String password = "password123";
        String salt = "abcdef0123456789abcdef0123456789";

        String hash1 = SecurityUtils.hashPassword(password, salt);
        String hash2 = SecurityUtils.hashPassword(password, salt);

        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length()); // SHA-256 = 64 hex chars
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
