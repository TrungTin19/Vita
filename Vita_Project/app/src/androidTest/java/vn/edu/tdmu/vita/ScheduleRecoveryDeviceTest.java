package vn.edu.tdmu.vita;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import vn.edu.tdmu.vita.receivers.*;
import vn.edu.tdmu.vita.utils.SessionManager;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ScheduleRecoveryDeviceTest {
    @Test public void timezoneChangeReplacesExistingDoseAlarm() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("clock_" + System.nanoTime());
        UserDao users = new UserDao(context); long owner = users.register(user, "test-password");
        SessionManager session = new SessionManager(context); session.saveSession(owner, user.getUsername(), "");
        Medicine medicine = new Medicine(0, owner, "Clock", "1", "23:59", 127, true);
        medicine.setId(new MedicineDao(context).insert(medicine));
        Intent alarm = new Intent(context, MedicineAlarmReceiver.class)
                .setData(Uri.parse("vita://alarm/" + medicine.getId() + "/23:59"));
        try {
            MedicineAlarmReceiver.schedule(context, medicine);
            PendingIntent before = PendingIntent.getBroadcast(context, 0, alarm, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            assertNotNull(before);
            new BootReceiver().onReceive(context, new Intent(Intent.ACTION_TIMEZONE_CHANGED));
            PendingIntent after = PendingIntent.getBroadcast(context, 0, alarm, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            assertNotNull(after); assertNotEquals(before, after);
        } finally {
            MedicineAlarmReceiver.cancel(context, medicine); WaterReminderReceiver.cancel(context);
            users.deleteProfileWithPassword(owner, "test-password"); session.clearSession();
        }
    }
}
