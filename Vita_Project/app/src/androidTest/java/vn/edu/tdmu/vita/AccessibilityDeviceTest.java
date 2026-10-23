package vn.edu.tdmu.vita;

import android.content.Context;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.AddMedicineActivity;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import vn.edu.tdmu.vita.utils.SessionManager;
import vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AccessibilityDeviceTest {
    @Test public void weekdayControlsExposeCheckedStateAndSaveTheSameSchedule() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("a11y_" + System.nanoTime());
        UserDao users = new UserDao(context); long owner = users.register(user, "test-password");
        new SessionManager(context).saveSession(owner, user.getUsername(), "");
        MedicineDao medicines = new MedicineDao(context);
        try {
            try (ActivityScenario<AddMedicineActivity> screen = ActivityScenario.launch(AddMedicineActivity.class)) {
                screen.onActivity(a -> {
                    android.widget.TextView title = a.findViewById(R.id.title_add_medicine);
                    assertNotNull(title.getLayout());
                    for (int line = 0; line < title.getLineCount(); line++)
                        assertEquals("Title must remain readable without ellipsis", 0, title.getLayout().getEllipsisCount(line));
                });
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.et_med_time))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo());
                screen.onActivity(a -> a.findViewById(R.id.et_med_time).requestFocus());
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.et_med_time))
                        .perform(androidx.test.espresso.action.ViewActions.pressKey(android.view.KeyEvent.KEYCODE_ENTER));
                java.util.concurrent.atomic.AtomicReference<android.view.View> pickerRoot = new java.util.concurrent.atomic.AtomicReference<>();
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(android.widget.TimePicker.class))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                        .check((view, error) -> {
                            androidx.test.espresso.assertion.ViewAssertions.matches(
                                    androidx.test.espresso.matcher.ViewMatchers.isDisplayed()).check(view, error);
                            pickerRoot.set(view.getRootView());
                        });
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(android.R.id.button2))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.withDecorView(org.hamcrest.Matchers.is(pickerRoot.get())))
                        .perform(androidx.test.espresso.action.ViewActions.click());
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_day_sun))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo());
                screen.onActivity(a -> {
                    float density = a.getResources().getDisplayMetrics().density;
                    assertTrue("Add-time action must support keyboard focus", a.findViewById(R.id.et_med_time).isFocusable());
                    com.google.android.material.chip.ChipGroup times = a.findViewById(R.id.chips_med_times);
                    com.google.android.material.chip.Chip time = (com.google.android.material.chip.Chip) times.getChildAt(0);
                    com.google.android.material.chip.ChipDrawable drawable = (com.google.android.material.chip.ChipDrawable) time.getChipDrawable();
                    assertTrue("Remove-time target must be at least 48dp wide",
                            drawable.getCloseIconStartPadding() + drawable.getCloseIconSize()
                                    + drawable.getCloseIconEndPadding() >= 48 * density);
                    for (int id : new int[]{R.id.btn_day_mon, R.id.btn_day_tue, R.id.btn_day_wed,
                            R.id.btn_day_thu, R.id.btn_day_fri, R.id.btn_day_sat, R.id.btn_day_sun}) {
                        android.view.View day = a.findViewById(id);
                        assertTrue("Weekday touch width must be at least 48dp", day.getWidth() >= 48 * density);
                        assertTrue("Weekday touch height must be at least 48dp", day.getHeight() >= 48 * density);
                    }
                    AccessibilityNodeInfo initial = a.findViewById(R.id.btn_day_mon).createAccessibilityNodeInfo();
                    assertTrue("Weekday must expose a checkable control", initial.isCheckable());
                    assertTrue("The initial daily schedule must expose checked weekdays", initial.isChecked());
                    initial.recycle();
                    ((android.widget.CompoundButton) a.findViewById(R.id.cb_med_all_days)).setChecked(false);
                    a.findViewById(R.id.btn_day_mon).performClick();
                    a.findViewById(R.id.btn_day_fri).performClick();
                    for (int id : new int[]{R.id.btn_day_mon, R.id.btn_day_fri, R.id.btn_day_tue}) {
                        AccessibilityNodeInfo node = a.findViewById(id).createAccessibilityNodeInfo();
                        assertEquals(id != R.id.btn_day_tue, node.isChecked()); node.recycle();
                    }
                });
                capture(context, "a11y-medicine-days.png");
                screen.onActivity(a -> {
                    ((EditText) a.findViewById(R.id.et_med_name)).setText("Thuốc kiểm thử trợ năng");
                    a.findViewById(R.id.btn_med_save).performClick();
                });
            }
            java.util.List<Medicine> saved = medicines.getAllByUserId(owner);
            assertEquals(1, saved.size());
            assertEquals(17, saved.get(0).getDaysMask());
        } finally {
            for (Medicine medicine : medicines.getAllByUserId(owner)) MedicineAlarmReceiver.cancel(context, medicine);
            users.deleteProfileWithPassword(owner, "test-password");
            new SessionManager(context).clearSession();
        }
    }
    @Test public void healthPickersOpenWithEnterAndKeepTheOriginalValues() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("picker_" + System.nanoTime());
        UserDao users = new UserDao(context); long owner = users.register(user, "test-password");
        new SessionManager(context).saveSession(owner, user.getUsername(), "");
        final String[] original = new String[2];
        try {
            try (ActivityScenario<vn.edu.tdmu.vita.activities.AddHealthActivity> screen = ActivityScenario.launch(vn.edu.tdmu.vita.activities.AddHealthActivity.class)) {
                screen.onActivity(a -> {
                    original[0] = ((EditText) a.findViewById(R.id.et_add_health_date)).getText().toString();
                    original[1] = ((EditText) a.findViewById(R.id.et_health_time)).getText().toString();
                });
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.sp_health_metric_group))
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
                screen.onActivity(a -> {
                    android.view.ViewGroup groups = a.findViewById(R.id.sp_health_metric_group);
                    for (int index = 0; index < groups.getChildCount(); index++) {
                        android.widget.TextView chip = (android.widget.TextView) groups.getChildAt(index);
                        assertTrue("Metric label must fit inside its chip", chip.getWidth() >= chip.getPaint().measureText(chip.getText().toString()) + 32 * a.getResources().getDisplayMetrics().density);
                    }
                });
                capture(context, "a11y-health-groups.png");
                int[] ids = {R.id.et_add_health_date, R.id.et_health_time};
                for (int index = 0; index < ids.length; index++) {
                    final int id = ids[index];
                    androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(id))
                            .perform(androidx.test.espresso.action.ViewActions.scrollTo());
                    screen.onActivity(a -> assertTrue("Picker must receive keyboard focus", a.findViewById(id).requestFocus()));
                    androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(id))
                            .perform(androidx.test.espresso.action.ViewActions.pressKey(android.view.KeyEvent.KEYCODE_ENTER));
                    androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(index == 0 ? android.widget.DatePicker.class : android.widget.TimePicker.class))
                            .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                            .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
                    androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(android.R.id.button2))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                        .perform(androidx.test.espresso.action.ViewActions.click());
                }
                capture(context, "a11y-health-pickers.png");
                screen.onActivity(a -> {
                    assertEquals(original[0], ((EditText) a.findViewById(R.id.et_add_health_date)).getText().toString());
                    assertEquals(original[1], ((EditText) a.findViewById(R.id.et_health_time)).getText().toString());
                    ((EditText) a.findViewById(R.id.et_add_heart_rate)).setText("72");
                    a.findViewById(R.id.btn_add_health_save).performClick();
                });
            }
            java.util.List<HealthRecord> records = new HealthDao(context).getAllByUserId(owner, null);
            assertEquals(1, records.size());
            assertEquals(original[1], records.get(0).getMeasuredTime());
            assertEquals(Integer.valueOf(72), records.get(0).getHeartRate());
        } finally {
            users.deleteProfileWithPassword(owner, "test-password");
            new SessionManager(context).clearSession();
        }
    }
    @Test public void medicineActionsKeepReadableTouchTargets() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("targets_" + System.nanoTime());
        UserDao users = new UserDao(context); long owner = users.register(user, "test-password");
        new SessionManager(context).saveSession(owner, user.getUsername(), "");
        MedicineDao medicines = new MedicineDao(context);
        long medicineId = medicines.insert(new Medicine(0, owner, "Vitamin C", "1 viên sau ăn", "12:00", 127, true));
        // Thông báo lưu từ kịch bản trước có thể vẫn che màn hình tiếp theo.
        android.os.SystemClock.sleep(3000);
        try {
            try (ActivityScenario<vn.edu.tdmu.vita.activities.MainActivity> screen = ActivityScenario.launch(vn.edu.tdmu.vita.activities.MainActivity.class)) {
                screen.onActivity(a -> ((com.google.android.material.bottomnavigation.BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_reminder));
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.tv_med_item_status))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo());
                screen.onActivity(a -> {
                    android.widget.TextView status = a.findViewById(R.id.tv_med_item_status);
                    assertTrue("Dose status must have space for large text", status.getHeight() >= 48 * a.getResources().getDisplayMetrics().density);
                });
                screen.onActivity(a -> {
                    android.view.View subtitle = a.findViewById(R.id.tv_reminder_subtitle);
                    android.view.View inner = (android.view.View) subtitle.getParent();
                    assertTrue("Reminder heading must not clip its subtitle", ((android.view.View) inner.getParent()).getHeight() >= inner.getHeight());
                    if (a.getResources().getConfiguration().fontScale > 1.3f)
                        assertTrue("Dose name needs a readable column at large font", a.findViewById(R.id.tv_dose_name).getWidth() >= 120 * a.getResources().getDisplayMetrics().density);
                    assertEquals("Reminder tabs must have equal heights", a.findViewById(R.id.tab_today_doses).getHeight(), a.findViewById(R.id.tab_medicine_list).getHeight());
                    for (int id : new int[]{R.id.tab_today_doses, R.id.tab_medicine_list}) {
                        android.widget.TextView tab = a.findViewById(id);
                        for (int line = 0; line < tab.getLineCount(); line++) assertEquals(0, tab.getLayout().getEllipsisCount(line));
                    }
                });
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.tv_med_item_status))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast(90)));
                capture(context, "a11y-dose-status.png");
                screen.onActivity(a -> a.findViewById(R.id.tab_medicine_list).performClick());
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.sw_med_active))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo());
                screen.onActivity(a -> {
                    android.view.View active = a.findViewById(R.id.sw_med_active);
                    assertTrue("Reminder switch must have a 48dp touch height", active.getHeight() >= 48 * a.getResources().getDisplayMetrics().density);
                });
                capture(context, "a11y-medicine-switch.png");
                screen.onActivity(a -> a.findViewById(R.id.sw_med_active).performClick());
                assertFalse(medicines.getById(medicineId, owner).isActive());
            }
        } finally {
            MedicineAlarmReceiver.cancel(context, medicines.getById(medicineId, owner));
            users.deleteProfileWithPassword(owner, "test-password");
            new SessionManager(context).clearSession();
        }
    }
    private static void capture(Context context, String name) {
        android.app.Instrumentation instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation();
        instrumentation.waitForIdleSync();
        java.util.concurrent.CountDownLatch frames = new java.util.concurrent.CountDownLatch(1);
        instrumentation.runOnMainSync(() -> android.view.Choreographer.getInstance().postFrameCallback(first ->
                android.view.Choreographer.getInstance().postFrameCallback(second -> frames.countDown())));
        try { assertTrue(frames.await(5, java.util.concurrent.TimeUnit.SECONDS)); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new AssertionError(error); }
        android.graphics.Bitmap image = instrumentation.getUiAutomation().takeScreenshot();
        assertNotNull(image);
        try (java.io.FileOutputStream output = new java.io.FileOutputStream(new java.io.File(context.getExternalFilesDir(null), name))) {
            assertTrue(image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output));
        } catch (java.io.IOException error) { throw new AssertionError(error); }
        finally { image.recycle(); }
    }
}
