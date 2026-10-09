package vn.edu.tdmu.vita.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import vn.edu.tdmu.vita.R;

/**
 * Utility class providing BMI (Body Mass Index) calculations and clinical classifications
 * according to WHO and Asian (IDI & WPRO) standards.
 */
public final class BmiUtils {

    public static final String STANDARD_ASIAN = "ASIAN";
    public static final String STANDARD_WHO = "WHO";

    private BmiUtils() {
        // Prevent instantiation
    }

    /**
     * Calculates BMI from height in centimeters and weight in kilograms.
     * BMI = weight (kg) / [height (m)]^2
     *
     * @param heightCm height in cm
     * @param weightKg weight in kg
     * @return rounded BMI value (1 decimal place), or 0.0 if inputs are invalid
     */
    public static double calculateBmi(double heightCm, double weightKg) {
        if (heightCm <= 0 || weightKg <= 0) {
            return 0.0;
        }
        double heightM = heightCm / 100.0;
        double rawBmi = weightKg / (heightM * heightM);
        return BigDecimal.valueOf(rawBmi)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Classifies BMI value according to standard ("ASIAN" or "WHO").
     *
     * @param bmi calculated BMI
     * @param standard "ASIAN" or "WHO" (defaults to ASIAN)
     * @return Classification label in Vietnamese
     */
    public static String classifyBmi(double bmi, String standard) {
        if (bmi <= 0) {
            return "Chưa xác định";
        }

        boolean isWho = STANDARD_WHO.equalsIgnoreCase(standard);

        if (bmi < 18.5) {
            return "Thiếu cân";
        }

        if (isWho) {
            if (bmi < 25.0) {
                return "Bình thường";
            } else if (bmi < 30.0) {
                return "Thừa cân";
            } else if (bmi < 35.0) {
                return "Béo phì độ I";
            } else {
                return "Béo phì độ II";
            }
        } else {
            // Asian (IDI & WPRO / National Institute of Nutrition Vietnam)
            if (bmi < 23.0) {
                return "Bình thường";
            } else if (bmi < 25.0) {
                return "Thừa cân";
            } else if (bmi < 30.0) {
                return "Béo phì độ I";
            } else {
                return "Béo phì độ II";
            }
        }
    }

    /**
     * Returns color resource ID reflecting the health status corresponding to BMI.
     *
     * @param bmi calculated BMI
     * @param standard "ASIAN" or "WHO"
     * @return Color resource ID
     */
    public static int getStatusColorRes(double bmi, String standard) {
        if (bmi <= 0) {
            return R.color.vita_text_secondary;
        }
        boolean isWho = STANDARD_WHO.equalsIgnoreCase(standard);

        if (bmi < 18.5) {
            return R.color.vita_aqua; // Underweight
        }

        double normalUpper = isWho ? 25.0 : 23.0;
        if (bmi < normalUpper) {
            return R.color.vita_status_good; // Normal (Green)
        }

        double overweightUpper = isWho ? 30.0 : 25.0;
        if (bmi < overweightUpper) {
            return R.color.vita_status_warning; // Overweight (Orange)
        }

        return R.color.vita_status_danger; // Obese (Red)
    }

    /**
     * Calculates the healthy target weight range (kg) for a given height.
     *
     * @param heightCm height in cm
     * @param standard "ASIAN" or "WHO"
     * @return double array of length 2 [minWeightKg, maxWeightKg]
     */
    public static double[] getHealthyWeightRange(double heightCm, String standard) {
        if (heightCm <= 0) {
            return new double[]{0.0, 0.0};
        }
        double heightM = heightCm / 100.0;
        double heightMSquared = heightM * heightM;

        double minBmi = 18.5;
        double maxBmi = STANDARD_WHO.equalsIgnoreCase(standard) ? 24.9 : 22.9;

        double minWeight = BigDecimal.valueOf(minBmi * heightMSquared)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        double maxWeight = BigDecimal.valueOf(maxBmi * heightMSquared)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        return new double[]{minWeight, maxWeight};
    }
}
