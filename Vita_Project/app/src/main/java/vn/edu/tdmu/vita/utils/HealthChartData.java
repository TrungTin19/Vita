package vn.edu.tdmu.vita.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vn.edu.tdmu.vita.models.HealthRecord;

public final class HealthChartData {
    private HealthChartData() {}

    public enum Metric { WEIGHT, HEART_RATE, BLOOD_SUGAR, BLOOD_PRESSURE }

    public static final class Point {
        public final int dayIndex;
        public final float value;
        public final float x;
        public final HealthRecord record;

        Point(int dayIndex, float value, HealthRecord record) {
            this.dayIndex = dayIndex;
            this.value = value;
            this.x = measurementX(record, dayIndex);
            this.record = record;
        }
    }

    public static float measurementX(HealthRecord record, int dayIndex) {
        String time = record.getMeasuredTime();
        return dayIndex + (time == null ? 0f : (Integer.parseInt(time.substring(0, 2)) * 60
                + Integer.parseInt(time.substring(3))) / 1440f);
    }

    public static List<Point> metricPoints(List<HealthRecord> records, List<String> days, Metric metric) {
        List<Point> points = new ArrayList<>();
        for (HealthRecord record : records) {
            Number value;
            switch (metric) {
                case WEIGHT: value = record.getWeightKg(); break;
                case HEART_RATE: value = record.getHeartRate(); break;
                case BLOOD_PRESSURE: continue;
                default: value = record.getBloodSugar();
            }
            int index = days.indexOf(record.getDate());
            if (value != null && index >= 0) points.add(new Point(index, value.floatValue(), record));
        }
        return points;
    }
}
