package vn.edu.tdmu.vita.models;

import org.junit.Test;
import static org.junit.Assert.*;

public class ModelsTest {

    @Test
    public void testUserInstantiation() {
        User user = new User(1L, "trungtin", "hash123", "salt123", "Huỳnh Trung Tín", 1980, "Nam", 168.0f, 64.0f, 2000, "ASIAN");
        assertEquals(1L, user.getId());
        assertEquals("trungtin", user.getUsername());
        assertEquals("ASIAN", user.getBmiStandard());
        assertEquals(2000, user.getWaterGoalMl());
    }

    @Test
    public void testHealthRecordNullableMetrics() {
        HealthRecord record = new HealthRecord(10L, 1L, "2026-10-09", null, 120, 80, 75, null, "Cảm giác khỏe");
        assertEquals(10L, record.getId());
        assertEquals("2026-10-09", record.getDate());
        assertNull(record.getWeightKg());
        assertEquals(Integer.valueOf(120), record.getSystolic());
        assertEquals(Integer.valueOf(80), record.getDiastolic());
        assertNull(record.getBloodSugar());
    }

    @Test
    public void testMedicineAndLog() {
        Medicine med = new Medicine(100L, 1L, "Panadol Extra", "1 viên sau ăn", "08:00", 127, true);
        assertEquals("Panadol Extra", med.getName());
        assertEquals(127, med.getDaysMask());
        assertTrue(med.isActive());

        MedicineLog log = new MedicineLog(1001L, 100L, "2026-10-09", "TAKEN", "2026-10-09 08:05");
        assertEquals("TAKEN", log.getStatus());
    }

    @Test
    public void testWaterAndSleepLog() {
        WaterLog water = new WaterLog(201L, 1L, "2026-10-09", 250, "2026-10-09 09:00");
        assertEquals(250, water.getAmountMl());

        SleepLog sleep = new SleepLog(301L, 1L, "2026-10-09", "23:00", "06:30", 7.5f);
        assertEquals(7.5f, sleep.getDurationH(), 0.01f);
    }

    @Test
    public void testWeatherData() {
        WeatherData weather = new WeatherData(33.0f, 68, 65, "Bình Dương", false, "08:30");
        assertEquals(33.0f, weather.getTemperature(), 0.01f);
        assertEquals(65, weather.getAqi());
        assertFalse(weather.isOffline());
    }
}
