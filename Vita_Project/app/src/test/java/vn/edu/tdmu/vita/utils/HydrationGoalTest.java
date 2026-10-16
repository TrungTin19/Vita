package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import vn.edu.tdmu.vita.models.WeatherData;
import static org.junit.Assert.*;

public class HydrationGoalTest {
    @Test public void weatherOptOut_keepsPersonalGoalOnHotDay() {
        assertEquals(1500, HydrationGoal.forDay(1500, hot(), "Hà Nội", "2026-10-16", false));
        assertEquals(1800, HydrationGoal.forDay(1500, hot(), "Hà Nội", "2026-10-16", true));
    }
    private WeatherData hot() {
        return new WeatherData(33f, 70, 120, "Hà Nội", true, "2026-10-16 10:00:00");
    }

    @Test
    public void hotDay_adds300WithoutAccumulating() {
        assertEquals(2300, HydrationGoal.forDay(2000, hot(), "Hà Nội", "2026-10-16", true));
        assertEquals(2300, HydrationGoal.forDay(2000, hot(), "Hà Nội", "2026-10-16", true));
        WeatherData cool = hot();
        cool.setTemperature(32.9f);
        assertEquals(2000, HydrationGoal.forDay(2000, cool, "Hà Nội", "2026-10-16", true));
    }

    @Test
    public void staleOrOtherCity_doesNotChangeGoal() {
        assertEquals(2000, HydrationGoal.forDay(2000, hot(), "Đà Nẵng", "2026-10-16", true));
        assertEquals(2000, HydrationGoal.forDay(2000, hot(), "Hà Nội", "2026-10-17", true));
        assertEquals(2000, HydrationGoal.forDay(2000, null, "Hà Nội", "2026-10-16", true));
        assertEquals(2000, HydrationGoal.forDay(0, null, "Hà Nội", "2026-10-16", true));
    }
}
