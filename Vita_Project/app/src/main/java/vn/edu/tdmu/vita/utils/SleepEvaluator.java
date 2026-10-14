package vn.edu.tdmu.vita.utils;

/**
 * Phân loại chất lượng giấc ngủ theo tổng thời lượng.
 * ponytail: spec 14 bỏ ngỏ khoảng 6-7h và 9-10h -> gộp 6-<7 "Hơi thiếu ngủ",
 * >9 "Ngủ nhiều" để mọi input đều có nhãn.
 */
public final class SleepEvaluator {

    public static final int STATUS_OVERSLEEP = 3;

    public static class SleepResult {
        public final String label;
        public final int severity; // GOOD, WARNING (HealthEvaluator) hoặc STATUS_OVERSLEEP

        public SleepResult(String label, int severity) {
            this.label = label;
            this.severity = severity;
        }
    }

    private SleepEvaluator() {}

    public static SleepResult classify(float hours) {
        if (hours < 6f) return new SleepResult("Thiếu ngủ", HealthEvaluator.STATUS_WARNING);
        if (hours < 7f) return new SleepResult("Hơi thiếu ngủ", HealthEvaluator.STATUS_WARNING);
        if (hours <= 9f) return new SleepResult("Đủ giấc", HealthEvaluator.STATUS_GOOD);
        return new SleepResult("Ngủ nhiều", STATUS_OVERSLEEP);
    }
}
