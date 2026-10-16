package vn.edu.tdmu.vita.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

public class DateTimeUtils {

    public static final String DATE_FORMAT_DB = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_DISPLAY = "dd/MM/yyyy";

    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DB, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatDisplayDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat(DATE_FORMAT_DB, Locale.getDefault());
            Date date = dbFormat.parse(dateStr);
            if (date != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault());
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            // Return raw string if parse fails
            return dateStr;
        }
        return dateStr;
    }

    /**
     * Calculates sleep duration in hours from HH:mm sleepTime to HH:mm wakeTime.
     * Handles overnight periods automatically (e.g., 23:00 to 07:00 = 8.0h).
     */
    public static float calculateSleepDuration(String sleepTime, String wakeTime) {
        if (sleepTime == null || wakeTime == null) return 0f;
        String[] sleepParts = sleepTime.split(":");
        String[] wakeParts = wakeTime.split(":");
        if (sleepParts.length < 2 || wakeParts.length < 2) return 0f;

        try {
            int sleepHour = Integer.parseInt(sleepParts[0].trim());
            int sleepMin = Integer.parseInt(sleepParts[1].trim());
            int wakeHour = Integer.parseInt(wakeParts[0].trim());
            int wakeMin = Integer.parseInt(wakeParts[1].trim());

            if (sleepHour < 0 || sleepHour > 23 || wakeHour < 0 || wakeHour > 23
                    || sleepMin < 0 || sleepMin > 59 || wakeMin < 0 || wakeMin > 59) return 0f;

            int sleepTotalMinutes = sleepHour * 60 + sleepMin;
            int wakeTotalMinutes = wakeHour * 60 + wakeMin;

            int diffMinutes = wakeTotalMinutes - sleepTotalMinutes;
            if (diffMinutes < 0) {
                // Slept past midnight
                diffMinutes += 24 * 60;
            }

            return diffMinutes / 60.0f;
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public static String sleepStartDate(String wakeDate, String sleepTime, String wakeTime) {
        if (sleepTime.compareTo(wakeTime) <= 0) return wakeDate;
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_DB, Locale.US);
        format.setLenient(false);
        try {
            Date date = format.parse(wakeDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            return format.format(calendar.getTime());
        } catch (ParseException e) {
            return wakeDate;
        }
    }
}
