package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;

/**
 * TDD RED: MedicineSchedule chưa tồn tại -> test phải FAIL khi chạy.
 * Quy ước bit: bit0=T2 ... bit6=CN (127 = hằng ngày).
 */
public class MedicineScheduleTest {

    private static long millis(int year, int month0, int day, int hour, int min) {
        Calendar c = Calendar.getInstance();
        c.set(year, month0, day, hour, min, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    @Test
    public void daily_futureTimeToday_returnsToday() {
        // T2 12/10/2026 08:00, hẹn 09:00 hằng ngày -> hôm nay 09:00
        long now = millis(2026, Calendar.OCTOBER, 12, 8, 0);
        long next = MedicineSchedule.computeNextTriggerMillis("09:00", 127, now);
        assertEquals(millis(2026, Calendar.OCTOBER, 12, 9, 0), next);
    }

    @Test
    public void daily_pastTimeToday_returnsTomorrow() {
        // T2 08:00 đã qua 09:00? Không: dùng now=10:00, hẹn 09:00 -> T3 09:00
        long now = millis(2026, Calendar.OCTOBER, 12, 10, 0);
        long next = MedicineSchedule.computeNextTriggerMillis("09:00", 127, now);
        assertEquals(millis(2026, Calendar.OCTOBER, 13, 9, 0), next);
    }

    @Test
    public void mondayOnly_futureTime_returnsToday() {
        long now = millis(2026, Calendar.OCTOBER, 12, 8, 0); // T2
        long next = MedicineSchedule.computeNextTriggerMillis("09:00", 1, now); // bit0 = T2
        assertEquals(millis(2026, Calendar.OCTOBER, 12, 9, 0), next);
    }

    @Test
    public void mondayOnly_pastTime_returnsNextMonday() {
        long now = millis(2026, Calendar.OCTOBER, 12, 10, 0); // T2, đã qua giờ
        long next = MedicineSchedule.computeNextTriggerMillis("09:00", 1, now);
        assertEquals(millis(2026, Calendar.OCTOBER, 19, 9, 0), next); // T2 tuần sau
    }

    @Test
    public void sundayOnly_fromMonday_returnsSunday() {
        long now = millis(2026, Calendar.OCTOBER, 12, 8, 0); // T2
        long next = MedicineSchedule.computeNextTriggerMillis("07:30", 64, now); // bit6 = CN
        assertEquals(millis(2026, Calendar.OCTOBER, 18, 7, 30), next); // CN 18/10
    }

    @Test
    public void emptyMask_returnsMinusOne() {
        long now = millis(2026, Calendar.OCTOBER, 12, 8, 0);
        assertEquals(-1L, MedicineSchedule.computeNextTriggerMillis("09:00", 0, now));
    }

    @Test
    public void invalidTime_returnsMinusOne() {
        long now = millis(2026, Calendar.OCTOBER, 12, 8, 0);
        assertEquals(-1L, MedicineSchedule.computeNextTriggerMillis("xx", 127, now));
        assertEquals(-1L, MedicineSchedule.computeNextTriggerMillis(null, 127, now));
        assertEquals(-1L, MedicineSchedule.computeNextTriggerMillis("25:99", 127, now));
    }
}
