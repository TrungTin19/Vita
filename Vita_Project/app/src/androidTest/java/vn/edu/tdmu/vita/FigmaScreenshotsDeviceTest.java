package vn.edu.tdmu.vita;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.*;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import vn.edu.tdmu.vita.utils.*;
import static org.junit.Assert.*;

/** Real SQLite fixtures confined to the verification application. */
@RunWith(AndroidJUnit4.class)
public class FigmaScreenshotsDeviceTest {
    private Context context;

    @Test public void dashboardAndStatsTableKeepTheirFullHeadings() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("headings_" + System.nanoTime());
        user.setFullname("Nguyễn Minh An"); user.setHeightCm(165); user.setBaseWeightKg(62);
        UserDao users = new UserDao(context);
        long owner = users.register(user, "test-password"); assertTrue(owner > 0);
        new SessionManager(context).saveSession(owner, user.getUsername(), user.getFullname());
        try {
            assertTrue(new HealthDao(context).insert(new HealthRecord(0, owner, DateTimeUtils.getTodayDate(),
                    null, 118, 78, null, null, null)) > 0);
            assertTrue(new WaterDao(context).addWater(owner, DateTimeUtils.getTodayDate(), 1000,
                    DateTimeUtils.getCurrentTimestamp()) > 0);
            assertTrue(new WaterDao(context).addWater(owner, DateTimeUtils.getTodayDate(), 1250,
                    DateTimeUtils.getCurrentTimestamp()) > 0);
            boolean[] headingsFit = {false, false, false, false};
            try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
                capture("dashboard-heading");
                screen.onActivity(a -> {
                    android.view.View toolbar = a.findViewById(R.id.toolbar_main);
                    android.widget.TextView date = a.findViewById(R.id.tv_main_date);
                    int[] toolbarPosition = new int[2], datePosition = new int[2];
                    toolbar.getLocationOnScreen(toolbarPosition); date.getLocationOnScreen(datePosition);
                    headingsFit[0] = datePosition[1] + date.getHeight() <= toolbarPosition[1] + toolbar.getHeight();
                    headingsFit[0] &= date.getLayout() != null && date.getLayout().getHeight()
                            <= date.getHeight() - date.getCompoundPaddingTop() - date.getCompoundPaddingBottom();
                    for (int id : new int[]{R.id.tv_main_title, R.id.tv_main_date, R.id.btn_main_profile}) {
                        android.view.View child = a.findViewById(id);
                        int[] position = new int[2]; child.getLocationOnScreen(position);
                        headingsFit[0] &= position[1] >= toolbarPosition[1]
                                && position[1] + child.getHeight() <= toolbarPosition[1] + toolbar.getHeight();
                    }
                });
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.tv_home_water_progress))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo());
                capture("dashboard-water-total");
                screen.onActivity(a -> {
                    headingsFit[2] = ((android.widget.TextView) a.findViewById(R.id.tv_home_water_progress)).getLineCount() == 1;
                });
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_home_custom_water))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo());
                capture("dashboard-water-actions");
                screen.onActivity(a -> {
                    android.widget.TextView quick = a.findViewById(R.id.btn_home_add_water);
                    android.widget.TextView custom = a.findViewById(R.id.btn_home_custom_water);
                    headingsFit[3] = quick.getLineCount() == 1 && custom.getLineCount() == 1
                            && quick.getHeight() >= 48 * a.getResources().getDisplayMetrics().density
                            && custom.getHeight() >= 48 * a.getResources().getDisplayMetrics().density;
                    ((BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_stats);
                });
                screen.onActivity(a -> ((android.widget.Spinner) a.findViewById(R.id.sp_stats_metric)).setSelection(1));
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_stats_data))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo(), androidx.test.espresso.action.ViewActions.click());
                capture("stats-table-heading");
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.toolbar_stats_data))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                        .check((view, error) -> {
                            if (error != null) throw error;
                            android.view.ViewGroup toolbar = (android.view.ViewGroup) view;
                            for (int i = 0; i < toolbar.getChildCount(); i++) {
                                if (!(toolbar.getChildAt(i) instanceof android.widget.TextView)) continue;
                                android.widget.TextView title = (android.widget.TextView) toolbar.getChildAt(i);
                                if (title.getText().length() == 0) continue;
                                headingsFit[1] = title.getLayout() != null;
                                headingsFit[1] &= title.getLayout() != null && title.getLayout().getHeight()
                                        <= title.getHeight() - title.getCompoundPaddingTop() - title.getCompoundPaddingBottom();
                                for (int line = 0; line < title.getLineCount(); line++)
                                    headingsFit[1] &= title.getLayout().getEllipsisCount(line) == 0;
                            }
                        });
                assertTrue("Home date must fit inside its toolbar", headingsFit[0]);
                assertTrue("Stats table title must not be ellipsized", headingsFit[1]);
                assertTrue("Home water total must stay on one line", headingsFit[2]);
                assertTrue("Home water actions must stay readable", headingsFit[3]);
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withContentDescription(R.string.a11y_navigate_back))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(androidx.test.espresso.action.ViewActions.click());
                screen.onActivity(a -> ((android.widget.Spinner) a.findViewById(R.id.sp_stats_metric)).setSelection(4));
                screen.onActivity(a -> a.findViewById(R.id.btn_range_30).performClick());
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_stats_data))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo(), androidx.test.espresso.action.ViewActions.click());
                capture("water-table-labels");
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.tv_stats_data_period))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.withText(org.hamcrest.Matchers.containsString("30 ngày"))));
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText(org.hamcrest.Matchers.containsString("2250 ml")))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(org.hamcrest.Matchers.not(
                                androidx.test.espresso.matcher.ViewMatchers.withText(org.hamcrest.Matchers.containsString("· ·")))));
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText(org.hamcrest.Matchers.containsString("2250 ml")))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                        .check((view, error) -> {
                            if (error != null) throw error;
                            android.text.Spanned text = (android.text.Spanned) ((android.widget.TextView) view).getText();
                            android.text.style.AbsoluteSizeSpan[] spans = text.getSpans(0, text.length(), android.text.style.AbsoluteSizeSpan.class);
                            assertEquals(1, spans.length);
                            float pixels = spans[0].getDip() ? spans[0].getSize() * view.getResources().getDisplayMetrics().density : spans[0].getSize();
                            if (view.getResources().getConfiguration().fontScale > 1.3f)
                                assertTrue("Table dates must follow the larger system font", pixels > 12 * view.getResources().getDisplayMetrics().density);
                        });
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withContentDescription(R.string.a11y_navigate_back))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(androidx.test.espresso.action.ViewActions.click());
            }
        } finally {
            users.deleteProfileWithPassword(owner, "test-password");
            new SessionManager(context).clearSession();
        }
    }

    @Test public void emptyStatesKeepTheirEntryActionsAndDoNotCreateData() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("empty_ui_" + System.nanoTime());
        UserDao users = new UserDao(context);
        long owner = users.register(user, "test-password"); assertTrue(owner > 0);
        new SessionManager(context).saveSession(owner, user.getUsername(), "");
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a -> ((BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_health));
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.layout_health_empty))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            capture("verified-health-empty");
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.fab_add_health))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo(), androidx.test.espresso.action.ViewActions.click());
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.et_add_health_date))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            androidx.test.espresso.Espresso.closeSoftKeyboard();
            androidx.test.espresso.Espresso.pressBack();
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.bottom_nav_main))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            screen.onActivity(a -> ((BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_stats));
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.layout_stats_empty))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            capture("verified-stats-empty");
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_stats_first_measurement))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo(), androidx.test.espresso.action.ViewActions.click());
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.et_add_weight))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            androidx.test.espresso.Espresso.closeSoftKeyboard();
            androidx.test.espresso.Espresso.pressBack();
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.bottom_nav_main))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            screen.onActivity(a -> WaterJournalDialog.show(a, owner, DateTimeUtils.getTodayDate(), null));
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText(R.string.water_journal_empty))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            capture("verified-water-empty");
            assertTrue(new WaterDao(context).getLogsByDate(owner, DateTimeUtils.getTodayDate()).isEmpty());
            assertTrue(new HealthDao(context).getAllByUserId(owner, null).isEmpty());
        } finally {
            users.deleteProfileWithPassword(owner, "test-password");
            new SessionManager(context).clearSession();
        }
    }

    @Test public void captureMedicineHistoryAndWaterJournalLargeText() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("dialog_ui_" + System.nanoTime());
        UserDao users = new UserDao(context); long owner = users.register(user, "test-password"); assertTrue(owner > 0);
        new SessionManager(context).saveSession(owner, user.getUsername(), "");
        MedicineDao medicines = new MedicineDao(context);
        long id = medicines.insert(new Medicine(0, owner, "Vitamin tổng hợp dùng sau bữa ăn buổi sáng", "1 viên sau bữa sáng", "07:00,20:00", 127, true));
        Medicine medicine = medicines.getById(id, owner);
        medicines.logMedicineStatus(id, owner, DateTimeUtils.getTodayDate(), "20:00", "TAKEN", DateTimeUtils.getCurrentTimestamp());
        WaterDao water = new WaterDao(context);
        water.addWater(owner, DateTimeUtils.getTodayDate(), 1000, DateTimeUtils.getTodayDate() + " 08:00:00");
        water.addWater(owner, DateTimeUtils.getTodayDate(), 1250, DateTimeUtils.getTodayDate() + " 12:00:00");
        try {
            try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
                screen.onActivity(a -> MedicineDoseDialogs.history(a, owner, medicine, null));
                assertPageTitleReadable();
                capture("medicine-history-large");
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText(R.string.figma_edit_schedule))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo());
                capture("medicine-history-action-large");
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withContentDescription(R.string.a11y_navigate_back))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(androidx.test.espresso.action.ViewActions.click());
                screen.onActivity(a -> MedicineDoseDialogs.edit(a, owner, medicine, DateTimeUtils.getTodayDate(), "07:00", null));
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(android.R.id.button2))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(androidx.test.espresso.action.ViewActions.scrollTo());
                capture("medicine-dose-dialog-large"); dialogButton(android.R.id.button2);
                screen.onActivity(a -> WaterJournalDialog.show(a, owner, DateTimeUtils.getTodayDate(), null));
                assertPageTitleReadable();
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText(String.format(java.util.Locale.getDefault(), "%,d", 2250)))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                        .check((view, error) -> { assertNull(error); assertEquals("Water total must stay on one line", 1, ((android.widget.TextView) view).getLineCount()); });
                capture("water-journal-large");
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText(R.string.figma_home_custom))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                        .check((view, error) -> { assertNull(error); assertEquals("Custom amount action must stay readable", 1, ((android.widget.TextView) view).getLineCount()); });
                capture("water-journal-actions-large");
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withContentDescription(R.string.a11y_navigate_back))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(androidx.test.espresso.action.ViewActions.click());
            }
            assertNull(medicines.getLogForDose(id, owner, DateTimeUtils.getTodayDate(), "07:00"));
            assertEquals("TAKEN", medicines.getLogForDose(id, owner, DateTimeUtils.getTodayDate(), "20:00").getStatus());
            assertEquals(2250, water.getDailyTotal(owner, DateTimeUtils.getTodayDate()));
        } finally {
            vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver.cancel(context, medicine);
            vn.edu.tdmu.vita.receivers.WaterReminderReceiver.cancel(context);
            users.deleteProfileWithPassword(owner, "test-password"); new SessionManager(context).clearSession();
        }
    }

    private void assertPageTitleReadable() {
        androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.tv_page_title))
                .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                .check((view, error) -> {
                    assertNull(error);
                    android.widget.TextView title = (android.widget.TextView) view;
                    for (int line = 0; line < title.getLineCount(); line++) assertEquals(0, title.getLayout().getEllipsisCount(line));
                });
    }

    @Test public void captureRegistrationLargeTextAndValidation() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        String username = "register_ui_" + System.nanoTime();
        try (ActivityScenario<RegisterActivity> screen = ActivityScenario.launch(RegisterActivity.class)) {
            capture("register-account-large");
            screen.onActivity(a -> {
                ((android.widget.EditText) a.findViewById(R.id.et_reg_username)).setText(username);
                ((android.widget.EditText) a.findViewById(R.id.et_reg_password)).setText("test-password");
                ((android.widget.EditText) a.findViewById(R.id.et_reg_confirm)).setText("test-password");
                a.findViewById(R.id.btn_reg_submit).performClick();
            });
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.et_reg_fullname))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            screen.onActivity(a -> {
                android.widget.TextView title = a.findViewById(R.id.title_register);
                for (int line = 0; line < title.getLineCount(); line++) assertEquals(0, title.getLayout().getEllipsisCount(line));
                android.widget.TextView step = a.findViewById(R.id.tv_register_step);
                assertEquals("Step indicator should stay on one line", 1, step.getLineCount());
            });
            capture("register-profile-large");
            screen.onActivity(a -> ((android.widget.EditText) a.findViewById(R.id.et_reg_height)).setText("20"));
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.et_reg_weight))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo(), androidx.test.espresso.action.ViewActions.click(),
                            androidx.test.espresso.action.ViewActions.replaceText("2000"));
            assertKeyboardVisible(screen);
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_reg_submit))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast(90)));
            capture("register-keyboard-large");
            androidx.test.espresso.Espresso.closeSoftKeyboard();
            screen.onActivity(a -> {
                a.findViewById(R.id.btn_reg_submit).performClick();
                assertNotNull(((com.google.android.material.textfield.TextInputLayout) a.findViewById(R.id.til_reg_height)).getError());
                assertNotNull(((com.google.android.material.textfield.TextInputLayout) a.findViewById(R.id.til_reg_weight)).getError());
            });
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.et_reg_height))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo());
            screen.onActivity(a -> {
                android.widget.EditText weight = a.findViewById(R.id.et_reg_weight);
                assertTrue("Weight value must fit beside its unit and error icon", weight.getPaint().measureText(weight.getText().toString()) <= weight.getWidth() - weight.getCompoundPaddingLeft() - weight.getCompoundPaddingRight());
            });
            capture("register-validation-large");
            screen.onActivity(a -> {
                a.getOnBackPressedDispatcher().onBackPressed();
                assertEquals(android.view.View.VISIBLE, a.findViewById(R.id.group_register_account).getVisibility());
                assertEquals(username, ((android.widget.EditText) a.findViewById(R.id.et_reg_username)).getText().toString());
                a.findViewById(R.id.btn_reg_submit).performClick();
                assertEquals("20", ((android.widget.EditText) a.findViewById(R.id.et_reg_height)).getText().toString());
                assertEquals("2000", ((android.widget.EditText) a.findViewById(R.id.et_reg_weight)).getText().toString());
            });
        }
    }

    private void assertKeyboardVisible(ActivityScenario<? extends android.app.Activity> screen) {
        java.util.concurrent.atomic.AtomicBoolean visible = new java.util.concurrent.atomic.AtomicBoolean();
        java.util.concurrent.atomic.AtomicInteger height = new java.util.concurrent.atomic.AtomicInteger();
        int previousHeight = -1, stableSamples = 0;
        for (int attempt = 0; attempt < 50 && stableSamples < 3; attempt++) {
            screen.onActivity(a -> {
                androidx.core.view.WindowInsetsCompat insets = androidx.core.view.ViewCompat.getRootWindowInsets(a.getWindow().getDecorView());
                visible.set(insets != null && insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()));
                height.set(a.findViewById(android.R.id.content).getHeight());
            });
            stableSamples = visible.get() && height.get() > 0 && height.get() == previousHeight ? stableSamples + 1 : 0;
            previousHeight = height.get();
            if (stableSamples < 3) android.os.SystemClock.sleep(100);
        }
        assertTrue("The keyboard and resized viewport must be ready", visible.get() && stableSamples == 3);
    }

    @Test public void captureWaterSettingsAndProfileEditing() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("water_ui_" + System.nanoTime());
        user.setFullname("Nguyễn Thị Hoàng Minh Phương"); user.setBirthYear(1988);
        user.setHeightCm(165); user.setBaseWeightKg(62.4f);
        UserDao users = new UserDao(context); long owner = users.register(user, "test-password");
        assertTrue(owner > 0);
        assertTrue(users.updateWaterSettings(owner, 2000, "07:00", "22:00", 250, "08:00,12:00,18:00"));
        new SessionManager(context).saveSession(owner, user.getUsername(), user.getFullname());
        try {
            try (ActivityScenario<EditProfileActivity> screen = ActivityScenario.launch(EditProfileActivity.class)) {
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.et_edit_fullname))
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
                screen.onActivity(a -> {
                    android.widget.TextView title = a.findViewById(R.id.title_edit_profile);
                    for (int line = 0; line < title.getLineCount(); line++) assertEquals(0, title.getLayout().getEllipsisCount(line));
                });
                capture("edit-profile-large");
            }
            Intent water = new Intent(context, EditProfileActivity.class).putExtra(EditProfileActivity.EXTRA_WATER_SETTINGS_ONLY, true);
            try (ActivityScenario<EditProfileActivity> screen = ActivityScenario.launch(water)) {
                capture("water-settings-top-large");
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_add_water_time))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo());
                screen.onActivity(a -> {
                    com.google.android.material.chip.ChipGroup group = a.findViewById(R.id.chips_water_times);
                    com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) group.getChildAt(2);
                    com.google.android.material.chip.ChipDrawable drawable = (com.google.android.material.chip.ChipDrawable) chip.getChipDrawable();
                    assertTrue(drawable.getCloseIconStartPadding() + drawable.getCloseIconSize() + drawable.getCloseIconEndPadding() >= 48 * a.getResources().getDisplayMetrics().density);
                });
                screen.onActivity(a -> {
                    android.view.View add = a.findViewById(R.id.btn_add_water_time);
                    add.setFocusableInTouchMode(true);
                    add.requestFocus();
                });
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_add_water_time))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast(90)));
                capture("water-settings-custom-large");
                screen.onActivity(a -> {
                    com.google.android.material.chip.ChipGroup group = a.findViewById(R.id.chips_water_times);
                    ((com.google.android.material.chip.Chip) group.getChildAt(2)).performCloseIconClick();
                    assertEquals(2, group.getChildCount());
                });
                screen.onActivity(a -> a.findViewById(R.id.rb_water_every_two_hours).performClick());
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.tv_water_schedule_preview))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo());
                capture("water-settings-preview-large");
                screen.onActivity(a -> a.findViewById(R.id.rb_water_custom_times).performClick());
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.et_edit_water_amount))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo(), androidx.test.espresso.action.ViewActions.click(),
                                androidx.test.espresso.action.ViewActions.replaceText("300"));
                assertKeyboardVisible(screen);
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_edit_save))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast(90)));
                capture("water-settings-keyboard-large");
                androidx.test.espresso.Espresso.closeSoftKeyboard();
                screen.onActivity(a -> a.findViewById(R.id.btn_edit_save).performClick());
            }
            User saved = users.getUserById(owner);
            assertEquals(300, saved.getWaterAmount());
            assertEquals("08:00,12:00", saved.getWaterTimes());
            assertEquals(user.getFullname(), saved.getFullname());
        } finally {
            vn.edu.tdmu.vita.receivers.WaterReminderReceiver.cancel(context);
            users.deleteProfileWithPassword(owner, "test-password");
            new SessionManager(context).clearSession();
        }
    }

    @Test public void captureAuditFollowupWithLongProfile() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("followup_" + System.nanoTime());
        user.setFullname("Nguyễn Thị Hoàng Minh Phương"); user.setBirthYear(1988);
        user.setHeightCm(165); user.setBaseWeightKg(62.4f);
        UserDao users = new UserDao(context);
        long owner = users.register(user, "test-password");
        new SessionManager(context).saveSession(owner, user.getUsername(), user.getFullname());
        HealthRecord record = new HealthRecord(0, owner, DateTimeUtils.getTodayDate(), 62.4f, 118, 78, 72, 95f,
                "Ghi chú dài: đo buổi sáng sau khi nghỉ ngơi. Theo dõi các lần đo trong nhật ký và giữ nguyên thông tin thực tế đã nhập.");
        record.setMeasuredTime("07:30");
        long recordId = new HealthDao(context).insert(record);
        try {
            try (ActivityScenario<ProfileActivity> screen = ActivityScenario.launch(ProfileActivity.class)) {
                capture("followup-profile-long");
                screen.onActivity(a -> {
                    android.widget.TextView name = a.findViewById(R.id.tv_profile_fullname);
                    assertEquals(user.getFullname(), name.getText().toString());
                    assertNotNull(name.getLayout());
                    for (int line = 0; line < name.getLineCount(); line++) assertEquals(0, name.getLayout().getEllipsisCount(line));
                });
                screen.onActivity(a -> {
                    android.view.View parent = a.findViewById(R.id.group_profile_account_actions);
                    while (!(parent instanceof android.widget.ScrollView)) parent = (android.view.View) parent.getParent();
                    ((android.widget.ScrollView) parent).fullScroll(android.view.View.FOCUS_DOWN);
                });
                capture("followup-profile-actions");
            }
            try (ActivityScenario<BmiActivity> screen = ActivityScenario.launch(BmiActivity.class)) {
                capture("followup-bmi");
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.tv_profile_healthy_weight))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast(90)));
                capture("followup-bmi-range");
                screen.onActivity(a -> a.findViewById(R.id.rb_bmi_who).performClick());
                capture("followup-bmi-who");
            }
            try (ActivityScenario<DetailHealthActivity> screen = ActivityScenario.launch(new Intent(context, DetailHealthActivity.class)
                    .putExtra(DetailHealthActivity.EXTRA_RECORD_ID, recordId))) {
                capture("followup-detail");
                screen.onActivity(a -> {
                    assertEquals("Blood pressure must remain readable on one line for this fixture", 1,
                            ((android.widget.TextView) a.findViewById(R.id.tv_detail_bp_value)).getLineCount());
                    android.widget.TextView title = a.findViewById(R.id.tv_page_title);
                    assertEquals(a.getString(R.string.figma_health_detail_title), title.getText().toString());
                    for (int line = 0; line < title.getLineCount(); line++)
                        assertEquals("Detail heading must not be ellipsized", 0, title.getLayout().getEllipsisCount(line));
                });
                screen.onActivity(a -> a.findViewById(R.id.tv_detail_bp_status).performClick());
                capture("followup-detail-advice");
            }
            try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
                screen.onActivity(a -> ((BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_stats));
                screen.onActivity(a -> ((android.widget.Spinner) a.findViewById(R.id.sp_stats_metric)).setSelection(1));
                capture("followup-stats-bp");
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.chart_bp))
                        .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                        .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast(90)));
                capture("followup-stats-chart");
            }
        } finally {
            users.deleteProfileWithPassword(owner, "test-password");
            new SessionManager(context).clearSession();
        }
    }

    @Test public void captureLoginWithRealKeyboardAndValidation() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        try (ActivityScenario<LoginActivity> screen = ActivityScenario.launch(LoginActivity.class)) {
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.title_login))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            screen.onActivity(a -> {
                android.widget.TextView title = a.findViewById(R.id.title_login);
                for (int line = 0; line < title.getLineCount(); line++) assertEquals(0, title.getLayout().getEllipsisCount(line));
            });
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.et_login_username))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo(), androidx.test.espresso.action.ViewActions.click(), androidx.test.espresso.action.ViewActions.typeText("nguoi_dung_kiem_tra"));
            assertKeyboardVisible(screen);
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(R.id.btn_login_submit))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo())
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast(90)));
            capture("verified-login-keyboard");
            androidx.test.espresso.Espresso.closeSoftKeyboard();
            screen.onActivity(a -> {
                a.findViewById(R.id.btn_login_submit).performClick();
                assertNotNull(((com.google.android.material.textfield.TextInputLayout) a.findViewById(R.id.til_login_password)).getError());
                assertEquals("nguoi_dung_kiem_tra", ((android.widget.EditText) a.findViewById(R.id.et_login_username)).getText().toString());
            });
            capture("verified-login-validation");
        }
    }

    private void dialogButton(int id) {
        androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(id))
                .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                .perform(androidx.test.espresso.action.ViewActions.click());
    }

    private void capture(String name) throws Exception {
        android.app.Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.waitForIdleSync();
        instrumentation.getUiAutomation().waitForIdle(250, 5000);
        java.util.concurrent.CountDownLatch frames = new java.util.concurrent.CountDownLatch(1);
        instrumentation.runOnMainSync(() -> android.view.Choreographer.getInstance().postFrameCallback(first ->
                android.view.Choreographer.getInstance().postFrameCallback(second -> frames.countDown())));
        assertTrue("Window did not produce capture frames", frames.await(5, java.util.concurrent.TimeUnit.SECONDS));
        Bitmap bitmap = instrumentation.getUiAutomation().takeScreenshot();
        assertNotNull(bitmap);
        try (FileOutputStream out = new FileOutputStream(new File(context.getExternalFilesDir(null), "figma-" + name + ".png"))) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out));
        } finally { bitmap.recycle(); }
    }

    @Test public void captureMainFlows() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User();
        user.setUsername("figma_" + System.nanoTime()); user.setFullname("An");
        user.setBirthYear(2000); user.setHeightCm(165); user.setBaseWeightKg(62.4f);
        UserDao users = new UserDao(context);
        long owner = users.register(user, "test-password"); assertTrue(owner > 0);
        new SessionManager(context).saveSession(owner, user.getUsername(), "An");
        HealthDao health = new HealthDao(context);
        HealthRecord record = new HealthRecord(0, owner, DateTimeUtils.getTodayDate(), 62.4f, 118, 78, 72, 95f, "Đo buổi sáng");
        record.setMeasuredTime("07:30");
        long recordId = health.insert(record);
        MedicineDao medicines = new MedicineDao(context);
        long medicineId = medicines.insert(new Medicine(0, owner, "Vitamin C", "1 viên sau ăn", "07:00,19:00", 127, true));
        Medicine medicine = medicines.getById(medicineId, owner);
        new WaterDao(context).addWater(owner, DateTimeUtils.getTodayDate(), 1250, DateTimeUtils.getTodayDate() + " 09:15:00");
        try {
            try (ActivityScenario<SplashActivity> screen = ActivityScenario.launch(SplashActivity.class)) {
                screen.onActivity(a -> { a.findViewById(R.id.ll_splash_brand).animate().cancel(); a.findViewById(R.id.ll_splash_brand).setAlpha(1); });
                capture("splash");
            }
            try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
                capture("home");
                int[] tabs = {R.id.nav_health, R.id.nav_reminder, R.id.nav_stats};
                String[] names = {"health", "reminders", "stats"};
                for (int i = 0; i < tabs.length; i++) {
                    final int tab = tabs[i];
                    screen.onActivity(a -> ((BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(tab));
                    int visible = tab == R.id.nav_health ? R.id.fab_add_health : tab == R.id.nav_reminder ? R.id.tab_today_doses : R.id.sp_stats_metric;
                    androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(visible))
                            .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
                    capture(names[i]);
                    if (tab == R.id.nav_reminder) {
                        screen.onActivity(a -> a.findViewById(R.id.tab_medicine_list).performClick());
                        capture("medicine-list");
                        screen.onActivity(a -> MedicineDoseDialogs.history(a, owner, medicine, null));
                        capture("medicine-history");
                        androidx.test.espresso.Espresso.pressBack();
                        screen.onActivity(a -> MedicineDoseDialogs.edit(a, owner, medicine, DateTimeUtils.getTodayDate(), "19:00", null));
                        capture("dose-dialog"); dialogButton(android.R.id.button2);
                        screen.onActivity(a -> MedicineDoseDialogs.delete(a, owner, medicine, null));
                        capture("delete-medicine-dialog"); dialogButton(android.R.id.button2);
                        screen.onActivity(a -> a.findViewById(R.id.tab_today_doses).performClick());
                    }
                }
                for (int metric = 0; metric < 5; metric++) {
                    final int selected = metric;
                    screen.onActivity(a -> ((android.widget.Spinner) a.findViewById(R.id.sp_stats_metric)).setSelection(selected));
                    capture("stats-metric-" + metric);
                }
                screen.onActivity(a -> a.findViewById(R.id.btn_range_30).performClick());
                capture("stats-30-days");
                screen.onActivity(a -> a.findViewById(R.id.btn_stats_data).performClick());
                capture("stats-data");
            }
            Class<?>[] screens = {ProfileActivity.class, BmiActivity.class, EditProfileActivity.class, AddHealthActivity.class, AddMedicineActivity.class};
            for (Class<?> screen : screens) {
                try (ActivityScenario<?> ignored = ActivityScenario.launch(new Intent(context, screen))) {
                    int content = screen == ProfileActivity.class ? R.id.tv_profile_fullname
                            : screen == BmiActivity.class ? R.id.tv_profile_bmi_value
                            : screen == EditProfileActivity.class ? R.id.et_edit_fullname
                            : screen == AddHealthActivity.class ? R.id.et_add_health_date : R.id.et_med_name;
                    androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withId(content))
                            .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
                    capture(screen.getSimpleName());
                }
            }
            try (ActivityScenario<DetailHealthActivity> ignored = ActivityScenario.launch(new Intent(context, DetailHealthActivity.class).putExtra(DetailHealthActivity.EXTRA_RECORD_ID, recordId))) { capture("health-detail"); }
            try (ActivityScenario<EditProfileActivity> screen = ActivityScenario.launch(new Intent(context, EditProfileActivity.class).putExtra(EditProfileActivity.EXTRA_WATER_SETTINGS_ONLY, true))) {
                capture("water-settings");
                screen.onActivity(a -> a.findViewById(R.id.rb_water_custom_times).performClick());
                capture("water-settings-custom");
                screen.onActivity(a -> a.findViewById(R.id.btn_edit_save).performClick());
                capture("water-settings-error");
            }
            try (ActivityScenario<ProfileActivity> screen = ActivityScenario.launch(ProfileActivity.class)) {
                screen.onActivity(a -> a.findViewById(R.id.btn_profile_logout).performClick());
                capture("logout-dialog");
                dialogButton(android.R.id.button2);
                screen.onActivity(a -> a.findViewById(R.id.btn_profile_delete).performClick());
                capture("delete-profile-dialog");
            }
            try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
                screen.onActivity(a -> WaterJournalDialog.show(a, owner, DateTimeUtils.getTodayDate(), () -> {}));
                capture("water-journal");
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText("09:15"))
                        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(androidx.test.espresso.action.ViewActions.click());
                capture("water-edit"); dialogButton(android.R.id.button3);
                capture("delete-water-dialog"); dialogButton(android.R.id.button2);
            }
            try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
                screen.onActivity(a -> WaterJournalDialog.showAddAmount(a, owner, () -> {}));
                capture("water-add");
            }
            health.delete(recordId, owner);
            for (WaterLog log : new WaterDao(context).getLogsByDate(owner, DateTimeUtils.getTodayDate())) new WaterDao(context).deleteLog(log.getId(), owner);
            try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
                screen.onActivity(a -> ((BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_health));
                capture("health-empty");
                screen.onActivity(a -> ((BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_stats));
                capture("stats-empty");
                screen.onActivity(a -> WaterJournalDialog.show(a, owner, DateTimeUtils.getTodayDate(), null));
                capture("water-empty");
            }
            new SessionManager(context).clearSession();
            try (ActivityScenario<LoginActivity> screen = ActivityScenario.launch(LoginActivity.class)) {
                capture("login");
                screen.onActivity(a -> {
                    android.widget.EditText field = a.findViewById(R.id.et_login_username); field.requestFocus();
                    ((android.view.inputmethod.InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(field, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                });
                capture("login-keyboard"); androidx.test.espresso.Espresso.closeSoftKeyboard();
                screen.onActivity(a -> {
                    ((android.widget.EditText) a.findViewById(R.id.et_login_username)).setText(user.getUsername());
                    ((android.widget.EditText) a.findViewById(R.id.et_login_password)).setText("wrong-password");
                    a.findViewById(R.id.btn_login_submit).performClick();
                });
                capture("login-error");
            }
            try (ActivityScenario<RegisterActivity> screen = ActivityScenario.launch(RegisterActivity.class)) {
                capture("register-account");
                screen.onActivity(a -> {
                    ((android.widget.EditText) a.findViewById(R.id.et_reg_username)).setText("new_figma_account");
                    ((android.widget.EditText) a.findViewById(R.id.et_reg_password)).setText("test-password");
                    ((android.widget.EditText) a.findViewById(R.id.et_reg_confirm)).setText("test-password");
                    a.findViewById(R.id.btn_reg_submit).performClick();
                });
                capture("register-profile");
            }
        } finally {
            users.deleteProfileWithPassword(owner, "test-password");
            new SessionManager(context).clearSession();
        }
    }
}
