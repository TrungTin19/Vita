package vn.edu.tdmu.vita;

import android.content.*;
import android.widget.TextView;
import android.widget.EditText;
import android.view.KeyEvent;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.EditProfileActivity;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.receivers.WaterReminderReceiver;
import vn.edu.tdmu.vita.utils.SessionManager;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class WaterSettingsScreenDeviceTest {
    @Test public void correctingWaterAmountClearsItsStaleError() {
        Intent intent = new Intent(context, EditProfileActivity.class).putExtra("water_settings_only", true);
        try (ActivityScenario<EditProfileActivity> screen = ActivityScenario.launch(intent)) {
            screen.onActivity(a -> {
                EditText amount = a.findViewById(R.id.et_edit_water_amount);
                com.google.android.material.textfield.TextInputLayout field = a.findViewById(R.id.til_edit_water_amount);
                amount.setText("20");
                a.findViewById(R.id.btn_edit_save).performClick();
                assertNotNull(field.getError());
                amount.setText("250");
                assertNull("An old error must clear when the value changes", field.getError());
            });
        }
    }
    @Test public void twoHourPreviewMatchesOvernightWindow_andOmitsUnalignedEnd() {
        Intent intent = new Intent(context, EditProfileActivity.class).putExtra("water_settings_only", true);
        try (ActivityScenario<EditProfileActivity> screen = ActivityScenario.launch(intent)) {
            screen.onActivity(a -> {
                ((TextView) a.findViewById(R.id.et_edit_water_start)).setText("22:00");
                ((TextView) a.findViewById(R.id.et_edit_water_end)).setText("05:00");
                String preview = ((TextView) a.findViewById(R.id.tv_water_schedule_preview)).getText().toString();
                assertTrue(preview, preview.contains("22:00 · 00:00 · 02:00 · 04:00"));
                assertFalse(preview, preview.contains("05:00"));
            });
        }
    }
    private Context context; private long owner;
    @Before public void setup() {
        context=ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        User user=new User(); user.setUsername("water_only_"+System.nanoTime()); user.setFullname("Unchanged");
        owner=new UserDao(context).register(user,"test-password"); assertTrue(owner>0);
        new SessionManager(context).saveSession(owner,user.getUsername(),"Unchanged");
    }
    @After public void cleanup() {
        WaterReminderReceiver.cancel(context); new SessionManager(context).clearSession();
        try(DatabaseHelper helper=new DatabaseHelper(context)) { helper.getWritableDatabase().delete("users","id=?",new String[]{String.valueOf(owner)}); }
    }
    @Test public void waterOnlyMode_savesWithoutRequiringHeightOrWeight() {
        Intent intent=new Intent(context,EditProfileActivity.class).putExtra("water_settings_only",true);
        try(ActivityScenario<EditProfileActivity> screen=ActivityScenario.launch(intent)) {
            screen.onActivity(activity->{
                ((TextView)activity.findViewById(R.id.et_edit_water_goal)).setText("1500");
                ((TextView)activity.findViewById(R.id.et_edit_water_start)).setText("08:00");
                ((TextView)activity.findViewById(R.id.et_edit_water_end)).setText("20:00");
                ((TextView)activity.findViewById(R.id.et_edit_water_amount)).setText("350");
            });
            screen.recreate();
            screen.onActivity(activity->activity.findViewById(R.id.btn_edit_save).performClick());
        }
        User saved=new UserDao(context).getUserById(owner);
        assertEquals(1500,saved.getWaterGoalMl()); assertEquals(350,saved.getWaterAmount());
        assertEquals("Unchanged",saved.getFullname()); assertEquals(0,saved.getHeightCm(),0); assertEquals(0,saved.getBaseWeightKg(),0);
    }
    @Test public void timePickers_ignoreHardwareTextEditing() {
        Intent intent=new Intent(context,EditProfileActivity.class).putExtra("water_settings_only",true);
        try(ActivityScenario<EditProfileActivity> screen=ActivityScenario.launch(intent)) {
            screen.onActivity(activity->{
                for(int id:new int[]{R.id.et_edit_water_start,R.id.et_edit_water_end}) {
                    EditText field=activity.findViewById(id);
                    assertTrue(field.isFocusable());
                    String original=field.getText().toString();
                    field.requestFocus(); field.setSelection(field.length());
                    field.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DEL));
                    field.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DEL));
                    assertEquals("Time must only change through the picker",original,field.getText().toString());
                }
            });
        }
    }
}
