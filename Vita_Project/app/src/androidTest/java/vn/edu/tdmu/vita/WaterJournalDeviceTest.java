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
        assertTrue(users.updateWaterSettings(owner,2000,"07:00","22:00",350,false,""));
        WaterDao water=new WaterDao(context); String today=DateTimeUtils.getTodayDate();
        long first=water.addWater(owner,today,250,"07:00");
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(new Intent(context,MainActivity.class)
                .putExtra(MainActivity.EXTRA_TAB,MainActivity.TAB_REMINDER))) {
            onView(withId(R.id.btn_quick_add_water)).perform(click());
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
                onView(isAssignableFrom(TimePicker.class)).check((view,error)->{ ((TimePicker)view).setHour(time[0]); ((TimePicker)view).setMinute(time[1]); });
                onView(withId(android.R.id.button1)).perform(click());
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
            db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY, username TEXT, password_hash TEXT, salt TEXT, fullname TEXT, birth_year INTEGER, gender TEXT, height_cm REAL, base_weight_kg REAL, water_goal_ml INTEGER, bmi_standard TEXT, water_start TEXT, water_end TEXT, water_amount INTEGER, weather_adjustment INTEGER)");
            db.execSQL("INSERT INTO users VALUES(7,'old','hash','salt','Name',1990,'Nam',170,60,1500,'WHO','20:00','06:00',350,1)");
            DatabaseHelper helper=new DatabaseHelper(context) {
                @Override public SQLiteDatabase getWritableDatabase() { return db; }
                @Override public SQLiteDatabase getReadableDatabase() { return db; }
            };
            helper.onUpgrade(db,4,5); User saved=new UserDao(helper).getUserById(7);
            assertEquals("",saved.getWaterTimes()); assertEquals(1500,saved.getWaterGoalMl());
            assertEquals("20:00",saved.getWaterStart()); assertEquals(350,saved.getWaterAmount()); assertEquals(1990,saved.getBirthYear());
            assertTrue(saved.isWeatherAdjustmentEnabled());
        }
    }
    @Test public void customSettings_rejectOutsideWindowWithoutChangingOtherProfile() {
        UserDao users=new UserDao(context);
        assertTrue(users.updateWaterSettings(owner,1500,"07:00","22:00",350,true,"21:30,07:15,21:30"));
        assertEquals("07:15,21:30",users.getUserById(owner).getWaterTimes());
        assertFalse(users.updateWaterSettings(owner,1500,"07:00","22:00",350,true,"06:59"));
        assertEquals("07:15,21:30",users.getUserById(owner).getWaterTimes());
        assertEquals("",users.getUserById(other).getWaterTimes()); assertEquals(2000,users.getUserById(other).getWaterGoalMl());
    }
}
