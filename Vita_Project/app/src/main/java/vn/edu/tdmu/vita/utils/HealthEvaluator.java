package vn.edu.tdmu.vita.utils;

/**
 * Health metric evaluation utilities.
 * Blood pressure classification follows Vietnamese Heart Association
 * (Hội Tim mạch học Việt Nam) standards — 5 levels based on JNC 7/ESH.
 * Uses the HIGHER category when systolic and diastolic fall in different levels.
 */
public class HealthEvaluator {

    public static final int STATUS_GOOD = 0;
    public static final int STATUS_WARNING = 1;
    public static final int STATUS_DANGER = 2;

    public static class BpResult {
        public final String label;
        public final int severity; // STATUS_GOOD, STATUS_WARNING, STATUS_DANGER

        public BpResult(String label, int severity) {
            this.label = label;
            this.severity = severity;
        }
    }

    /**
     * Classifies blood pressure according to Vietnamese Heart Association standards.
     * When systolic and diastolic fall into different categories,
     * the HIGHER (worse) category is used.
     *
     * Levels:
     * - Bình thường tối ưu: < 120 AND < 80
     * - Bình thường: 120-129 OR 80-84
     * - Tiền tăng huyết áp: 130-139 OR 85-89
     * - Tăng huyết áp độ 1: 140-159 OR 90-99
     * - Tăng huyết áp độ 2: >= 160 OR >= 100
     */
    public static BpResult classifyBloodPressure(int systolic, int diastolic) {
        int sysLevel = classifySystolic(systolic);
        int diaLevel = classifyDiastolic(diastolic);
        int level = Math.max(sysLevel, diaLevel);

        switch (level) {
            case 0: return new BpResult("Bình thường tối ưu", STATUS_GOOD);
            case 1: return new BpResult("Bình thường", STATUS_GOOD);
            case 2: return new BpResult("Tiền tăng huyết áp", STATUS_WARNING);
            case 3: return new BpResult("Tăng huyết áp độ 1", STATUS_DANGER);
            default: return new BpResult("Tăng huyết áp độ 2", STATUS_DANGER);
        }
    }

    private static int classifySystolic(int sys) {
        if (sys < 120) return 0;
        if (sys < 130) return 1;
        if (sys < 140) return 2;
        if (sys < 160) return 3;
        return 4;
    }

    private static int classifyDiastolic(int dia) {
        if (dia < 80) return 0;
        if (dia < 85) return 1;
        if (dia < 90) return 2;
        if (dia < 100) return 3;
        return 4;
    }

    /**
     * Classifies heart rate.
     * Normal range: 60-100 bpm (resting, adults).
     */
    public static String classifyHeartRate(int bpm) {
        if (bpm < 60) return "Nhịp chậm";
        if (bpm <= 100) return "Bình thường";
        return "Nhịp nhanh";
    }

    /**
     * Returns a brief advisory text for the BP result.
     */
    public static String getBpAdvice(BpResult result) {
        switch (result.severity) {
            case STATUS_GOOD:
                return "Huyết áp ổn định, tiếp tục duy trì lối sống lành mạnh.";
            case STATUS_WARNING:
                return "Huyết áp ở mức cần theo dõi. Hạn chế muối, tập thể dục đều đặn.";
            case STATUS_DANGER:
                return "Huyết áp cao, cần tham vấn bác sĩ để được tư vấn điều trị.";
            default:
                return "";
        }
    }
}
