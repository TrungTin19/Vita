package vn.edu.tdmu.vita;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import vn.edu.tdmu.vita.activities.MainActivity;
import vn.edu.tdmu.vita.fragments.ReminderFragment;
import vn.edu.tdmu.vita.fragments.StatsFragment;
import vn.edu.tdmu.vita.database.DatabaseHelper;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.database.SleepDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.SleepLog;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.SessionManager;

/**
 * Chạy trên thiết bị: menu Sửa/Xóa giấc ngủ + dòng trạng thái uống thuốc.
 */
@RunWith(AndroidJUnit4.class)
public class SleepMedicineUiTest {

    private Context context;
    private long userId;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DatabaseHelper.DATABASE_NAME);

        UserDao userDao = new UserDao(context);
        User user = new User();
        user.setUsername("ui_test");
        user.setHeightCm(170f);
        user.setBaseWeightKg(65f);
        userId = userDao.register(user, "matkhau123");
        assertTrue(userId > 0);
        new SessionManager(context).saveSession(userId, "ui_test", "");

        SleepDao sleepDao = new SleepDao(context);
        long sleepId = sleepDao.insert(new SleepLog(0, userId,
                DateTimeUtils.getTodayDate(), "23:00", "06:30", 7.5f));
        assertTrue(sleepId > 0);

        MedicineDao medicineDao = new MedicineDao(context);
        long medId = medicineDao.insert(new Medicine(0, userId,
                "Panadol", "1 vien", "08:00", 127, true));
        assertTrue(medId > 0);
    }

    @Test
    public void sleepContextMenu_deleteRemovesItem() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new StatsFragment())
                    .commitNow());
            onView(withId(R.id.cv_sleep_item)).perform(scrollTo(), longClick());
            onView(withText("Xóa giấc ngủ này")).perform(click());
            onView(withText("Xóa")).perform(click());
            onView(withId(R.id.tv_sleep_empty)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void medicineItem_showsTodayStatus() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ReminderFragment())
                    .commitNow());
            onView(withText("Hôm nay: chưa xác nhận")).check(matches(isDisplayed()));
        }
    }
}
