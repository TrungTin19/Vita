package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class BmiUtilsTest {

    @Test
    public void testCalculateBmi_ValidInputs() {
        // Height: 170cm = 1.7m, Weight: 65kg
        // BMI = 65 / (1.7 * 1.7) = 22.4913...
        double bmi = BmiUtils.calculateBmi(170.0, 65.0);
        assertEquals(22.5, bmi, 0.1);
    }

    @Test
    public void testCalculateBmi_InvalidInputs_ReturnsZero() {
        assertEquals(0.0, BmiUtils.calculateBmi(0, 65.0), 0.001);
        assertEquals(0.0, BmiUtils.calculateBmi(-170, 65.0), 0.001);
        assertEquals(0.0, BmiUtils.calculateBmi(170, 0), 0.001);
        assertEquals(0.0, BmiUtils.calculateBmi(170, -65), 0.001);
    }

    @Test
    public void testClassifyBmi_AsianStandard() {
        assertEquals("Thiếu cân", BmiUtils.classifyBmi(18.0, "ASIAN"));
        assertEquals("Bình thường", BmiUtils.classifyBmi(18.5, "ASIAN"));
        assertEquals("Bình thường", BmiUtils.classifyBmi(22.5, "ASIAN"));
        assertEquals("Thừa cân", BmiUtils.classifyBmi(23.0, "ASIAN"));
        assertEquals("Thừa cân", BmiUtils.classifyBmi(24.5, "ASIAN"));
        assertEquals("Béo phì độ I", BmiUtils.classifyBmi(26.0, "ASIAN"));
        assertEquals("Béo phì độ II", BmiUtils.classifyBmi(31.0, "ASIAN"));
    }

    @Test
    public void testClassifyBmi_WhoStandard() {
        assertEquals("Thiếu cân", BmiUtils.classifyBmi(18.0, "WHO"));
        assertEquals("Bình thường", BmiUtils.classifyBmi(22.0, "WHO"));
        assertEquals("Bình thường", BmiUtils.classifyBmi(24.9, "WHO"));
        assertEquals("Thừa cân", BmiUtils.classifyBmi(25.5, "WHO"));
        assertEquals("Thừa cân", BmiUtils.classifyBmi(29.5, "WHO"));
        assertEquals("Béo phì độ I", BmiUtils.classifyBmi(32.0, "WHO"));
        assertEquals("Béo phì độ II", BmiUtils.classifyBmi(36.0, "WHO"));
    }

    @Test
    public void testGetHealthyWeightRange() {
        // Height: 170cm = 1.7m. Asian min BMI=18.5 -> 18.5 * 2.89 = 53.5kg. Max BMI=22.9 -> 22.9 * 2.89 = 66.2kg
        double[] rangeAsian = BmiUtils.getHealthyWeightRange(170.0, "ASIAN");
        assertEquals(53.5, rangeAsian[0], 0.2);
        assertEquals(66.2, rangeAsian[1], 0.2);

        // WHO min BMI=18.5 -> 53.5kg. Max BMI=24.9 -> 24.9 * 2.89 = 72.0kg
        double[] rangeWho = BmiUtils.getHealthyWeightRange(170.0, "WHO");
        assertEquals(53.5, rangeWho[0], 0.2);
        assertEquals(72.0, rangeWho[1], 0.2);
    }
}
