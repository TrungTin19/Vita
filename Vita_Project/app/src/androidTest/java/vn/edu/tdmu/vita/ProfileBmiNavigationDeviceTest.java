package vn.edu.tdmu.vita;

import android.app.Instrumentation;
import android.content.Context;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.ProfileActivity;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.SessionManager;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ProfileBmiNavigationDeviceTest {
    @Test public void profileOpensSeparateBmiScreen() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User();
        user.setUsername("bmi_nav_" + System.nanoTime());
        user.setHeightCm(170); user.setBaseWeightKg(60); user.setBirthYear(2000);
        long id = new UserDao(context).register(user, "test-password");
        new SessionManager(context).saveSession(id, user.getUsername(), "");
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor("vn.edu.tdmu.vita.activities.BmiActivity", null, false);
        try (ActivityScenario<ProfileActivity> screen = ActivityScenario.launch(ProfileActivity.class)) {
            screen.onActivity(activity -> {
                assertEquals(View.GONE, activity.findViewById(R.id.group_profile_bmi).getVisibility());
                activity.findViewById(R.id.btn_profile_open_bmi).performClick();
            });
            android.app.Activity bmi = instrumentation.waitForMonitorWithTimeout(monitor, 3000);
            assertNotNull("Profile must open the distinct BMI screen", bmi);
            instrumentation.runOnMainSync(() -> {
                assertEquals(View.VISIBLE, bmi.findViewById(R.id.group_profile_bmi).getVisibility());
                assertEquals(View.GONE, bmi.findViewById(R.id.group_profile_summary).getVisibility());
                bmi.finish();
            });
        } finally {
            instrumentation.removeMonitor(monitor);
            try (vn.edu.tdmu.vita.database.DatabaseHelper helper = new vn.edu.tdmu.vita.database.DatabaseHelper(context)) {
                helper.getWritableDatabase().delete("users", "id=?", new String[]{Long.toString(id)});
            }
            new SessionManager(context).clearSession();
        }
    }
}
