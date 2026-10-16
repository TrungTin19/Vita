package vn.edu.tdmu.vita.utils;

public final class WaterReminderPolicy {
    private WaterReminderPolicy() {}
    private static int minute(String time) {
        if (time == null || !time.matches("([01]\\d|2[0-3]):[0-5]\\d")) return -1;
        return Integer.parseInt(time.substring(0, 2)) * 60 + Integer.parseInt(time.substring(3));
    }
    public static long nextTriggerMillis(String start, String end, long now) {
        if (!validSettings(start, end, 250)) return -1;
        java.util.Calendar current = java.util.Calendar.getInstance(); current.setTimeInMillis(now);
        int nowMinute = current.get(java.util.Calendar.HOUR_OF_DAY) * 60 + current.get(java.util.Calendar.MINUTE);
        java.util.Calendar candidate = (java.util.Calendar) current.clone();
        candidate.add(java.util.Calendar.HOUR_OF_DAY, 2);
        int nextMinute = candidate.get(java.util.Calendar.HOUR_OF_DAY) * 60 + candidate.get(java.util.Calendar.MINUTE);
        if (shouldNotify(start, end, nowMinute, 0, 1) && shouldNotify(start, end, nextMinute, 0, 1))
            return candidate.getTimeInMillis();
        current.set(java.util.Calendar.HOUR_OF_DAY, minute(start) / 60);
        current.set(java.util.Calendar.MINUTE, minute(start) % 60);
        current.set(java.util.Calendar.SECOND, 0); current.set(java.util.Calendar.MILLISECOND, 0);
        if (current.getTimeInMillis() <= now) current.add(java.util.Calendar.DAY_OF_YEAR, 1);
        return current.getTimeInMillis();
    }

    public static boolean validSettings(String start, String end, int amount) {
        return minute(start) >= 0 && minute(end) >= 0 && !start.equals(end)
                && amount >= 50 && amount <= 2000;
    }
    public static boolean shouldNotify(String start, String end, int nowMinute, int total, int goal) {
        int from = minute(start), to = minute(end);
        if (from < 0 || to < 0 || from == to || nowMinute < 0 || nowMinute >= 1440
                || goal <= 0 || total >= goal) return false;
        return from < to ? nowMinute >= from && nowMinute < to : nowMinute >= from || nowMinute < to;
    }
}
