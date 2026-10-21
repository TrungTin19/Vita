package vn.edu.tdmu.vita;

import android.content.*;
import android.graphics.Bitmap;
import android.widget.EditText;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.*;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.*;
import com.google.android.material.textfield.TextInputLayout;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FormClippingDeviceTest {
    private void capture(Context context, String name) throws Exception {
        android.app.Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.waitForIdleSync();
        instrumentation.getUiAutomation().waitForIdle(300, 5000);
        Bitmap image = instrumentation.getUiAutomation().takeScreenshot();
        assertNotNull(image);
        try (FileOutputStream out = new FileOutputStream(new File(context.getExternalFilesDir(null), "clipping-" + name + ".png"))) {
            assertTrue(image.compress(Bitmap.CompressFormat.PNG, 100, out));
        } finally { image.recycle(); }
    }

    @Test public void captureInvalidFormsAndWaterWindowCorrections() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user = new User(); user.setUsername("clipping_" + System.nanoTime());
        user.setHeightCm(165); user.setBaseWeightKg(55);
        UserDao users = new UserDao(context);
        long owner = users.register(user, "test-password");
        new SessionManager(context).saveSession(owner, user.getUsername(), "");
        try {
            try (ActivityScenario<AddHealthActivity> screen = ActivityScenario.launch(AddHealthActivity.class)) {
                screen.onActivity(a -> {
                    ((EditText) a.findViewById(R.id.et_add_systolic)).setText("20");
                    ((EditText) a.findViewById(R.id.et_add_diastolic)).setText("80");
                    ((EditText) a.findViewById(R.id.et_add_sugar)).setText("10");
                    a.findViewById(R.id.btn_add_health_save).performClick();
                    assertNotNull(((TextInputLayout) a.findViewById(R.id.til_add_systolic)).getError());
                });
                capture(context, "health-errors");
            }
            try (ActivityScenario<EditProfileActivity> screen = ActivityScenario.launch(new Intent(context, EditProfileActivity.class)
                    .putExtra(EditProfileActivity.EXTRA_WATER_SETTINGS_ONLY, true))) {
                screen.onActivity(a -> {
                    ((EditText) a.findViewById(R.id.et_edit_water_start)).setText("07:00");
                    ((EditText) a.findViewById(R.id.et_edit_water_end)).setText("07:00");
                    a.findViewById(R.id.btn_edit_save).performClick();
                    assertNotNull(((TextInputLayout) a.findViewById(R.id.til_edit_water_end)).getError());
                    assertNull(((TextInputLayout) a.findViewById(R.id.til_edit_water_amount)).getError());
                });
                capture(context, "water-window-error");
                screen.onActivity(a -> {
                    ((EditText) a.findViewById(R.id.et_edit_water_end)).setText("22:00");
                    assertNull(((TextInputLayout) a.findViewById(R.id.til_edit_water_end)).getError());
                });
                capture(context, "water-settings");
            }
            try (ActivityScenario<MainActivity> screen = ActivityScenario.launch(MainActivity.class)) {
                screen.onActivity(a -> {
                    WaterJournalDialog.showAddAmount(a, owner, null);
                    a.getSupportFragmentManager().executePendingTransactions();
                });
                capture(context, "water-add");
            }
        } finally {
            users.deleteProfileWithPassword(owner, "test-password");
            new SessionManager(context).clearSession();
        }
    }
}
