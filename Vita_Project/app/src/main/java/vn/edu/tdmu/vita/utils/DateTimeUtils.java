package vn.edu.tdmu.vita.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
            // Trả về chuỗi ban đầu nếu không thể phân tích dữ liệu
            return dateStr;
        }
        return dateStr;
    }

}
