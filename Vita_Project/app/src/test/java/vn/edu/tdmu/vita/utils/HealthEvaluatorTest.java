package vn.edu.tdmu.vita.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for HealthEvaluator — blood pressure classification
 * following Vietnamese Heart Association (Hội Tim mạch học Việt Nam) standards.
 */
public class HealthEvaluatorTest {

    // ---- Blood Pressure Classification Tests ----

    @Test
    public void testOptimalBP() {
        // < 120/80
        HealthEvaluator.BpResult result = HealthEvaluator.classifyBloodPressure(110, 70);
        assertEquals("Bình thường tối ưu", result.label);
        assertEquals(HealthEvaluator.STATUS_GOOD, result.severity);
    }

    @Test
    public void testNormalBP() {
        // 120-129 / 80-84
        HealthEvaluator.BpResult result = HealthEvaluator.classifyBloodPressure(125, 82);
        assertEquals("Bình thường", result.label);
        assertEquals(HealthEvaluator.STATUS_GOOD, result.severity);
    }

    @Test
    public void testHighNormalBP() {
        // 130-139 / 85-89
        HealthEvaluator.BpResult result = HealthEvaluator.classifyBloodPressure(135, 87);
        assertEquals("Tiền tăng huyết áp", result.label);
        assertEquals(HealthEvaluator.STATUS_WARNING, result.severity);
    }

    @Test
    public void testHypertensionGrade1() {
        // 140-159 / 90-99
        HealthEvaluator.BpResult result = HealthEvaluator.classifyBloodPressure(145, 92);
        assertEquals("Tăng huyết áp độ 1", result.label);
        assertEquals(HealthEvaluator.STATUS_DANGER, result.severity);
    }

    @Test
    public void testHypertensionGrade2() {
        // >= 160 / >= 100
        HealthEvaluator.BpResult result = HealthEvaluator.classifyBloodPressure(170, 105);
        assertEquals("Tăng huyết áp độ 2", result.label);
        assertEquals(HealthEvaluator.STATUS_DANGER, result.severity);
    }

    @Test
    public void testBoundaryOptimalToNormal() {
        // Exactly 120/80 → Normal (not optimal)
        HealthEvaluator.BpResult result = HealthEvaluator.classifyBloodPressure(120, 80);
        assertEquals("Bình thường", result.label);
    }

    @Test
    public void testSystolicHigherCategory() {
        // Systolic 142 (Grade 1), Diastolic 78 (Optimal) → use higher: Grade 1
        HealthEvaluator.BpResult result = HealthEvaluator.classifyBloodPressure(142, 78);
        assertEquals("Tăng huyết áp độ 1", result.label);
    }

    @Test
    public void testDiastolicHigherCategory() {
        // Systolic 118 (Optimal), Diastolic 92 (Grade 1) → use higher: Grade 1
        HealthEvaluator.BpResult result = HealthEvaluator.classifyBloodPressure(118, 92);
        assertEquals("Tăng huyết áp độ 1", result.label);
    }

    // ---- Heart Rate Classification Tests ----

    @Test
    public void testNormalHeartRate() {
        assertEquals("Bình thường", HealthEvaluator.classifyHeartRate(72));
    }

    @Test
    public void testLowHeartRate() {
        assertEquals("Nhịp chậm", HealthEvaluator.classifyHeartRate(55));
    }

    @Test
    public void testHighHeartRate() {
        assertEquals("Nhịp nhanh", HealthEvaluator.classifyHeartRate(105));
    }

    @Test
    public void testBoundaryHeartRate60() {
        assertEquals("Bình thường", HealthEvaluator.classifyHeartRate(60));
    }

    @Test
    public void testBoundaryHeartRate100() {
        assertEquals("Bình thường", HealthEvaluator.classifyHeartRate(100));
    }
}
