package vn.edu.tdmu.vita;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.MainActivity;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver;
import vn.edu.tdmu.vita.receivers.WaterReminderReceiver;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.MedicineDoseDialogs;
import vn.edu.tdmu.vita.utils.SessionManager;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FigmaDialogDeviceTest {
    private Context context;
    private long owner;
    private Medicine medicine;
    private MedicineDao medicines;
    private SessionManager session;

    @Before public void setup() {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("figma_dialog_" + System.nanoTime());
        owner = new UserDao(context).register(user, "test-password"); assertTrue(owner > 0);
        session = new SessionManager(context); session.saveSession(owner, user.getUsername(), "Test");
        medicines = new MedicineDao(context);
        long id = medicines.insert(new Medicine(0, owner, "Vitamin C", "1 viên", "08:00", 127, false));
        medicine = medicines.getById(id, owner); assertNotNull(medicine);
    }

    @After public void cleanup() {
        MedicineAlarmReceiver.cancel(context, medicine); WaterReminderReceiver.cancel(context);
        new UserDao(context).deleteProfileWithPassword(owner, "test-password"); session.clearSession();
    }

    @Test public void doseDialog_cancelTakeChangeAndClear_preserveDoseSemantics() {
        String date = DateTimeUtils.getTodayDate();
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a -> MedicineDoseDialogs.edit(a, owner, medicine, date, "08:00", null));
            onView(withId(android.R.id.button2)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            assertNull(medicines.getLogForDose(medicine.getId(), owner, date, "08:00"));
            screen.onActivity(a -> MedicineDoseDialogs.edit(a, owner, medicine, date, "08:00", null));
            onView(withId(android.R.id.button1)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            assertEquals("TAKEN", medicines.getLogForDose(medicine.getId(), owner, date, "08:00").getStatus());
            screen.onActivity(a -> MedicineDoseDialogs.edit(a, owner, medicine, date, "08:00", null));
            onView(withId(android.R.id.button1)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            assertEquals("SKIPPED", medicines.getLogForDose(medicine.getId(), owner, date, "08:00").getStatus());
            screen.onActivity(a -> MedicineDoseDialogs.edit(a, owner, medicine, date, "08:00", null));
            onView(withId(android.R.id.button3)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            assertNull(medicines.getLogForDose(medicine.getId(), owner, date, "08:00"));
        }
    }

    @Test public void medicineDelete_cancelKeepsData_andChangedSessionCannotDelete() {
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a -> MedicineDoseDialogs.delete(a, owner, medicine, null));
            onView(withId(android.R.id.button2)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            assertNotNull(medicines.getById(medicine.getId(), owner));
            screen.onActivity(a -> MedicineDoseDialogs.delete(a, owner, medicine, null));
            session.clearSession();
            onView(withId(android.R.id.button1)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            assertNotNull(medicines.getById(medicine.getId(), owner));
        }
    }
}
