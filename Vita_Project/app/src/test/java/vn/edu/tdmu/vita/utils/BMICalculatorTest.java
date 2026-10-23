package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class BMICalculatorTest {

    @Test
    public void whoObesity_preservesClassTwoBelowFortyAndStartsClassThreeAtForty() {
        assertEquals("Béo phì độ II", BMICalculator.classify(35f, "WHO").getCategory());
        assertEquals("Béo phì độ II", BMICalculator.classify(39.99f, "WHO").getCategory());
        assertEquals("Béo phì độ III", BMICalculator.classify(40f, "WHO").getCategory());
        assertEquals("Béo phì độ III", BMICalculator.classify(45f, "WHO").getCategory());
        assertEquals("Béo phì độ II", BMICalculator.classify(40f, "ASIAN").getCategory());
    }

    @Test
    public void ageAwareBmi_underTwentyHasOnlyNumericValue() {
        assertNumericOnly(BMICalculator.calculateAndClassify(68f, 170f, "WHO", 2007, 2026));
        assertNumericOnly(BMICalculator.calculateAndClassify(68f, 170f, "ASIAN", 2016, 2026));
    }

    @Test
    public void ageAwareBmi_unknownOrInvalidBirthYearHasOnlyNumericValue() {
        assertNumericOnly(BMICalculator.calculateAndClassify(68f, 170f, "WHO", 0, 2026));
        assertNumericOnly(BMICalculator.calculateAndClassify(68f, 170f, "ASIAN", -1, 2026));
        assertNumericOnly(BMICalculator.calculateAndClassify(68f, 170f, "WHO", 2027, 2026));
        assertNumericOnly(BMICalculator.calculateAndClassify(68f, 170f, "ASIAN", 1905, 2026));
    }

    @Test
    public void ageAwareBmi_twentyAndOlderRetainsAdultClassificationAndRange() {
        BMICalculator.BmiResult result = BMICalculator.calculateAndClassify(68f, 170f, "WHO", 2006, 2026);
        assertEquals("Bình thường", result.getCategory());
        assertEquals(53.5f, result.getMinHealthyWeight(), 0.01f);
        assertEquals(72f, result.getMaxHealthyWeight(), 0.01f);
        assertEquals("Thừa cân", BMICalculator.calculateAndClassify(68f, 170f, "ASIAN", 1980, 2026).getCategory());
    }

    private static void assertNumericOnly(BMICalculator.BmiResult result) {
        assertEquals(23.53f, result.getBmi(), 0.01f);
        assertEquals("", result.getCategory());
        assertEquals(vn.edu.tdmu.vita.R.color.vita_text_secondary, result.getColorRes());
        assertEquals(0f, result.getMinHealthyWeight(), 0f);
        assertEquals(0f, result.getMaxHealthyWeight(), 0f);
    }

    @Test
    public void latestWeight_replacesRegistrationWeight() {
        assertEquals(75f, BMICalculator.currentWeight(75f, 68f), 0f);
        assertEquals(68f, BMICalculator.currentWeight(null, 68f), 0f);
    }

    @Test
    public void classification_usesUnroundedMeasurement() {
        // 66,46 / 1,7^2 = 22,9965: làm tròn không được khiến kết quả bị xếp sang thừa cân.
        assertEquals("Bình thường",
                BMICalculator.calculateAndClassify(66.46f, 170f, "ASIAN").getCategory());
    }

    @Test
    public void testCalculate_ValidInputs() {
        // Chiều cao: 170 cm, cân nặng: 68 kg
        // BMI = 68 / (1,7 * 1,7) = 23,529...
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
        // Chiều cao 170 cm, cân nặng 68 kg -> BMI khoảng 23,53
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
