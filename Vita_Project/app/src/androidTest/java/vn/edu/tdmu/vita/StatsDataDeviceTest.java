package vn.edu.tdmu.vita;

import android.content.Context;
import android.widget.Spinner;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.MainActivity;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import vn.edu.tdmu.vita.utils.*;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.*;

@RunWith(AndroidJUnit4.class)
public class StatsDataDeviceTest {
    @Test public void emptyAndSingleMeasurement_showDifferentStates_andFirstActionOpensWeightForm() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("stats_empty_" + System.nanoTime());
        UserDao users = new UserDao(context); long owner = users.register(user, "test-password");
        new SessionManager(context).saveSession(owner, user.getUsername(), "");
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a -> {
                ((com.google.android.material.bottomnavigation.BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_stats);
                a.getSupportFragmentManager().executePendingTransactions();
                android.view.View empty = a.findViewById(context.getResources().getIdentifier("layout_stats_empty", "id", context.getPackageName()));
                assertNotNull("Empty chart must offer the first measurement action", empty);
                assertEquals(android.view.View.VISIBLE, empty.getVisibility());
            });
            int action = context.getResources().getIdentifier("btn_stats_first_measurement", "id", context.getPackageName());
            onView(androidx.test.espresso.matcher.ViewMatchers.withId(action)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                android.app.Activity form = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED).iterator().next();
                assertTrue(form instanceof vn.edu.tdmu.vita.activities.AddHealthActivity);
                assertEquals(3, form.getIntent().getIntExtra(vn.edu.tdmu.vita.activities.AddHealthActivity.EXTRA_METRIC_GROUP, -1));
            });
            androidx.test.espresso.Espresso.closeSoftKeyboard();
            androidx.test.espresso.Espresso.pressBack();
            onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.sp_stats_metric)).check(matches(
                    androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            HealthRecord record = new HealthRecord(0, owner, DateTimeUtils.getTodayDate(), 62f, null, null, null, null, null);
            record.setMeasuredTime("07:30"); assertTrue(new HealthDao(context).insert(record) > 0);
            screen.onActivity(a -> ((vn.edu.tdmu.vita.fragments.StatsFragment) a.getSupportFragmentManager().findFragmentByTag("tab:" + R.id.nav_stats)).loadAll());
            screen.onActivity(a -> {
                android.view.View empty = a.findViewById(context.getResources().getIdentifier("layout_stats_empty", "id", context.getPackageName()));
                assertEquals(android.view.View.GONE, empty.getVisibility());
                assertEquals(android.view.View.VISIBLE, a.findViewById(R.id.chart_weight).getVisibility());
            });
        } finally { users.deleteProfileWithPassword(owner, "test-password"); new SessionManager(context).clearSession(); }
    }

    @Test public void newestFirstRowsOpenTheirOwnRecord() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("stats_rows_" + System.nanoTime());
        UserDao users = new UserDao(context); long owner = users.register(user, "test-password");
        new SessionManager(context).saveSession(owner, user.getUsername(), "");
        HealthDao dao = new HealthDao(context);
        HealthRecord older = new HealthRecord(0, owner, DateTimeUtils.getTodayDate(), 60f, null, null, null, null, null);
        older.setMeasuredTime("07:00"); long olderId = dao.insert(older);
        HealthRecord newer = new HealthRecord(0, owner, DateTimeUtils.getTodayDate(), 61f, null, null, null, null, null);
        newer.setMeasuredTime("10:00"); long newerId = dao.insert(newer);
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a -> ((com.google.android.material.bottomnavigation.BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_stats));
            long[] expected = {newerId, olderId};
            for (int position = 0; position < expected.length; position++) {
                final long id = expected[position];
                onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_stats_data)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
                final int row = position;
                final String time = position == 0 ? "10:00" : "07:00";
                onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.list_stats_data))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).check((view, error) -> {
                            if (error != null) throw error;
                            assertTrue(((android.widget.ListView) view).getAdapter().getItem(row).toString().contains(time));
                        });
                onView(withText(org.hamcrest.Matchers.containsString(time)))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
                onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.tv_detail_weight)).check(matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                    android.app.Activity activity = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED).iterator().next();
                    assertEquals(id, activity.getIntent().getLongExtra(vn.edu.tdmu.vita.activities.DetailHealthActivity.EXTRA_RECORD_ID, -1));
                });
                androidx.test.espresso.Espresso.pressBack();
            }
        } finally { users.deleteProfileWithPassword(owner, "test-password"); new SessionManager(context).clearSession(); }
    }
    @Test public void glucoseTableUsesSelectedMealContext() {
        Context context=ApplicationProvider.getApplicationContext(); assertTrue(context.getPackageName().endsWith(".verification"));
        User user=new User(); user.setUsername("stats_"+System.nanoTime()); UserDao users=new UserDao(context);
        long owner=users.register(user,"test-password"); new SessionManager(context).saveSession(owner,user.getUsername(),"");
        HealthDao health=new HealthDao(context);
        HealthRecord morning=new HealthRecord(0,owner,DateTimeUtils.getTodayDate(),null,null,null,null,95f,null);
        morning.setMeasuredTime("07:00"); morning.setMealContext("FASTING"); health.insert(morning);
        HealthRecord evening=new HealthRecord(0,owner,DateTimeUtils.getTodayDate(),null,null,null,null,140f,null);
        evening.setMeasuredTime("19:00"); evening.setMealContext("AFTER_MEAL"); health.insert(evening);
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a->{
                ((com.google.android.material.bottomnavigation.BottomNavigationView)a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_stats);
                a.getSupportFragmentManager().executePendingTransactions();
                Spinner metric=a.findViewById(context.getResources().getIdentifier("sp_stats_metric","id",context.getPackageName()));
                assertNotNull("Statistics must let the user choose one metric",metric); metric.setSelection(3);
            });
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            screen.onActivity(a->((Spinner)a.findViewById(context.getResources().getIdentifier("sp_stats_context","id",context.getPackageName()))).setSelection(1));
            onView(withText("Xem dữ liệu")).perform(androidx.test.espresso.action.ViewActions.scrollTo(),click());
            onView(withText(org.hamcrest.Matchers.containsString("95"))).check(matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            onView(withText(org.hamcrest.Matchers.containsString("140"))).check(doesNotExist());
        } finally { users.deleteProfileWithPassword(owner,"test-password"); new SessionManager(context).clearSession(); }
    }
}
