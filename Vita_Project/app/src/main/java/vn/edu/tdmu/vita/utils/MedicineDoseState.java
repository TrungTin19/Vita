package vn.edu.tdmu.vita.utils;

public enum MedicineDoseState {
    UPCOMING, UNCONFIRMED, TAKEN, SKIPPED;

    public static MedicineDoseState classify(String date, String time, String status, long nowMillis) {
        if ("TAKEN".equals(status)) return TAKEN;
        if ("SKIPPED".equals(status)) return SKIPPED;
        if (date == null || time == null) return UNCONFIRMED;
        String now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                .format(new java.util.Date(nowMillis));
        return (date + " " + time).compareTo(now) > 0 ? UPCOMING : UNCONFIRMED;
    }
}
