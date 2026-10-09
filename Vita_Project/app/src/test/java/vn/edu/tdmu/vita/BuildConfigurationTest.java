package vn.edu.tdmu.vita;

import org.junit.Test;

import static org.junit.Assert.*;

public class BuildConfigurationTest {

    @Test
    public void testPackageAndSdkConstants() {
        String expectedPackage = "vn.edu.tdmu.vita";
        int expectedMinSdk = 24;

        assertEquals("vn.edu.tdmu.vita", expectedPackage);
        assertTrue(expectedMinSdk >= 24);
    }
}
