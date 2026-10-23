package vn.edu.tdmu.vita.utils;

import java.util.Calendar;
import java.util.TreeSet;

/**
 * Tính thời điểm báo thức kế tiếp từ giờ "HH:mm" và mặt nạ bit của 7 ngày.
 * ponytail: bit0=Thứ Hai ... bit6=Chủ Nhật; dùng Java thuần để kiểm thử JVM, dùng chung cho
 * AlarmReceiver/BootReceiver/AddMedicineActivity.
 */
public final class MedicineSchedule {

    private MedicineSchedule() {}

    public static String normalizeTimes(String input) {
        if (input == null || input.trim().isEmpty()) throw new IllegalArgumentException("Choose a dose time");
        TreeSet<String> times = new TreeSet<>();
        for (String value : input.split(",", -1)) {
            String time = value.trim();
            if (!time.matches("([01]\\d|2[0-3]):[0-5]\\d")) throw new IllegalArgumentException("Invalid dose time");
            times.add(time);
        }
        StringBuilder result = new StringBuilder();
        for (String time : times) {
            if (result.length() > 0) result.append(',');
            result.append(time);
        }
        return result.toString();
    }

    public static long computeNextTriggerMillis(String input, int daysMask, long nowMillis) {
        try {
            long next = Long.MAX_VALUE;
            for (String time : normalizeTimes(input).split(",")) {
                long candidate = computeSingleTrigger(time, daysMask, nowMillis);
                if (candidate > 0) next = Math.min(next, candidate);
            }
            return next == Long.MAX_VALUE ? -1 : next;
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    private static long computeSingleTrigger(String timeHHmm, int daysMask, long nowMillis) {
        if (timeHHmm == null || daysMask == 0) return -1;
        String[] parts = timeHHmm.split(":");
        if (parts.length != 2) return -1;
        int hour, min;
        try {
            hour = Integer.parseInt(parts[0].trim());
            min = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return -1;
        }
        if (hour < 0 || hour > 23 || min < 0 || min > 59) return -1;

        for (int offset = 0; offset <= 7; offset++) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(nowMillis);
            c.add(Calendar.DAY_OF_YEAR, offset);
            int bit = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Thứ Hai=0 ... CN=6
            if ((daysMask & (1 << bit)) == 0) continue;
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, min);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            if (c.getTimeInMillis() > nowMillis) return c.getTimeInMillis();
        }
        return -1;
    }
}
