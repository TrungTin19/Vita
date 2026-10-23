package vn.edu.tdmu.vita;

import static org.junit.Assert.assertTrue;

import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.StatusBarNotification;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdmu.vita.database.DatabaseHelper;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver;
import vn.edu.tdmu.vita.utils.SessionManager;
import vn.edu.tdmu.vita.utils.NotificationHelper;

/**
 * Chạy trên thiết bị: đặt lịch thuốc sau 1 phút, chờ báo thức và xác nhận thông báo xuất hiện.
 */
@RunWith(AndroidJUnit4.class)
public class MedicineAlarmDeviceTest {

    private Context context;
    private Medicine scheduledMedicine;
    private long owner;

    @After public void cleanUp() {
        if (scheduledMedicine != null) MedicineAlarmReceiver.cancel(context, scheduledMedicine);
        context.getSystemService(NotificationManager.class).cancelAll();
        if (owner > 0) new UserDao(context).deleteProfileWithPassword(owner, "matkhau123");
        new SessionManager(context).clearSession();
    }

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DatabaseHelper.DATABASE_NAME);
    }

    @Test
    public void alarmFires_notificationShown() throws Exception {
        UserDao userDao = new UserDao(context);
        User user = new User();
        user.setUsername("alarm_test");
        user.setHeightCm(170f);
        user.setBaseWeightKg(65f);
        long userId = userDao.register(user, "matkhau123");
        owner = userId;
        assertTrue(userId > 0);
        new SessionManager(context).saveSession(userId, "alarm_test", "");
        assertTrue("Test requires notification permission", NotificationHelper.canNotify(context));

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1);
        String time = String.format(Locale.US, "%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

        MedicineDao medicineDao = new MedicineDao(context);
        Medicine medicine = new Medicine(0, userId, "ThuocTest", "1 vien", time, 127, true);
        long medId = medicineDao.insert(medicine);
        assertTrue(medId > 0);
        medicine.setId(medId);
        scheduledMedicine = medicine;

        MedicineAlarmReceiver.schedule(context, medicine);

        boolean shown = false;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        for (int i = 0; i < 30; i++) {
            Thread.sleep(4000);
            for (StatusBarNotification sbn : manager.getActiveNotifications()) {
                if (sbn.getId() == NotificationHelper.doseNotificationId(medId,
                        vn.edu.tdmu.vita.utils.DateTimeUtils.getTodayDate(), time)) {
                    shown = true;
                    break;
                }
            }
            if (shown) break;
        }
        assertTrue("Alarm khong no trong 120 giay", shown);
    }
}
