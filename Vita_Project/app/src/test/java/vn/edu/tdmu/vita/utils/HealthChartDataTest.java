package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import java.util.Arrays;
import vn.edu.tdmu.vita.models.HealthRecord;
import static org.junit.Assert.*;

public class HealthChartDataTest {
    @Test
    public void glucose_keepsSeparateMeasurementsAndMealContext() {
        HealthRecord morning = new HealthRecord(1, 1, "2026-10-16", null, null, null, null, 95f, null);
        morning.setMeasuredTime("07:00");
        morning.setMealContext("FASTING");
        HealthRecord evening = new HealthRecord(2, 1, "2026-10-16", null, null, null, null, 140f, null);
        evening.setMeasuredTime("19:00");
        evening.setMealContext("AFTER_MEAL");
        java.util.List<HealthChartData.Point> points = HealthChartData.metricPoints(
                Arrays.asList(morning, evening), Arrays.asList("2026-10-16"), HealthChartData.Metric.BLOOD_SUGAR);
        assertEquals(2, points.size());
        assertTrue(points.get(0).x < points.get(1).x);
        assertEquals(95f, points.get(0).value, 0f);
        assertEquals("19:00", points.get(1).record.getMeasuredTime());
        assertEquals(HealthRecord.MealContext.AFTER_MEAL, points.get(1).record.getMealContext());
        assertNull(new HealthRecord().getMeasuredTime());
        assertEquals(HealthRecord.MealContext.UNSPECIFIED, new HealthRecord().getMealContext());
    }
    @Test
    public void metricPoints_skipNullsAndPreserveDayPositions() {
        HealthRecord first = new HealthRecord(1, 1, "2026-10-14", 68f, null, null, 72, null, null);
        HealthRecord empty = new HealthRecord(2, 1, "2026-10-15", null, null, null, null, null, null);
        HealthRecord last = new HealthRecord(3, 1, "2026-10-16", 69f, null, null, 75, null, null);
        java.util.List<HealthChartData.Point> points = HealthChartData.metricPoints(
                Arrays.asList(first, empty, last),
                Arrays.asList("2026-10-14", "2026-10-15", "2026-10-16"), HealthChartData.Metric.WEIGHT);
        assertEquals(2, points.size());
        assertEquals(0, points.get(0).dayIndex);
        assertEquals(2, points.get(1).dayIndex);
        assertEquals(69f, points.get(1).value, 0f);
        assertEquals(75f, HealthChartData.metricPoints(Arrays.asList(last),
                Arrays.asList("2026-10-16"), HealthChartData.Metric.HEART_RATE).get(0).value, 0f);
    }
}
