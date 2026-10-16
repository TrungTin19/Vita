package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class DateTimeUtilsTest {

    @Test
    public void sleepStartDate_usesPreviousDayOnlyForOvernightSleep() {
        assertEquals("2026-10-15", DateTimeUtils.sleepStartDate("2026-10-16", "23:00", "07:00"));
        assertEquals("2026-10-16", DateTimeUtils.sleepStartDate("2026-10-16", "13:00", "14:00"));
        assertEquals("2025-12-31", DateTimeUtils.sleepStartDate("2026-01-01", "23:00", "07:00"));
    }

    @Test
    public void invalidClockTimes_doNotCreateSleepDuration() {
        assertEquals(0f, DateTimeUtils.calculateSleepDuration("25:00", "07:00"), 0f);
        assertEquals(0f, DateTimeUtils.calculateSleepDuration("23:70", "07:00"), 0f);
        assertEquals(0f, DateTimeUtils.calculateSleepDuration("23:00", "23:00"), 0f);
    }

    @Test
    public void todayAndTime_matchFormats() {
        assertTrue(DateTimeUtils.getTodayDate().matches("\\d{4}-\\d{2}-\\d{2}"));
        assertTrue(DateTimeUtils.getCurrentTime().matches("\\d{2}:\\d{2}"));
        assertTrue(DateTimeUtils.getCurrentTimestamp().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void formatDisplayDate_convertsToVietnamese() {
        assertEquals("14/10/2026", DateTimeUtils.formatDisplayDate("2026-10-14"));
        assertEquals("", DateTimeUtils.formatDisplayDate(null));
        assertEquals("", DateTimeUtils.formatDisplayDate("  "));
        assertEquals("not-a-date", DateTimeUtils.formatDisplayDate("not-a-date"));
    }

    @Test
    public void calculateSleepDuration_handlesOvernight() {
        assertEquals(8.0f, DateTimeUtils.calculateSleepDuration("23:00", "07:00"), 0.01f);
        assertEquals(7.5f, DateTimeUtils.calculateSleepDuration("23:00", "06:30"), 0.01f);
        assertEquals(8.0f, DateTimeUtils.calculateSleepDuration("22:00", "06:00"), 0.01f);
    }

    @Test
    public void calculateSleepDuration_invalid_returnsZero() {
        assertEquals(0f, DateTimeUtils.calculateSleepDuration(null, "07:00"), 0.001f);
        assertEquals(0f, DateTimeUtils.calculateSleepDuration("23:00", null), 0.001f);
        assertEquals(0f, DateTimeUtils.calculateSleepDuration("xx", "07:00"), 0.001f);
    }
}
