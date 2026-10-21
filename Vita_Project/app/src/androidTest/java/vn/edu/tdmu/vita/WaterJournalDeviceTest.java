package vn.edu.tdmu.vita;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.widget.*;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.*;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.receivers.WaterReminderReceiver;
import vn.edu.tdmu.vita.utils.*;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.click;

@RunWith(AndroidJUnit4.class)
public class WaterJournalDeviceTest {
    @Test public void deleteConfirmation_cancelKeepsLog_andConfirmDeletesOnlySelectedLog() {
        WaterDao water = new WaterDao(context);
        String today = DateTimeUtils.getTodayDate();
        water.addWater(owner, today, 250, today + " 08:00:00");
        water.addWater(other, today, 500, today + " 08:00:00");
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a -> WaterJournalDialog.show(a, owner, today, null));
            onView(withText("08:00")).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            onView(withId(android.R.id.button3)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            onView(withId(android.R.id.button2)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            assertEquals(250, water.getDailyTotal(owner, today));
            onView(withId(android.R.id.button3)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            onView(withText(R.string.figma_water_delete_action)).perform(click());
            assertEquals(0, water.getDailyTotal(owner, today));
            assertEquals(500, water.getDailyTotal(other, today));
        }
    }

    @Test public void amountDraftSurvivesRotation_andSaveRefreshesHome() {
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a -> WaterJournalDialog.showAddAmount(a, owner, null));
            onView(withHint(R.string.figma_water_entry_amount_label)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(androidx.test.espresso.action.ViewActions.replaceText("500"), androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
            screen.recreate();
            onView(withHint(R.string.figma_water_entry_amount_label)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).check(androidx.test.espresso.assertion.ViewAssertions.matches(withText("500")));
            onView(withId(android.R.id.button1)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            assertEquals(500, new WaterDao(context).getDailyTotal(owner, DateTimeUtils.getTodayDate()));
            screen.onActivity(a -> assertTrue(((TextView) a.findViewById(R.id.tv_home_water_progress)).getText().toString().contains("500")));
        }
    }

    @Test public void quickAddInsideJournal_canUndoWithoutLosingJournal() {
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a -> WaterJournalDialog.show(a, owner, DateTimeUtils.getTodayDate(), null));
            onView(withText(context.getString(R.string.figma_home_quick_water, 250))).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            assertEquals(250, new WaterDao(context).getDailyTotal(owner, DateTimeUtils.getTodayDate()));
            onView(withId(com.google.android.material.R.id.snackbar_action)).perform(click());
            assertEquals(0, new WaterDao(context).getDailyTotal(owner, DateTimeUtils.getTodayDate()));
            screen.onActivity(a -> assertNotNull(a.getSupportFragmentManager().findFragmentByTag("water_journal")));
        }
    }
    @Test public void journalOrdersLegacyAndFullTimestampsByTime() {
        WaterDao water = new WaterDao(context); String today = DateTimeUtils.getTodayDate();
        water.addWater(owner, today, 250, "09:00");
        water.addWater(owner, today, 350, today + " 08:00:00");
        assertEquals(250, water.getLogsByDate(owner, today).get(0).getAmountMl());
    }
    @Test public void journalLetsUserCorrectAnEntryAfterUndoHasGone() {
        WaterDao water = new WaterDao(context); String today = DateTimeUtils.getTodayDate();
        water.addWater(owner, today, 250, today + " 07:00:00");
        water.addWater(owner, today, 350, today + " 08:00:00");
        water.addWater(other, today, 999, today + " 08:00:00");
        try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(new Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_REMINDER))) {
            onView(withId(R.id.tv_water_progress_text)).perform(androidx.test.espresso.action.ViewActions.scrollTo(),click());
            onView(withText("08:00")).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            java.util.concurrent.atomic.AtomicReference<android.view.View> entryRoot = new java.util.concurrent.atomic.AtomicReference<>();
            screen.onActivity(a -> {
                a.getSupportFragmentManager().executePendingTransactions();
                androidx.fragment.app.DialogFragment entry = (androidx.fragment.app.DialogFragment) a.getSupportFragmentManager().findFragmentByTag("water_entry");
                assertNotNull(entry); assertNotNull(entry.getDialog());
                entryRoot.set(entry.getDialog().getWindow().getDecorView());
            });
            onView(withHint(R.string.figma_water_entry_amount_label)).inRoot(androidx.test.espresso.matcher.RootMatchers.withDecorView(org.hamcrest.Matchers.is(entryRoot.get())))
                    .perform(androidx.test.espresso.action.ViewActions.replaceText("500"));
            onView(withId(android.R.id.button1)).inRoot(androidx.test.espresso.matcher.RootMatchers.withDecorView(org.hamcrest.Matchers.is(entryRoot.get()))).perform(click());
            assertEquals(750, water.getDailyTotal(owner, today));
            assertEquals(999, water.getDailyTotal(other, today));
        }
    }

    private Context context; private long owner, other; private SessionManager session;
    @Before public void setup() {
        context=ApplicationProvider.getApplicationContext(); assertTrue(context.getPackageName().endsWith(".verification"));
        UserDao users=new UserDao(context); owner=create(users); other=create(users);
        session=new SessionManager(context); session.saveSession(owner,users.getUserById(owner).getUsername(),"Test");
    }
    private long create(UserDao users) {
        User user=new User(); user.setUsername("water_journal_"+System.nanoTime());
        long id=users.register(user,"test-password"); assertTrue(id>0); return id;
    }
    @After public void cleanup() {
        WaterReminderReceiver.cancel(context); session.clearSession();
        try(DatabaseHelper helper=new DatabaseHelper(context)) {
            for(long id:new long[]{owner,other}) helper.getWritableDatabase().delete("users","id=?",new String[]{String.valueOf(id)});
        }
    }
    private int id(String name) { return context.getResources().getIdentifier(name,"id",context.getPackageName()); }
    @Test public void undo_deletesOnlyOwnedSelectedLog() {
        WaterDao water=new WaterDao(context); String today=DateTimeUtils.getTodayDate();
        long first=water.addWater(owner,today,250,"07:00");
        long last=water.addWater(owner,today,350,"08:00");
        long foreign=water.addWater(other,today,500,"08:00");
        assertFalse(water.deleteLog(foreign,owner)); assertTrue(water.deleteLog(last,owner));
        assertEquals(250,water.getDailyTotal(owner,today)); assertEquals(first,water.getLogsByDate(owner,today).get(0).getId());
        assertEquals(500,water.getDailyTotal(other,today)); assertFalse(water.deleteLog(last,owner));
    }
    @Test public void quickAddAndUndo_restoresPreviousTotal() {
        UserDao users=new UserDao(context);
        assertTrue(users.updateWaterSettings(owner,2000,"07:00","22:00",350,""));
        WaterDao water=new WaterDao(context); String today=DateTimeUtils.getTodayDate();
        long first=water.addWater(owner,today,250,"07:00");
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(new Intent(context,MainActivity.class)
                .putExtra(MainActivity.EXTRA_TAB,MainActivity.TAB_REMINDER))) {
            onView(withId(R.id.btn_quick_add_water)).perform(androidx.test.espresso.action.ViewActions.scrollTo(),click());
            assertEquals(600,water.getDailyTotal(owner,today));
            onView(withId(com.google.android.material.R.id.snackbar_action)).perform(click());
            assertEquals(250,water.getDailyTotal(owner,today));
            assertEquals(first,water.getLogsByDate(owner,today).get(0).getId());
        }
    }
    @Test public void customTimes_surviveRecreationAndSwitchBackToTwoHours() {
        try(ActivityScenario<EditProfileActivity> screen=ActivityScenario.launch(new Intent(context,EditProfileActivity.class)
                .putExtra(EditProfileActivity.EXTRA_WATER_SETTINGS_ONLY,true))) {
            screen.onActivity(a->a.findViewById(id("rb_water_custom_times")).performClick());
            for(int[] time:new int[][]{{13,45},{9,15},{9,15}}) {
                screen.onActivity(a->a.findViewById(id("btn_add_water_time")).performClick());
                onView(isAssignableFrom(TimePicker.class)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).check((view,error)->{ if (error != null) throw error; ((TimePicker)view).setHour(time[0]); ((TimePicker)view).setMinute(time[1]); });
                onView(withId(android.R.id.button1)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            }
            screen.recreate(); screen.onActivity(a->a.findViewById(R.id.btn_edit_save).performClick());
        }
        UserDao users=new UserDao(context); assertEquals("09:15,13:45",users.getUserById(owner).getWaterTimes());
        try(ActivityScenario<EditProfileActivity> screen=ActivityScenario.launch(new Intent(context,EditProfileActivity.class)
                .putExtra(EditProfileActivity.EXTRA_WATER_SETTINGS_ONLY,true))) {
            screen.onActivity(a->{ a.findViewById(id("rb_water_every_two_hours")).performClick(); a.findViewById(R.id.btn_edit_save).performClick(); });
        }
        assertEquals("",users.getUserById(owner).getWaterTimes());
    }
    @Test public void migration4to5_keepsProfileAndDefaultsToTwoHours() {
        try(SQLiteDatabase db=SQLiteDatabase.create(null)) {
            db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY, username TEXT, password_hash TEXT, salt TEXT, fullname TEXT, birth_year INTEGER, gender TEXT, height_cm REAL, base_weight_kg REAL, water_goal_ml INTEGER, bmi_standard TEXT, water_start TEXT, water_end TEXT, water_amount INTEGER)");
            db.execSQL("INSERT INTO users VALUES(7,'old','hash','salt','Name',1990,'Nam',170,60,1500,'WHO','20:00','06:00',350)");
            DatabaseHelper helper=new DatabaseHelper(context) {
                @Override public SQLiteDatabase getWritableDatabase() { return db; }
                @Override public SQLiteDatabase getReadableDatabase() { return db; }
            };
            helper.onUpgrade(db,4,5); User saved=new UserDao(helper).getUserById(7);
            assertEquals("",saved.getWaterTimes()); assertEquals(1500,saved.getWaterGoalMl());
            assertEquals("20:00",saved.getWaterStart()); assertEquals(350,saved.getWaterAmount()); assertEquals(1990,saved.getBirthYear());
        }
    }
    @Test public void customSettings_rejectOutsideWindowWithoutChangingOtherProfile() {
        UserDao users=new UserDao(context);
        assertTrue(users.updateWaterSettings(owner,1500,"07:00","22:00",350,"21:30,07:15,21:30"));
        assertEquals("07:15,21:30",users.getUserById(owner).getWaterTimes());
        assertFalse(users.updateWaterSettings(owner,1500,"07:00","22:00",350,"06:59"));
        assertEquals("07:15,21:30",users.getUserById(owner).getWaterTimes());
        assertEquals("",users.getUserById(other).getWaterTimes()); assertEquals(2000,users.getUserById(other).getWaterGoalMl());
    }
}
