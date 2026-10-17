package vn.edu.tdmu.vita;

import android.content.Context;
import android.widget.TextView;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.*;
import vn.edu.tdmu.vita.activities.*;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.receivers.WaterReminderReceiver;
import vn.edu.tdmu.vita.utils.SessionManager;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BmiScopeDeviceTest {
    private Context context; private final List<Long> created=new ArrayList<>();
    @Before public void setup() {
        context=ApplicationProvider.getApplicationContext(); assertTrue(context.getPackageName().endsWith(".verification"));
    }
    private void profile(int year) {
        User user=new User(); user.setUsername("bmi_scope_"+System.nanoTime()); user.setHeightCm(170);
        user.setBaseWeightKg(130); user.setBmiStandard("WHO"); user.setBirthYear(year);
        long id=new UserDao(context).register(user,"test-password"); assertTrue(id>0); created.add(id);
        new SessionManager(context).saveSession(id,user.getUsername(),"BMI test");
    }
    @After public void cleanup() {
        WaterReminderReceiver.cancel(context); new SessionManager(context).clearSession();
        try(DatabaseHelper helper=new DatabaseHelper(context)) {
            for(long id:created) helper.getWritableDatabase().delete("users","id=?",new String[]{String.valueOf(id)});
        }
    }
    @Test public void childAndUnknownBirthYear_showNumericOnlyOnBothScreens() {
        for(int year:new int[]{Calendar.getInstance().get(Calendar.YEAR)-12,0}) {
            profile(year);
            try(ActivityScenario<ProfileActivity> screen=ActivityScenario.launch(ProfileActivity.class)) {
                screen.onActivity(a->{
                    assertEquals(context.getString(R.string.bmi_numeric_only),((TextView)a.findViewById(R.id.tv_profile_bmi_category)).getText().toString());
                    assertFalse(((TextView)a.findViewById(R.id.tv_profile_bmi_value)).getText().toString().contains("--"));
                    assertEquals(context.getString(R.string.bmi_outside_adult_scope),((TextView)a.findViewById(R.id.tv_profile_healthy_weight)).getText().toString());
                });
            }
            try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
                screen.onActivity(a->{
                    assertEquals(context.getString(R.string.bmi_numeric_only),((TextView)a.findViewById(R.id.tv_home_bmi_badge)).getText().toString());
                    assertEquals(context.getString(R.string.bmi_outside_adult_scope),((TextView)a.findViewById(R.id.tv_home_bmi_standard)).getText().toString());
                });
            }
        }
    }
    @Test public void adultWhoBmi45_showsClassThreeOnBothScreens() {
        profile(Calendar.getInstance().get(Calendar.YEAR)-30);
        try(ActivityScenario<ProfileActivity> screen=ActivityScenario.launch(ProfileActivity.class)) {
            screen.onActivity(a->assertEquals("Béo phì độ III",((TextView)a.findViewById(R.id.tv_profile_bmi_category)).getText().toString()));
        }
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a->assertEquals("Béo phì độ III",((TextView)a.findViewById(R.id.tv_home_bmi_badge)).getText().toString()));
        }
    }
}
