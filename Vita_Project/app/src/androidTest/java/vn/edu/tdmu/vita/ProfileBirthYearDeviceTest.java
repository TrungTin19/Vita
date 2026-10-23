package vn.edu.tdmu.vita;

import android.content.Context;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.*;
import com.google.android.material.textfield.TextInputLayout;
import vn.edu.tdmu.vita.activities.*;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.receivers.*;
import vn.edu.tdmu.vita.utils.SessionManager;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ProfileBirthYearDeviceTest {
    private Context context;
    private UserDao users;
    private final List<Long> created=new ArrayList<>();
    @Before public void setup() {
        context=ApplicationProvider.getApplicationContext();
        assertTrue("Use verification package only",context.getPackageName().endsWith(".verification"));
        users=new UserDao(context);
    }
    @After public void cleanup() {
        for(long id:created) {
            for(vn.edu.tdmu.vita.models.Medicine med:new MedicineDao(context).getAllByUserId(id)) MedicineAlarmReceiver.cancel(context,med);
            try(DatabaseHelper helper=new DatabaseHelper(context)) { helper.getWritableDatabase().delete("users","id=?",new String[]{String.valueOf(id)}); }
        }
        WaterReminderReceiver.cancel(context);
        new SessionManager(context).clearSession();
    }
    private String register(String year) {
        String name="birth_"+System.nanoTime();
        try(ActivityScenario<RegisterActivity> screen=ActivityScenario.launch(RegisterActivity.class)) {
            screen.onActivity(activity->{
                fill(activity,name,year);
                activity.findViewById(R.id.btn_reg_submit).performClick();
                activity.findViewById(R.id.btn_reg_submit).performClick();
            });
            DeviceTestWait.until("Registration must finish before closing its screen",
                    () -> new SessionManager(context).isLoggedIn());
        }
        User user=users.login(name,"test-password");
        assertNotNull(user); created.add(user.getId());
        return name;
    }
    private void fill(RegisterActivity screen,String name,String year) {
        ((TextView)screen.findViewById(R.id.et_reg_username)).setText(name);
        ((TextView)screen.findViewById(R.id.et_reg_password)).setText("test-password");
        ((TextView)screen.findViewById(R.id.et_reg_confirm)).setText("test-password");
        ((TextView)screen.findViewById(R.id.et_reg_height)).setText("170");
        ((TextView)screen.findViewById(R.id.et_reg_weight)).setText("60");
        ((TextView)screen.findViewById(R.id.et_reg_birth_year)).setText(year);
    }
    @Test public void blankRegistrationYear_showsUnknownInProfile() {
        User user=users.login(register(""),"test-password");
        assertEquals(0,user.getBirthYear());
        try(ActivityScenario<ProfileActivity> screen=ActivityScenario.launch(ProfileActivity.class)) {
            screen.onActivity(activity->assertEquals(context.getString(R.string.profile_not_updated),
                    ((TextView)activity.findViewById(R.id.tv_profile_birth_year)).getText().toString()));
        }
    }
    @Test public void validRegistrationYear_keepsEnteredValue() {
        assertEquals(2001,users.login(register("2001"),"test-password").getBirthYear());
    }
    @Test public void invalidRegistrationYear_doesNotCreateProfile() {
        String name="invalid_birth_"+System.nanoTime();
        try(ActivityScenario<RegisterActivity> screen=ActivityScenario.launch(RegisterActivity.class)) {
            for(String year:new String[]{"1899","9999"}) screen.onActivity(activity->{
                fill(activity,name,year); activity.findViewById(R.id.btn_reg_submit).performClick();
                activity.findViewById(R.id.btn_reg_submit).performClick();
                assertNotNull(((TextInputLayout)activity.findViewById(R.id.til_reg_birth_year)).getError());
                assertFalse(users.isUsernameExists(name));
            });
        }
    }
    @Test public void clearingYearInEditor_removesKnownYear() {
        User user=new User(); user.setUsername("clear_year_"+System.nanoTime());
        user.setBirthYear(2000); user.setHeightCm(170); user.setBaseWeightKg(60);
        long id=users.register(user,"test-password"); created.add(id);
        new SessionManager(context).saveSession(id,user.getUsername(),"");
        try(ActivityScenario<EditProfileActivity> screen=ActivityScenario.launch(EditProfileActivity.class)) {
            screen.onActivity(activity->{
                ((TextView)activity.findViewById(R.id.et_edit_birth_year)).setText("");
                activity.findViewById(R.id.btn_edit_save).performClick();
            });
        }
        assertEquals(0,users.getUserById(id).getBirthYear());
    }
    @Test public void viewingKnownYear2000_doesNotReinterpretItAsUnknown() {
        User user=new User(); user.setUsername("known_year_"+System.nanoTime());
        user.setBirthYear(2000); user.setHeightCm(170); user.setBaseWeightKg(60);
        long id=users.register(user,"test-password"); created.add(id);
        new SessionManager(context).saveSession(id,user.getUsername(),"");
        try(ActivityScenario<ProfileActivity> screen=ActivityScenario.launch(ProfileActivity.class)) {
            screen.onActivity(activity->assertTrue(((TextView)activity.findViewById(R.id.tv_profile_birth_year)).getText().toString().startsWith("2000")));
        }
        assertEquals(2000,users.getUserById(id).getBirthYear());
    }
}
