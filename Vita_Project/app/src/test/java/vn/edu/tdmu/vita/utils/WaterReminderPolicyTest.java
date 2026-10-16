package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class WaterReminderPolicyTest {
    @Test public void nextTrigger_entersShortWindowAndSkipsSleepingHours() {
        java.util.Calendar now=java.util.Calendar.getInstance(); now.set(2026,9,16,6,0,0); now.set(java.util.Calendar.MILLISECOND,0);
        java.util.Calendar next=(java.util.Calendar)now.clone(); next.set(java.util.Calendar.HOUR_OF_DAY,7);
        assertEquals(next.getTimeInMillis(),WaterReminderPolicy.nextTriggerMillis("07:00","07:30",now.getTimeInMillis()));
        now.set(java.util.Calendar.HOUR_OF_DAY,7); now.set(java.util.Calendar.MINUTE,20);
        next.add(java.util.Calendar.DAY_OF_YEAR,1);
        assertEquals(next.getTimeInMillis(),WaterReminderPolicy.nextTriggerMillis("07:00","07:30",now.getTimeInMillis()));
    }
    @Test public void reminders_stayInWakeWindowAndStopAtGoal() {
        assertTrue(WaterReminderPolicy.shouldNotify("07:00", "22:00", 7 * 60, 1500, 2000));
        assertFalse(WaterReminderPolicy.shouldNotify("07:00", "22:00", 22 * 60, 1500, 2000));
        assertFalse(WaterReminderPolicy.shouldNotify("07:00", "22:00", 3 * 60, 1500, 2000));
        assertFalse(WaterReminderPolicy.shouldNotify("07:00", "22:00", 12 * 60, 2000, 2000));
        assertFalse(WaterReminderPolicy.shouldNotify("07:00", "22:00", 12 * 60, 2500, 2000));
    }

    @Test public void overnightWindow_andCustomAmountsAreValidated() {
        assertTrue(WaterReminderPolicy.shouldNotify("20:00", "06:00", 23 * 60, 0, 2000));
        assertTrue(WaterReminderPolicy.shouldNotify("20:00", "06:00", 5 * 60, 0, 2000));
        assertFalse(WaterReminderPolicy.shouldNotify("20:00", "06:00", 12 * 60, 0, 2000));
        assertFalse(WaterReminderPolicy.validSettings("07:00", "07:00", 250));
        assertFalse(WaterReminderPolicy.validSettings("25:00", "22:00", 250));
        assertFalse(WaterReminderPolicy.validSettings("07:00", "22:00", 0));
        assertTrue(WaterReminderPolicy.validSettings("07:00", "22:00", 350));
    }
}
