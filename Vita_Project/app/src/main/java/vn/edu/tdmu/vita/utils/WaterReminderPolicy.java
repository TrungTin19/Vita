package vn.edu.tdmu.vita.utils;

public final class WaterReminderPolicy {
    private WaterReminderPolicy() {}
    private static int minute(String time) {
        if (time == null || !time.matches("([01]\\d|2[0-3]):[0-5]\\d")) return -1;
        return Integer.parseInt(time.substring(0, 2)) * 60 + Integer.parseInt(time.substring(3));
    }
    public static long nextTriggerMillis(String start, String end, long now) {
        return nextTriggerMillis(start, end, "", now);
    }

    public static String normalizeTimes(String start, String end, String times) {
        if (!validSettings(start, end, 250) || times == null) throw new IllegalArgumentException("Invalid window");
        if (times.isEmpty()) return "";
        String normalized = MedicineSchedule.normalizeTimes(times);
        for (String time : normalized.split(",")) {
            if (!shouldNotify(start, end, minute(time), 0, 1)) throw new IllegalArgumentException("Time outside window");
        }
        return normalized;
    }

    public static long nextTriggerMillis(String start, String end, String times, long now) {
        try {
            String schedule = normalizeTimes(start, end, times);
            if (schedule.isEmpty()) {
                int from = minute(start), duration = (minute(end) - from + 1440) % 1440;
                StringBuilder slots = new StringBuilder();
                for (int offset = 0; offset <= duration; offset += 120) {
                    int slot = (from + offset) % 1440;
                    if (slots.length() > 0) slots.append(',');
                    slots.append(String.format(java.util.Locale.US, "%02d:%02d", slot / 60, slot % 60));
                }
                schedule = slots.toString();
            }
            return MedicineSchedule.computeNextTriggerMillis(schedule, 127, now);
        } catch (IllegalArgumentException invalid) {
            return -1;
        }
    }

    public static boolean validSettings(String start, String end, int amount) {
        return minute(start) >= 0 && minute(end) >= 0 && !start.equals(end)
                && amount >= 50 && amount <= 2000;
    }
    public static boolean shouldNotify(String start, String end, int nowMinute, int total, int goal) {
        int from = minute(start), to = minute(end);
        if (from < 0 || to < 0 || from == to || nowMinute < 0 || nowMinute >= 1440
                || goal <= 0 || total >= goal) return false;
        return from < to ? nowMinute >= from && nowMinute <= to : nowMinute >= from || nowMinute <= to;
    }
}
