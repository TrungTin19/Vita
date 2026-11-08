package vn.edu.tdmu.vita.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

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
    public void storedDatesAndTimesUseAsciiAndGregorianYearInEveryLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"));
            todayAndTime_matchFormats();
            Locale.setDefault(Locale.forLanguageTag("th-TH"));
            String year = String.valueOf(new GregorianCalendar().get(Calendar.YEAR));
            assertTrue(DateTimeUtils.getTodayDate().startsWith(year + "-"));
            assertTrue(DateTimeUtils.getCurrentTimestamp().startsWith(year + "-"));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void formatDisplayDate_convertsToVietnamese() {
        assertEquals("14/10/2026", DateTimeUtils.formatDisplayDate("2026-10-14"));
        assertEquals("", DateTimeUtils.formatDisplayDate(null));
        assertEquals("", DateTimeUtils.formatDisplayDate("  "));
        assertEquals("not-a-date", DateTimeUtils.formatDisplayDate("not-a-date"));
    }

}
