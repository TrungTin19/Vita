package vn.edu.tdmu.vita;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import vn.edu.tdmu.vita.activities.MainActivity;
import vn.edu.tdmu.vita.fragments.ReminderFragment;
import vn.edu.tdmu.vita.database.DatabaseHelper;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.SessionManager;

/**
 * Chạy trên thiết bị: dòng trạng thái uống thuốc.
 */
@RunWith(AndroidJUnit4.class)
public class MedicineStatusUiTest {

    private Context context;
    private long userId;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));

        UserDao userDao = new UserDao(context);
        User user = new User();
        user.setUsername("ui_test_" + System.nanoTime());
        user.setHeightCm(170f);
        user.setBaseWeightKg(65f);
        userId = userDao.register(user, "matkhau123");
        assertTrue(userId > 0);
        new SessionManager(context).saveSession(userId, user.getUsername(), "");

        MedicineDao medicineDao = new MedicineDao(context);
        long medId = medicineDao.insert(new Medicine(0, userId,
                "Panadol", "1 vien", "08:00", 127, true));
        assertTrue(medId > 0);
    }

    @After public void cleanUp() {
        new UserDao(context).deleteProfileWithPassword(userId, "matkhau123");
        new SessionManager(context).clearSession();
    }

    @Test
    public void stoppedMedicineKeepsHistoryAndCanResumeWithoutEnablingAlarms() {
        MedicineDao dao = new MedicineDao(context);
        Medicine medicine = dao.getAllByUserId(userId).get(0);
        long id = medicine.getId();
        medicine.setTime("08:00,20:00");
        assertTrue(dao.update(medicine, userId));
        String today = vn.edu.tdmu.vita.utils.DateTimeUtils.getTodayDate();
        assertTrue(dao.logMedicineStatus(id, userId, today, "08:00", "TAKEN", today + " 08:00:00") > 0);

        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(new android.content.Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_REMINDER))) {
            onView(withId(R.id.tab_medicine_list)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withId(R.id.btn_med_history)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withText(R.string.medicine_tracking_stop)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withId(android.R.id.button1)).perform(click());
            onView(withText(R.string.medicine_tracking_stopped)).check(matches(isDisplayed()));
            assertFalse(dao.toggleActive(id, userId, true));
            assertNull(dao.getNextUnloggedDose(userId, System.currentTimeMillis()));
            assertEquals(1, dao.getHistory(id, userId).size());
            assertEquals(-1L, dao.logMedicineStatus(id, userId, today, "20:00", "TAKEN", today + " 20:00:00"));

            onView(withId(R.id.tab_today_doses)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(allOf(withId(R.id.tv_dose_time), withText("08:00"))).check(matches(isDisplayed()));
            onView(allOf(withId(R.id.tv_dose_time), withText("20:00"))).check(doesNotExist());
            onView(withId(R.id.tab_medicine_list)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withId(R.id.btn_med_history)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withText(R.string.medicine_tracking_resume)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            assertFalse(dao.getById(id, userId).isActive());
            assertEquals(1, dao.getHistory(id, userId).size());
            assertTrue(dao.toggleActive(id, userId, true));
        }
    }

    @Test
    public void historyLabelsScheduledAndConfirmationTimesSeparately() {
        MedicineDao dao = new MedicineDao(context);
        long id = dao.getAllByUserId(userId).get(0).getId();
        assertTrue(dao.logMedicineStatus(id, userId, "2025-10-21", "23:55",
                "TAKEN", "2025-10-22 00:05:00") > 0);

        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(new android.content.Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_REMINDER))) {
            onView(withId(R.id.tab_medicine_list)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withId(R.id.btn_med_history)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withText("Giờ theo lịch: 21/10/2025 · 23:55")).check(matches(isDisplayed()));
            onView(withText(org.hamcrest.Matchers.containsString("Xác nhận lúc: 2025-10-22 00:05:00")))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void deletingMedicineAlsoRemovesItsTodayDoses() {
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(new android.content.Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_REMINDER))) {
            onView(withId(R.id.tab_medicine_list)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withId(R.id.cv_medicine_item)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), androidx.test.espresso.action.ViewActions.longClick());
            onView(withText(R.string.reminder_context_menu_delete)).perform(click());
            onView(withId(android.R.id.button1)).perform(click());
            onView(withId(R.id.tab_today_doses)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withId(R.id.layout_today_doses)).check(matches(org.hamcrest.Matchers.not(
                    androidx.test.espresso.matcher.ViewMatchers.hasDescendant(withText(org.hamcrest.Matchers.containsString("Panadol"))))));
        }
    }

    @Test
    public void reminderTabs_separateTodayFromPrescriptionList() {
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(new android.content.Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_REMINDER))) {
            onView(withId(R.id.layout_today_doses)).check(matches(isDisplayed()));
            onView(withId(R.id.rv_medicines)).check(matches(org.hamcrest.Matchers.not(isDisplayed())));
            onView(withId(R.id.tab_medicine_list)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withId(R.id.rv_medicines)).check(matches(isDisplayed()));
            onView(withId(R.id.layout_today_doses)).check(matches(org.hamcrest.Matchers.not(isDisplayed())));
            onView(withId(R.id.tab_today_doses)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            onView(withId(R.id.layout_today_doses)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void medicineItem_showsTodayStatus() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ReminderFragment())
                    .commitNow());
            onView(withId(R.id.layout_today_doses)).check(matches(androidx.test.espresso.matcher.ViewMatchers.hasDescendant(
                    withText(org.hamcrest.Matchers.containsString("Panadol")))));
        }
    }
}
