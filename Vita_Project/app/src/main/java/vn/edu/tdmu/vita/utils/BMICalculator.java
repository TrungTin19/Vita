package vn.edu.tdmu.vita.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import vn.edu.tdmu.vita.R;

/**
 * Utility class calculating Body Mass Index (BMI) and classifying nutritional status
 * supporting both Asian (IDI & WPRO) and International (WHO) standards.
 */
public final class BMICalculator {

    public static final String STANDARD_ASIAN = "ASIAN";
    public static final String STANDARD_WHO = "WHO";

    private BMICalculator() {
        // Prevent instantiation
    }

    public static float currentWeight(Float latestWeight, float baseWeight) {
        return latestWeight != null ? latestWeight : baseWeight;
    }

    /**
     * Data class holding complete BMI calculation and classification result.
     */
    public static class BmiResult {
        private final float bmi;
        private final String category;
        private final int colorRes;
        private final float minHealthyWeight;
        private final float maxHealthyWeight;

        public BmiResult(float bmi, String category, int colorRes, float minHealthyWeight, float maxHealthyWeight) {
            this.bmi = bmi;
            this.category = category;
            this.colorRes = colorRes;
            this.minHealthyWeight = minHealthyWeight;
            this.maxHealthyWeight = maxHealthyWeight;
        }

        public float getBmi() {
            return bmi;
        }

        public String getCategory() {
            return category;
        }

        public int getColorRes() {
            return colorRes;
        }

        public float getMinHealthyWeight() {
            return minHealthyWeight;
        }

        public float getMaxHealthyWeight() {
            return maxHealthyWeight;
        }
    }

    /**
     * Calculates BMI from weight (kg) and height (cm).
     * BMI = weight (kg) / [height (m)]^2
     *
     * @param weightKg body weight in kilograms
     * @param heightCm body height in centimeters
     * @return rounded BMI value (2 decimal places), or 0.0 if inputs are invalid
     */
    public static float calculate(float weightKg, float heightCm) {
        if (weightKg <= 0 || heightCm <= 0) {
            return 0.0f;
        }
        float heightM = heightCm / 100.0f;
        float rawBmi = weightKg / (heightM * heightM);
        return BigDecimal.valueOf(rawBmi)
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }

    /**
     * Classifies a given BMI value according to the specified standard.
     *
     * @param bmi calculated BMI
     * @param standard "ASIAN" or "WHO" (defaults to ASIAN)
     * @return BmiResult with category and color
     */
    public static BmiResult classify(float bmi, String standard) {
        if (bmi <= 0) {
            return new BmiResult(0.0f, "Chưa xác định", R.color.vita_text_secondary, 0f, 0f);
        }

        boolean isWho = STANDARD_WHO.equalsIgnoreCase(standard);
        String category;
        int colorRes;

        if (bmi < 18.5f) {
            category = "Thiếu cân";
            colorRes = R.color.vita_aqua;
        } else if (isWho) {
            if (bmi < 25.0f) {
                category = "Bình thường";
                colorRes = R.color.vita_status_good;
            } else if (bmi < 30.0f) {
                category = "Thừa cân";
                colorRes = R.color.vita_status_warning;
            } else if (bmi < 35.0f) {
                category = "Béo phì độ I";
                colorRes = R.color.vita_status_danger;
            } else if (bmi < 40.0f) {
                category = "Béo phì độ II";
                colorRes = R.color.vita_status_danger;
            } else {
                category = "Béo phì độ III";
                colorRes = R.color.vita_status_danger;
            }
        } else {
            // Asian (IDI & WPRO / National Institute of Nutrition Vietnam)
            if (bmi < 23.0f) {
                category = "Bình thường";
                colorRes = R.color.vita_status_good;
            } else if (bmi < 25.0f) {
                category = "Thừa cân";
                colorRes = R.color.vita_status_warning;
            } else if (bmi < 30.0f) {
                category = "Béo phì độ I";
                colorRes = R.color.vita_status_danger;
            } else {
                category = "Béo phì độ II";
                colorRes = R.color.vita_status_danger;
            }
        }

        return new BmiResult(bmi, category, colorRes, 0f, 0f);
    }

    /**
     * Calculates BMI, classifies health status, and computes the healthy weight range.
     *
     * @param weightKg weight in kg
     * @param heightCm height in cm
     * @param standard "ASIAN" or "WHO"
     * @return complete BmiResult
     */
    public static BmiResult calculateAndClassify(float weightKg, float heightCm, String standard,
                                                int birthYear, int currentYear) {
        int approximateAge = currentYear - birthYear;
        // shortcut: year-only age is approximate; use a full birth date if exact age is needed.
        if (birthYear <= 0 || approximateAge < 20 || approximateAge > 120) {
            return new BmiResult(calculate(weightKg, heightCm), "", R.color.vita_text_secondary, 0f, 0f);
        }
        return calculateAndClassify(weightKg, heightCm, standard);
    }

    public static BmiResult calculateAndClassify(float weightKg, float heightCm, String standard) {
        float bmi = calculate(weightKg, heightCm);
        BmiResult classification = classify(weightKg > 0 && heightCm > 0
                ? weightKg / ((heightCm / 100f) * (heightCm / 100f)) : 0f, standard);

        float minHealthyWeight = 0f;
        float maxHealthyWeight = 0f;

        if (heightCm > 0) {
            float heightM = heightCm / 100.0f;
            float heightMSquared = heightM * heightM;
            boolean isWho = STANDARD_WHO.equalsIgnoreCase(standard);

            float minBmi = 18.5f;
            float maxBmi = isWho ? 24.9f : 22.9f;

            minHealthyWeight = BigDecimal.valueOf(minBmi * heightMSquared)
                    .setScale(1, RoundingMode.HALF_UP)
                    .floatValue();
            maxHealthyWeight = BigDecimal.valueOf(maxBmi * heightMSquared)
                    .setScale(1, RoundingMode.HALF_UP)
                    .floatValue();
        }

        return new BmiResult(bmi, classification.getCategory(), classification.getColorRes(), minHealthyWeight, maxHealthyWeight);
    }
}
