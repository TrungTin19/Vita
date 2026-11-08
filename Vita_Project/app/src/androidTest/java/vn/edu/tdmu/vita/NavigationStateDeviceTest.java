package vn.edu.tdmu.vita;

import android.content.Context;
import android.view.View;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.DateSelector;
import com.google.android.material.datepicker.MaterialDatePicker;
import vn.edu.tdmu.vita.activities.MainActivity;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.SessionManager;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NavigationStateDeviceTest {
    @Test public void clockTickRefreshesVisibleDoseGroups() throws Exception {
        Context context=ApplicationProvider.getApplicationContext(); assertTrue(context.getPackageName().endsWith(".verification"));
        User user=new User(); user.setUsername("clock_ui_"+System.nanoTime()); UserDao users=new UserDao(context);
        long owner=users.register(user,"test-password"); new SessionManager(context).saveSession(owner,user.getUsername(),"");
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            capture(context,"home");
            screen.onActivity(a->{
                ((BottomNavigationView)a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_reminder);
                a.getSupportFragmentManager().executePendingTransactions();
                new vn.edu.tdmu.vita.database.MedicineDao(context).insert(new vn.edu.tdmu.vita.models.Medicine(0,owner,"Clock refresh medicine","1 tablet","08:00",127,true));
                try {
                    java.lang.reflect.Field field=MainActivity.class.getDeclaredField("clockReceiver"); field.setAccessible(true);
                    ((android.content.BroadcastReceiver)field.get(a)).onReceive(a,new android.content.Intent(android.content.Intent.ACTION_TIME_TICK));
                } catch(ReflectiveOperationException error) { throw new AssertionError(error); }
            });
            androidx.test.espresso.Espresso.onView(org.hamcrest.Matchers.allOf(
                    androidx.test.espresso.matcher.ViewMatchers.withText(org.hamcrest.Matchers.containsString("Clock refresh medicine")),
                    androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.layout_today_doses))))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            capture(context,"reminder");
        } finally { users.deleteProfileWithPassword(owner,"test-password"); new SessionManager(context).clearSession(); }
    }

    private void capture(Context context,String name) throws Exception {
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        android.graphics.Bitmap bitmap=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull(bitmap);
        try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(context.getExternalFilesDir(null),"upgrade-"+name+".png"))) {
            assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out));
        } finally { bitmap.recycle(); }
    }
    @Test public void statisticsRangeSurvivesTabSwitchAndRecreation() {
        Context context=ApplicationProvider.getApplicationContext(); assertTrue(context.getPackageName().endsWith(".verification"));
        User user=new User(); user.setUsername("navigation_"+System.nanoTime());
        UserDao users=new UserDao(context); long owner=users.register(user,"test-password");
        new SessionManager(context).saveSession(owner,user.getUsername(),"");
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a->{
                BottomNavigationView nav=a.findViewById(R.id.bottom_nav_main);
                nav.setSelectedItemId(R.id.nav_stats); a.getSupportFragmentManager().executePendingTransactions();
                a.findViewById(R.id.btn_range_30).performClick();
                nav.setSelectedItemId(R.id.nav_home); a.getSupportFragmentManager().executePendingTransactions();
                nav.setSelectedItemId(R.id.nav_stats); a.getSupportFragmentManager().executePendingTransactions();
                assertTrue("The 30-day choice was lost when changing tabs",((MaterialButton)a.findViewById(R.id.btn_range_30)).isChecked());
            });
            screen.recreate();
            screen.onActivity(a->assertTrue(((MaterialButton)a.findViewById(R.id.btn_range_30)).isChecked()));
        } finally { users.deleteProfileWithPassword(owner,"test-password"); new SessionManager(context).clearSession(); }
    }

    @Test public void healthDateRangeStillAppliesAfterRecreation() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User();
        user.setUsername("health_range_" + System.nanoTime());
        UserDao users = new UserDao(context);
        long owner = users.register(user, "test-password");
        assertTrue(owner > 0);
        SessionManager session = new SessionManager(context);
        session.saveSession(owner, user.getUsername(), "");
        java.util.Calendar date = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US);
        date.clear();
        date.set(2025, java.util.Calendar.OCTOBER, 21);
        long start = date.getTimeInMillis();
        date.add(java.util.Calendar.DAY_OF_MONTH, 1);
        Pair<Long, Long> selection = new Pair<>(start, date.getTimeInMillis());

        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a -> {
                BottomNavigationView nav = a.findViewById(R.id.bottom_nav_main);
                nav.setSelectedItemId(R.id.nav_health);
                a.getSupportFragmentManager().executePendingTransactions();
                a.findViewById(R.id.btn_health_filter_date).performClick();
                selectRange(healthRangePicker(a), selection);
            });

            screen.recreate();
            screen.onActivity(a -> {
                MaterialDatePicker<?> picker = healthRangePicker(a);
                assertEquals(selection, picker.getSelection());
                View confirm = picker.requireView().findViewById(com.google.android.material.R.id.confirm_button);
                assertTrue(confirm.isEnabled());
                confirm.performClick();
                MaterialButton filter = a.findViewById(R.id.btn_health_filter_date);
                assertEquals(a.getString(R.string.health_filter_range_format, "21/10/2025", "22/10/2025"),
                        filter.getText().toString());
            });
        } finally {
            users.deleteProfileWithPassword(owner, "test-password");
            session.clearSession();
        }
    }

    private static MaterialDatePicker<?> healthRangePicker(MainActivity activity) {
        FragmentManager root = activity.getSupportFragmentManager();
        root.executePendingTransactions();
        Fragment picker = root.findFragmentByTag("health_date_range");
        if (picker == null) {
            Fragment health = root.findFragmentByTag("tab:" + R.id.nav_health);
            assertNotNull(health);
            FragmentManager children = health.getChildFragmentManager();
            children.executePendingTransactions();
            picker = children.findFragmentByTag("health_date_range");
        }
        assertTrue(picker instanceof MaterialDatePicker);
        return (MaterialDatePicker<?>) picker;
    }

    @SuppressWarnings("unchecked")
    private static void selectRange(MaterialDatePicker<?> picker, Pair<Long, Long> selection) {
        try {
            // Đặt dữ liệu chọn để bài kiểm thử tập trung vào việc khôi phục và áp dụng bộ lọc.
            java.lang.reflect.Method getDateSelector = MaterialDatePicker.class.getDeclaredMethod("getDateSelector");
            getDateSelector.setAccessible(true);
            ((DateSelector<Pair<Long, Long>>) getDateSelector.invoke(picker)).setSelection(selection);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }
}
