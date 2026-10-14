package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class BMICalculatorTest {

    @Test
    public void testCalculate_ValidInputs() {
        // Height: 170cm, Weight: 68kg
        // BMI = 68 / (1.7 * 1.7) = 23.529...
        float bmi = BMICalculator.calculate(68.0f, 170.0f);
        assertEquals(23.53f, bmi, 0.1f);
    }

    @Test
    public void testCalculate_InvalidInputs_ReturnsZero() {
        assertEquals(0.0f, BMICalculator.calculate(0f, 170.0f), 0.001f);
        assertEquals(0.0f, BMICalculator.calculate(-68.0f, 170.0f), 0.001f);
        assertEquals(0.0f, BMICalculator.calculate(68.0f, 0f), 0.001f);
        assertEquals(0.0f, BMICalculator.calculate(68.0f, -170.0f), 0.001f);
    }

    @Test
    public void testClassify_AsianVsWhoComparison() {
        // Height 170cm, Weight 68kg -> BMI ~ 23.53
        float bmi = 23.53f;

        BMICalculator.BmiResult asianResult = BMICalculator.classify(bmi, "ASIAN");
        assertEquals("Thừa cân", asianResult.getCategory());

        BMICalculator.BmiResult whoResult = BMICalculator.classify(bmi, "WHO");
        assertEquals("Bình thường", whoResult.getCategory());
    }

    @Test
    public void testClassify_UnderweightAndObese() {
        BMICalculator.BmiResult underweight = BMICalculator.classify(17.5f, "ASIAN");
        assertEquals("Thiếu cân", underweight.getCategory());

        BMICalculator.BmiResult obese1 = BMICalculator.classify(26.0f, "ASIAN");
        assertEquals("Béo phì độ I", obese1.getCategory());

        BMICalculator.BmiResult obese2 = BMICalculator.classify(31.0f, "ASIAN");
        assertEquals("Béo phì độ II", obese2.getCategory());
    }

    @Test
    public void testCalculateAndClassify() {
        BMICalculator.BmiResult result = BMICalculator.calculateAndClassify(65.0f, 170.0f, "ASIAN");
        assertEquals(22.5f, result.getBmi(), 0.1f);
        assertEquals("Bình thường", result.getCategory());
        assertTrue(result.getMinHealthyWeight() > 50f);
        assertTrue(result.getMaxHealthyWeight() < 70f);
    }
}
