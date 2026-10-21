package vn.edu.tdmu.vita;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

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
