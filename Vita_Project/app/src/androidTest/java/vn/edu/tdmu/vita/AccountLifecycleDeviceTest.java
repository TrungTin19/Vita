package vn.edu.tdmu.vita;

import android.content.Context;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.ProfileActivity;
import vn.edu.tdmu.vita.activities.RegisterActivity;
import vn.edu.tdmu.vita.activities.MainActivity;
import vn.edu.tdmu.vita.activities.LoginActivity;
import vn.edu.tdmu.vita.database.DatabaseHelper;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver;
import vn.edu.tdmu.vita.utils.ProfileAccountActions;
import vn.edu.tdmu.vita.utils.SessionManager;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.*;

@RunWith(AndroidJUnit4.class)
public class AccountLifecycleDeviceTest {
    private final Context context = ApplicationProvider.getApplicationContext();

    @Test public void deletedProfileSessionIsClearedAndReturnsToLogin() {
        assertTrue(context.getPackageName().endsWith(".verification"));
        UserDao users = new UserDao(context);
        User user = new User(); user.setUsername("missing_owner_" + System.nanoTime());
        long owner = users.register(user, "test-password");
        assertTrue(owner > 0);
        SessionManager session = new SessionManager(context);
        session.saveSession(owner, user.getUsername(), "Deleted profile");
        assertTrue(users.deleteProfileWithPassword(owner, "test-password"));
        android.app.Instrumentation instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation();
        android.app.Instrumentation.ActivityMonitor login = instrumentation.addMonitor(LoginActivity.class.getName(), null, false);
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
            DeviceTestWait.until("Session for deleted profile must be cleared", () -> !session.isLoggedIn());
            assertEquals(-1, session.getUserId());
            assertNotNull("Deleted profile must return to login", instrumentation.waitForMonitorWithTimeout(login, 3000));
        } finally {
            instrumentation.removeMonitor(login);
            session.clearSession();
        }
    }

    private static void replaceDao(Object target, Class<?> type, UserDao dao) {
        try {
            java.lang.reflect.Field field = type.getDeclaredField("userDao");
            field.setAccessible(true);
            field.set(target, dao);
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    private static void await(CountDownLatch latch) {
        try { assertTrue("Background operation timed out", latch.await(10, TimeUnit.SECONDS)); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new AssertionError(error); }
    }

    @Test public void registrationCompletesOnceAfterRecreation() {
        assertTrue(context.getPackageName().endsWith(".verification"));
        SessionManager session = new SessionManager(context);
        session.clearSession();
        String name = "rotated_registration_" + System.nanoTime();
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1);
        UserDao delayed = new UserDao(context) {
            @Override public long register(User user, String password) {
                started.countDown(); await(release);
                return super.register(user, password);
            }
        };
        try (ActivityScenario<RegisterActivity> screen = ActivityScenario.launch(RegisterActivity.class)) {
            screen.onActivity(a -> {
                replaceDao(a, RegisterActivity.class, delayed);
                ((TextView) a.findViewById(R.id.et_reg_username)).setText(name);
                ((TextView) a.findViewById(R.id.et_reg_password)).setText("test-password");
                ((TextView) a.findViewById(R.id.et_reg_confirm)).setText("test-password");
                a.findViewById(R.id.btn_reg_submit).performClick();
                ((TextView) a.findViewById(R.id.et_reg_height)).setText("170");
                ((TextView) a.findViewById(R.id.et_reg_weight)).setText("60");
                a.findViewById(R.id.btn_reg_submit).performClick();
            });
            await(started);
            screen.recreate();
            screen.onActivity(a -> assertFalse(a.findViewById(R.id.btn_reg_submit).isEnabled()));
            release.countDown();
            DeviceTestWait.until("Registration result was lost after recreation", session::isLoggedIn);
            try (DatabaseHelper helper = new DatabaseHelper(context);
                 android.database.Cursor rows = helper.getReadableDatabase().rawQuery(
                         "SELECT COUNT(*) FROM users WHERE username=?", new String[]{name})) {
                assertTrue(rows.moveToFirst()); assertEquals(1, rows.getInt(0));
            }
        } finally {
            release.countDown();
            try (DatabaseHelper helper = new DatabaseHelper(context)) {
                helper.getWritableDatabase().delete("users", "username=?", new String[]{name});
            }
            session.clearSession();
        }
    }

    @Test public void deletionStillClearsSessionAndAlarmsAfterRecreation() {
        assertTrue(context.getPackageName().endsWith(".verification"));
        UserDao users = new UserDao(context);
        User user = new User(); user.setUsername("rotated_delete_" + System.nanoTime());
        long owner = users.register(user, "test-password");
        assertTrue(owner > 0);
        SessionManager session = new SessionManager(context);
        session.saveSession(owner, user.getUsername(), "Test");
        Medicine medicine = new Medicine(0, owner, "Test", "1", "08:00", 127, true);
        medicine.setId(new MedicineDao(context).insert(medicine));
        MedicineAlarmReceiver.schedule(context, medicine);
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1);
        UserDao delayed = new UserDao(context) {
            @Override public boolean deleteProfileWithPassword(long id, String password) {
                started.countDown(); await(release);
                return super.deleteProfileWithPassword(id, password);
            }
        };
        try (ActivityScenario<ProfileActivity> screen = ActivityScenario.launch(ProfileActivity.class)) {
            screen.onActivity(a -> {
                ProfileAccountActions actions = new ProfileAccountActions(a, users.getUserById(owner));
                replaceDao(actions, ProfileAccountActions.class, delayed);
                actions.showDeleteConfirmDialog();
            });
            onView(withId(R.id.et_delete_profile_password)).perform(replaceText("test-password"), closeSoftKeyboard());
            onView(withId(android.R.id.button1)).perform(click());
            await(started);
            screen.recreate();
            release.countDown();
            DeviceTestWait.until("Deleted profile session was retained after recreation", () -> !session.isLoggedIn());
            assertNull(users.getUserById(owner));
            android.content.Intent alarm = new android.content.Intent(context, MedicineAlarmReceiver.class)
                    .setData(android.net.Uri.parse("vita://alarm/" + medicine.getId() + "/08:00"));
            assertNull(android.app.PendingIntent.getBroadcast(context, 0, alarm,
                    android.app.PendingIntent.FLAG_NO_CREATE | android.app.PendingIntent.FLAG_IMMUTABLE));
        } finally {
            release.countDown();
            MedicineAlarmReceiver.cancel(context, medicine);
            users.deleteProfileWithPassword(owner, "test-password");
            session.clearSession();
        }
    }
}
