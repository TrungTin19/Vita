package vn.edu.tdmu.vita;

import android.content.Context;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.MainActivity;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UiAuditDeviceTest {
    @Test public void waterPercentageAgreesAcrossTabsIncludingHalfAndOverGoal() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User();
        user.setUsername("ui_audit_" + System.nanoTime());
        user.setHeightCm(170); user.setBaseWeightKg(60); user.setWaterGoalMl(2000);
        UserDao users = new UserDao(context);
        long owner = users.register(user, "test-password");
        new SessionManager(context).saveSession(owner, user.getUsername(), "");
        WaterDao water = new WaterDao(context);
        int previous = 0;
        try {
            for (int total : new int[]{0, 1250, 2250}) {
                if (total > previous) water.addWater(owner, DateTimeUtils.getTodayDate(), total - previous, DateTimeUtils.getCurrentTimestamp());
                previous = total;
                try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
                    final String percent = Math.round(total * 100f / 2000) + "%";
                    screen.onActivity(a -> assertTrue(((TextView) a.findViewById(R.id.tv_home_water_percent)).getText().toString().contains(percent)));
                    screen.onActivity(a -> ((BottomNavigationView) a.findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_reminder));
                    screen.onActivity(a -> assertTrue(((TextView) a.findViewById(R.id.tv_water_progress_text)).getText().toString().contains(percent)));
                }
            }
        } finally {
            users.deleteProfileWithPassword(owner, "test-password");
            new SessionManager(context).clearSession();
        }
    }
}
