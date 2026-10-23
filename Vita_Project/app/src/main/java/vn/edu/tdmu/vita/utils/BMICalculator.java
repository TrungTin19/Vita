package vn.edu.tdmu.vita.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import vn.edu.tdmu.vita.R;

/**
 * Lớp tiện ích tính chỉ số khối cơ thể (BMI) và phân loại tình trạng dinh dưỡng
 * theo tiêu chuẩn châu Á (IDI và WPRO) lẫn tiêu chuẩn quốc tế (WHO).
 */
public final class BMICalculator {

    public static final String STANDARD_ASIAN = "ASIAN";
    public static final String STANDARD_WHO = "WHO";

    private BMICalculator() {
        // Ngăn tạo đối tượng từ lớp tiện ích này
    }

    public static float currentWeight(Float latestWeight, float baseWeight) {
        return latestWeight != null ? latestWeight : baseWeight;
    }

    /**
     * Lớp dữ liệu lưu kết quả tính BMI và phân loại đầy đủ.
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
     * Tính BMI từ cân nặng (kg) và chiều cao (cm).
     * BMI = cân nặng (kg) / [chiều cao (m)]^2
     *
     * @param weightKg cân nặng tính bằng kilôgam
     * @param heightCm chiều cao tính bằng xentimét
     * @return BMI làm tròn đến 2 chữ số thập phân, hoặc 0.0 nếu dữ liệu không hợp lệ
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
     * Phân loại chỉ số BMI theo tiêu chuẩn được chọn.
     *
     * @param bmi chỉ số BMI đã tính
     * @param standard "ASIAN" hoặc "WHO" (mặc định là ASIAN)
     * @return BmiResult chứa nhóm phân loại và màu sắc
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
            // Tiêu chuẩn châu Á (IDI và WPRO / Viện Dinh dưỡng Quốc gia Việt Nam)
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
     * Tính BMI, phân loại tình trạng sức khỏe và tính khoảng cân nặng phù hợp.
     *
     * @param weightKg cân nặng tính bằng kg
     * @param heightCm chiều cao tính bằng cm
     * @param standard "ASIAN" or "WHO"
     * @return BmiResult chứa đầy đủ kết quả
     */
    public static BmiResult calculateAndClassify(float weightKg, float heightCm, String standard,
                                                int birthYear, int currentYear) {
        int approximateAge = currentYear - birthYear;
        // shortcut: chỉ có năm sinh nên tuổi mang tính ước lượng; cần ngày sinh đầy đủ nếu muốn tính tuổi chính xác.
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
