package vn.edu.tdmu.vita;

import android.app.*;
import android.content.*;
import android.view.View;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import com.google.android.material.textfield.TextInputLayout;
import vn.edu.tdmu.vita.activities.EditProfileActivity;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import vn.edu.tdmu.vita.receivers.*;
import vn.edu.tdmu.vita.utils.*;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.*;

@RunWith(AndroidJUnit4.class)
public class ProfileDeletionScreenDeviceTest {
    private Context context; private UserDao users; private long owner, other;
    private SessionManager session;
    @Before public void setup() {
        context=ApplicationProvider.getApplicationContext(); assertTrue(context.getPackageName().endsWith(".verification"));
        users=new UserDao(context); owner=create("owner"); other=create("other");
        session=new SessionManager(context); session.saveSession(owner,users.getUserById(owner).getUsername(),"Owner");
        context.getSystemService(NotificationManager.class).cancelAll();
    }
    private long create(String name) {
        User user=new User(); user.setUsername(name+"_"+System.nanoTime()); user.setHeightCm(170); user.setBaseWeightKg(60);
        long id=users.register(user,"test-password"); assertTrue(id>0); return id;
    }
    @After public void cleanup() {
        for(long id:new long[]{owner,other}) {
            for(Medicine med:new MedicineDao(context).getAllByUserId(id)) MedicineAlarmReceiver.cancel(context,med);
            try(DatabaseHelper helper=new DatabaseHelper(context)) {helper.getWritableDatabase().delete("users","id=?",new String[]{String.valueOf(id)});}
        }
        WaterReminderReceiver.cancel(context); session.clearSession(); context.getSystemService(NotificationManager.class).cancelAll();
    }
    private int id(String name) { return context.getResources().getIdentifier(name,"id",context.getPackageName()); }
    private void openDelete(ActivityScenario<EditProfileActivity> screen) {
        screen.onActivity(activity->{
            View button=activity.findViewById(id("btn_edit_delete"));
            assertNotNull("Profile deletion action is missing",button); button.performClick();
        });
    }
    private void confirm(String password) {
        onView(withId(id("et_delete_profile_password"))).perform(replaceText(password),closeSoftKeyboard());
        onView(withId(android.R.id.button1)).perform(click());
    }
    @Test public void cancellingDeletion_keepsProfileAndSession() {
        try(ActivityScenario<EditProfileActivity> screen=ActivityScenario.launch(EditProfileActivity.class)) {
            openDelete(screen); onView(withId(android.R.id.button2)).perform(click());
            assertNotNull(users.getUserById(owner)); assertTrue(session.isLoggedIn());
        }
    }
    @Test public void incorrectPassword_keepsProfileAndShowsInlineError() {
        try(ActivityScenario<EditProfileActivity> screen=ActivityScenario.launch(EditProfileActivity.class)) {
            openDelete(screen); confirm("incorrect");
            onView(withId(id("til_delete_profile_password"))).check((view,error)->assertNotNull(((TextInputLayout)view).getError()));
            assertNotNull(users.getUserById(owner)); assertTrue(session.isLoggedIn());
            onView(withId(android.R.id.button2)).perform(click());
        }
    }
    @Test public void confirmedDeletion_cancelsOwnedRemindersAndEndsSession() {
        MedicineDao medicines=new MedicineDao(context);
        Medicine med=new Medicine(0,owner,"Test","1","07:00,19:00",127,true); med.setId(medicines.insert(med));
        MedicineAlarmReceiver.schedule(context,med); WaterReminderReceiver.schedule(context);
        assertTrue("Enable notifications for this test",NotificationHelper.canNotify(context));
        NotificationHelper.showMedicineNotification(context,med,"2026-10-17","07:00");
        new HealthDao(context).insert(new HealthRecord(0,other,"2026-10-17",60f,null,null,null,null,null));
        try(ActivityScenario<EditProfileActivity> screen=ActivityScenario.launch(EditProfileActivity.class)) {
            openDelete(screen); confirm("test-password");
        }
        assertNull(users.getUserById(owner)); assertNotNull(users.getUserById(other));
        assertEquals(1,new HealthDao(context).getRecordsRange(other,"2026-10-17","2026-10-17").size());
        assertNull(medicines.getById(med.getId())); assertFalse(session.isLoggedIn());
        for(String time:new String[]{"07:00","19:00"}) {
            Intent intent=new Intent(context,MedicineAlarmReceiver.class).setData(android.net.Uri.parse("vita://alarm/"+med.getId()+"/"+time));
            assertNull(PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE));
        }
        assertNull(PendingIntent.getBroadcast(context,9001,new Intent(context,WaterReminderReceiver.class),PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE));
        assertEquals(0,context.getSystemService(NotificationManager.class).getActiveNotifications().length);
    }
    @Test public void changedSession_doesNotDeleteOrSignOutAnotherProfile() {
        try(ActivityScenario<EditProfileActivity> screen=ActivityScenario.launch(EditProfileActivity.class)) {
            openDelete(screen); session.saveSession(other,users.getUserById(other).getUsername(),"Other");
            confirm("test-password");
        }
        assertNotNull(users.getUserById(owner)); assertNotNull(users.getUserById(other)); assertEquals(other,session.getUserId());
    }
}

