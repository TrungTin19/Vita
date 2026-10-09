package vn.edu.tdmu.vita.utils;

import java.util.Calendar;

/**
 * Utility class for validating user input fields across registration and profile updates.
 */
public final class InputValidator {

    public static final float MIN_HEIGHT_CM = 50.0f;
    public static final float MAX_HEIGHT_CM = 250.0f;

    public static final float MIN_WEIGHT_KG = 20.0f;
    public static final float MAX_WEIGHT_KG = 300.0f;

    public static final int MIN_BIRTH_YEAR = 1900;

    public static final int MIN_WATER_GOAL_ML = 100;
    public static final int MAX_WATER_GOAL_ML = 10000;

    private InputValidator() {
        // Prevent instantiation
    }

    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        String trimmed = username.trim();
        return trimmed.length() >= 3 && !trimmed.contains(" ");
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.trim().length() >= 6;
    }

    public static boolean isValidHeight(float heightCm) {
        return heightCm >= MIN_HEIGHT_CM && heightCm <= MAX_HEIGHT_CM;
    }

    public static boolean isValidWeight(float weightKg) {
        return weightKg >= MIN_WEIGHT_KG && weightKg <= MAX_WEIGHT_KG;
    }

    public static boolean isValidBirthYear(int birthYear) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        return birthYear >= MIN_BIRTH_YEAR && birthYear <= currentYear;
    }

    public static boolean isValidWaterGoal(int waterGoalMl) {
        return waterGoalMl >= MIN_WATER_GOAL_ML && waterGoalMl <= MAX_WATER_GOAL_ML;
    }
}
