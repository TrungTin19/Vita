package vn.edu.tdmu.vita;

import android.content.Context;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.*;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import vn.edu.tdmu.vita.utils.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ProfileUpgradeDeviceTest {
    private Context context;
    private long owner;
    @Before public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("profile_upgrade_" + System.nanoTime());
        user.setBirthYear(1990); user.setHeightCm(170); user.setBaseWeightKg(60);
        owner = new UserDao(context).register(user, "test-password");
        new SessionManager(context).saveSession(owner, user.getUsername(), "");
    }
    @After public void cleanUp() {
        new UserDao(context).deleteProfileWithPassword(owner, "test-password");
        new SessionManager(context).clearSession();
    }
    @Test public void backProtectsDraftAfterRecreation() {
        try (ActivityScenario<EditProfileActivity> screen = ActivityScenario.launch(EditProfileActivity.class)) {
            screen.onActivity(a -> ((TextView) a.findViewById(R.id.et_edit_fullname)).setText("My draft"));
            screen.recreate();
            screen.onActivity(a -> {
                assertEquals("My draft", ((TextView) a.findViewById(R.id.et_edit_fullname)).getText().toString());
                a.getOnBackPressedDispatcher().onBackPressed();
            });
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText("Tiếp tục chỉnh sửa"))
                    .check(androidx.test.espresso.assertion.ViewAssertions.matches(androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
        }
    }
    @Test public void equivalentNumericFormattingDoesNotCreateUnsavedChanges() {
        try (ActivityScenario<EditProfileActivity> screen = ActivityScenario.launch(EditProfileActivity.class)) {
            screen.onActivity(a -> {
                ((TextView) a.findViewById(R.id.et_edit_height)).setText("170");
                a.getOnBackPressedDispatcher().onBackPressed();
            });
            long deadline = android.os.SystemClock.uptimeMillis() + 3000;
            while (screen.getState() != Lifecycle.State.DESTROYED && android.os.SystemClock.uptimeMillis() < deadline) {
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                android.os.SystemClock.sleep(20);
            }
            assertEquals(Lifecycle.State.DESTROYED, screen.getState());
        }
    }
    @Test public void profileNamesTheWeightAndDateUsedForBmi() {
        HealthRecord record = new HealthRecord(0, owner, DateTimeUtils.getTodayDate(), 65f, null, null, null, null, null);
        record.setMeasuredTime("07:00"); new HealthDao(context).insert(record);
        try (ActivityScenario<ProfileActivity> screen = ActivityScenario.launch(ProfileActivity.class)) {
            screen.onActivity(a -> {
                TextView source = a.findViewById(context.getResources().getIdentifier("tv_profile_weight_source", "id", context.getPackageName()));
                assertNotNull("The source of BMI weight is not shown", source);
                assertTrue(source.getText().toString().contains("65"));
                assertTrue(source.getText().toString().contains(DateTimeUtils.formatDisplayDate(DateTimeUtils.getTodayDate())));
            });
        }
    }
}
