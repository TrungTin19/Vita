package vn.edu.tdmu.vita.utils;

import java.util.Calendar;

/**
 * Tính mốc báo thức kế tiếp từ giờ "HH:mm" + mặt nạ bit 7 ngày.
 * ponytail: bit0=T2 ... bit6=CN; pure Java để test JVM được, dùng chung cho
 * AlarmReceiver/BootReceiver/AddMedicineActivity.
 */
public final class MedicineSchedule {

    private MedicineSchedule() {}

    public static long computeNextTriggerMillis(String timeHHmm, int daysMask, long nowMillis) {
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
            int bit = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // T2=0 ... CN=6
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
