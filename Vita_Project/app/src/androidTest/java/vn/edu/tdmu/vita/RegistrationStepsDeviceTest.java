package vn.edu.tdmu.vita;

import android.content.Context;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.RegisterActivity;
import vn.edu.tdmu.vita.database.UserDao;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RegistrationStepsDeviceTest {
    @Test public void secondStepSurvivesRecreation_andBackKeepsAccountDraft() {
        try (ActivityScenario<RegisterActivity> screen = ActivityScenario.launch(RegisterActivity.class)) {
            screen.onActivity(activity -> {
                ((TextView) activity.findViewById(R.id.et_reg_username)).setText("draft-user");
                ((TextView) activity.findViewById(R.id.et_reg_password)).setText("test-password");
                ((TextView) activity.findViewById(R.id.et_reg_confirm)).setText("test-password");
                activity.findViewById(R.id.btn_reg_submit).performClick();
                ((TextView) activity.findViewById(R.id.et_reg_height)).setText("171");
                ((TextView) activity.findViewById(R.id.et_reg_fullname)).setText("Draft profile");
            });
            screen.recreate();
            screen.onActivity(activity -> {
                assertTrue(activity.findViewById(R.id.et_reg_height).isShown());
                assertEquals("171", ((TextView) activity.findViewById(R.id.et_reg_height)).getText().toString());
                assertEquals("Draft profile", ((TextView) activity.findViewById(R.id.et_reg_fullname)).getText().toString());
                activity.getOnBackPressedDispatcher().onBackPressed();
                assertTrue(activity.findViewById(R.id.et_reg_username).isShown());
                assertEquals("draft-user", ((TextView) activity.findViewById(R.id.et_reg_username)).getText().toString());
                assertEquals("test-password", ((TextView) activity.findViewById(R.id.et_reg_password)).getText().toString());
            });
        }
    }

    @Test public void continueDoesNotCreateAccount_evenWhenProfileFieldsAreFilled() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        String username = "steps_" + System.nanoTime();
        UserDao users = new UserDao(context);
        try (ActivityScenario<RegisterActivity> screen = ActivityScenario.launch(RegisterActivity.class)) {
            screen.onActivity(activity -> {
                ((TextView) activity.findViewById(R.id.et_reg_username)).setText(username);
                ((TextView) activity.findViewById(R.id.et_reg_password)).setText("test-password");
                ((TextView) activity.findViewById(R.id.et_reg_confirm)).setText("test-password");
                ((TextView) activity.findViewById(R.id.et_reg_height)).setText("170");
                ((TextView) activity.findViewById(R.id.et_reg_weight)).setText("60");
                activity.findViewById(R.id.btn_reg_submit).performClick();
                assertFalse("Continue must show step two before saving", users.isUsernameExists(username));
                assertTrue(activity.findViewById(R.id.et_reg_height).isShown());
                assertFalse(activity.findViewById(R.id.et_reg_username).isShown());
            });
        } finally {
            try (vn.edu.tdmu.vita.database.DatabaseHelper helper = new vn.edu.tdmu.vita.database.DatabaseHelper(context)) {
                helper.getWritableDatabase().delete("users", "username=?", new String[]{username});
            }
            new vn.edu.tdmu.vita.utils.SessionManager(context).clearSession();
        }
    }
}
