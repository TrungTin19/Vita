package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class DateTimeUtilsTest {

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

}
