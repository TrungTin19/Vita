package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import java.util.Calendar;
import static org.junit.Assert.*;

public class InputValidatorTest {

    @Test
    public void testIsValidUsername() {
        assertFalse(InputValidator.isValidUsername(null));
        assertFalse(InputValidator.isValidUsername(""));
        assertFalse(InputValidator.isValidUsername("ab")); // Ít hơn 3 ký tự
        assertFalse(InputValidator.isValidUsername("user name")); // Có chứa khoảng trắng
        assertTrue(InputValidator.isValidUsername("trungtin19"));
        assertTrue(InputValidator.isValidUsername("user_123"));
    }

    @Test
    public void testIsValidPassword() {
        assertFalse(InputValidator.isValidPassword(null));
        assertFalse(InputValidator.isValidPassword(""));
        assertFalse(InputValidator.isValidPassword("12345")); // Ít hơn 6 ký tự
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

    @Test
    public void testIsValidBloodPressure() {
        assertFalse(InputValidator.isValidSystolic(49));
        assertFalse(InputValidator.isValidSystolic(251));
        assertTrue(InputValidator.isValidSystolic(120));

        assertFalse(InputValidator.isValidDiastolic(29));
        assertFalse(InputValidator.isValidDiastolic(151));
        assertTrue(InputValidator.isValidDiastolic(80));

        // Huyết áp tâm thu phải lớn hơn huyết áp tâm trương
        assertTrue(InputValidator.isValidBloodPressure(120, 80));
        assertFalse(InputValidator.isValidBloodPressure(80, 120));
        assertFalse(InputValidator.isValidBloodPressure(100, 100));
    }

    @Test
    public void testIsValidHeartRate() {
        assertFalse(InputValidator.isValidHeartRate(29));
        assertFalse(InputValidator.isValidHeartRate(251));
        assertTrue(InputValidator.isValidHeartRate(75));
    }

    @Test
    public void testIsValidBloodSugar() {
        assertFalse(InputValidator.isValidBloodSugar(19.9f));
        assertFalse(InputValidator.isValidBloodSugar(600.1f));
        assertTrue(InputValidator.isValidBloodSugar(95.0f));
    }
}
