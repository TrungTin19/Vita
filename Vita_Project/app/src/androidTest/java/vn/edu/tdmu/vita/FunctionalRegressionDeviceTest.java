package vn.edu.tdmu.vita;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import vn.edu.tdmu.vita.activities.ProfileActivity;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import vn.edu.tdmu.vita.receivers.NotificationActionReceiver;
import vn.edu.tdmu.vita.receivers.WaterReminderReceiver;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.SessionManager;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FunctionalRegressionDeviceTest {
    private Context context;
    private SessionManager session;
    private final List<Long> users = new ArrayList<>();
    private long oldUser;
    private String oldName;
    private String oldFullname;
    private boolean oldLoggedIn;

    @Before public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        session = new SessionManager(context);
        oldUser = session.getUserId();
        oldName = session.getUsername();
        oldFullname = session.getFullname();
        oldLoggedIn = session.isLoggedIn();
    }

    private long user() {
        User user = new User();
        user.setUsername("regression_" + System.nanoTime());
        user.setHeightCm(170f);
        user.setBaseWeightKg(68f);
        long id = new UserDao(context).register(user, "testpass123");
        assertTrue(id > 0);
        users.add(id);
        return id;
    }

    @After public void tearDown() {
        try (DatabaseHelper helper = new DatabaseHelper(context)) {
            for (Long id : users) {
                helper.getWritableDatabase().delete(DatabaseHelper.TABLE_USERS,
                        "id = ?", new String[]{String.valueOf(id)});
            }
        }
        if (oldLoggedIn) session.saveSession(oldUser, oldName, oldFullname);
        else session.clearSession();
    }

    @Test public void dateRange_includesBothBoundariesAndOnlyCurrentUser() {
        long owner = user();
        long other = user();
        HealthDao dao = new HealthDao(context);
        assertTrue(dao.insert(new HealthRecord(0, owner, "2026-10-14", 67f, null, null, null, null, null)) > 0);
        assertTrue(dao.insert(new HealthRecord(0, owner, "2026-10-15", 68f, null, null, null, null, null)) > 0);
        assertTrue(dao.insert(new HealthRecord(0, owner, "2026-10-16", 69f, null, null, null, null, null)) > 0);
        assertTrue(dao.insert(new HealthRecord(0, other, "2026-10-16", 99f, null, null, null, null, null)) > 0);
        List<HealthRecord> records = dao.getRecordsRange(owner, "2026-10-15", "2026-10-16");
        assertEquals(2, records.size());
        assertEquals("2026-10-15", records.get(0).getDate());
        assertEquals(Float.valueOf(69f), records.get(1).getWeightKg());
    }

    @Test public void profileBmi_usesLatestRecordedWeight() {
        long owner = user();
        session.saveSession(owner, "test", "Test");
        assertTrue(new HealthDao(context).insert(new HealthRecord(0, owner,
                DateTimeUtils.getTodayDate(), 75f, null, null, null, null, null)) > 0);
        try (ActivityScenario<ProfileActivity> activity = ActivityScenario.launch(ProfileActivity.class)) {
            activity.onActivity(screen -> assertEquals(String.format(Locale.getDefault(), "%.1f", 26f),
                    ((TextView) screen.findViewById(R.id.tv_profile_bmi_value)).getText().toString()));
        }
    }

    @Test public void oldNotification_doesNotWriteToAnotherAccount() {
        long owner = user();
        long other = user();
        MedicineDao medicines = new MedicineDao(context);
        long med = medicines.insert(new Medicine(0, owner, "Test", "1", "08:00", 127, true));
        session.saveSession(other, "other", "Other");
        Intent taken = new Intent(NotificationActionReceiver.ACTION_TAKEN)
                .putExtra(NotificationActionReceiver.EXTRA_MEDICINE_ID, med);
        new NotificationActionReceiver().onReceive(context, taken);
        assertNull(medicines.getLogForDose(med, DateTimeUtils.getTodayDate(), "08:00"));
        Intent drink = new Intent(WaterReminderReceiver.ACTION_DRINKED)
                .putExtra(WaterReminderReceiver.EXTRA_USER_ID, owner);
        new WaterReminderReceiver().onReceive(context, drink);
        assertEquals(0, new WaterDao(context).getDailyTotal(other, DateTimeUtils.getTodayDate()));
    }
}
