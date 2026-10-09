package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import java.util.Calendar;
import static org.junit.Assert.*;

public class InputValidatorTest {

    @Test
    public void testIsValidUsername() {
        assertFalse(InputValidator.isValidUsername(null));
        assertFalse(InputValidator.isValidUsername(""));
        assertFalse(InputValidator.isValidUsername("ab")); // < 3 chars
        assertFalse(InputValidator.isValidUsername("user name")); // Contains space
        assertTrue(InputValidator.isValidUsername("trungtin19"));
        assertTrue(InputValidator.isValidUsername("user_123"));
    }

    @Test
    public void testIsValidPassword() {
        assertFalse(InputValidator.isValidPassword(null));
        assertFalse(InputValidator.isValidPassword(""));
        assertFalse(InputValidator.isValidPassword("12345")); // < 6 chars
        assertTrue(InputValidator.isValidPassword("123456"));
        assertTrue(InputValidator.isValidPassword("StrongPass@123"));
    }

    @Test
    public void testIsValidHeight() {
        assertFalse(InputValidator.isValidHeight(0));
        assertFalse(InputValidator.isValidHeight(49.9f));
        assertFalse(InputValidator.isValidHeight(250.1f));
        assertTrue(InputValidator.isValidHeight(50.0f));
        assertTrue(InputValidator.isValidHeight(170.5f));
        assertTrue(InputValidator.isValidHeight(250.0f));
    }

    @Test
    public void testIsValidWeight() {
        assertFalse(InputValidator.isValidWeight(0));
        assertFalse(InputValidator.isValidWeight(19.9f));
        assertFalse(InputValidator.isValidWeight(300.1f));
        assertTrue(InputValidator.isValidWeight(20.0f));
        assertTrue(InputValidator.isValidWeight(65.5f));
        assertTrue(InputValidator.isValidWeight(300.0f));
    }

    @Test
    public void testIsValidBirthYear() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        assertFalse(InputValidator.isValidBirthYear(1899));
        assertFalse(InputValidator.isValidBirthYear(currentYear + 1));
        assertTrue(InputValidator.isValidBirthYear(1900));
        assertTrue(InputValidator.isValidBirthYear(2000));
        assertTrue(InputValidator.isValidBirthYear(currentYear));
    }

    @Test
    public void testIsValidWaterGoal() {
        assertFalse(InputValidator.isValidWaterGoal(99));
        assertFalse(InputValidator.isValidWaterGoal(10001));
        assertTrue(InputValidator.isValidWaterGoal(100));
        assertTrue(InputValidator.isValidWaterGoal(2000));
        assertTrue(InputValidator.isValidWaterGoal(10000));
    }
}
