package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;

/**
 * Ghi chú TDD ban đầu: MedicineSchedule chưa được cài đặt nên kiểm thử khi đó phải thất bại.
 * Quy ước bit: bit0=Thứ Hai ... bit6=Chủ Nhật (127 = hằng ngày).
 */
public class MedicineScheduleTest {

    @Test
    public void multipleTimes_selectsNextDoseToday() {
        long now = millis(2026, Calendar.OCTOBER, 12, 8, 0);
        assertEquals(millis(2026, Calendar.OCTOBER, 12, 12, 0),
                MedicineSchedule.computeNextTriggerMillis("19:00,07:00,12:00", 127, now));
        assertEquals(millis(2026, Calendar.OCTOBER, 13, 7, 0),
                MedicineSchedule.computeNextTriggerMillis("07:00,12:00,19:00", 127,
                        millis(2026, Calendar.OCTOBER, 12, 20, 0)));
    }

    @Test
    public void times_areSortedUniqueAndValidated() {
        assertEquals("07:00,12:00,19:00", MedicineSchedule.normalizeTimes("19:00, 07:00,12:00,07:00"));
        assertEquals(-1, MedicineSchedule.computeNextTriggerMillis("07:00,25:00", 127, 0));
        try {
            MedicineSchedule.normalizeTimes("07:00,,12:00");
            fail("Empty dose time must be rejected");
        } catch (IllegalArgumentException expected) { }
    }

    private static long millis(int year, int month0, int day, int hour, int min) {
        Calendar c = Calendar.getInstance();
        c.set(year, month0, day, hour, min, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    @Test
    public void daily_futureTimeToday_returnsToday() {
        // Thứ Hai 12/10/2026 lúc 08:00, hẹn 09:00 hằng ngày -> lần kế tiếp hôm nay lúc 09:00
        long now = millis(2026, Calendar.OCTOBER, 12, 8, 0);
        long next = MedicineSchedule.computeNextTriggerMillis("09:00", 127, now);
        assertEquals(millis(2026, Calendar.OCTOBER, 12, 9, 0), next);
    }

    @Test
    public void daily_pastTimeToday_returnsTomorrow() {
        // Đã qua 09:00 chưa? Có: hiện là 10:00, hẹn 09:00 -> lần kế tiếp Thứ Ba lúc 09:00
        long now = millis(2026, Calendar.OCTOBER, 12, 10, 0);
        long next = MedicineSchedule.computeNextTriggerMillis("09:00", 127, now);
        assertEquals(millis(2026, Calendar.OCTOBER, 13, 9, 0), next);
    }

    @Test
    public void mondayOnly_futureTime_returnsToday() {
        long now = millis(2026, Calendar.OCTOBER, 12, 8, 0); // Thứ Hai
        long next = MedicineSchedule.computeNextTriggerMillis("09:00", 1, now); // bit0 = Thứ Hai
        assertEquals(millis(2026, Calendar.OCTOBER, 12, 9, 0), next);
    }

    @Test
    public void mondayOnly_pastTime_returnsNextMonday() {
        long now = millis(2026, Calendar.OCTOBER, 12, 10, 0); // Thứ Hai, đã qua giờ hẹn
        long next = MedicineSchedule.computeNextTriggerMillis("09:00", 1, now);
        assertEquals(millis(2026, Calendar.OCTOBER, 19, 9, 0), next); // Thứ Hai tuần sau
    }

    @Test
    public void sundayOnly_fromMonday_returnsSunday() {
        long now = millis(2026, Calendar.OCTOBER, 12, 8, 0); // Thứ Hai
        long next = MedicineSchedule.computeNextTriggerMillis("07:30", 64, now); // bit6 = Chủ Nhật
        assertEquals(millis(2026, Calendar.OCTOBER, 18, 7, 30), next); // Chủ Nhật 18/10
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
