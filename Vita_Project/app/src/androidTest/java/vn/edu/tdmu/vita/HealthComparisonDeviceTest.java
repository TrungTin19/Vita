package vn.edu.tdmu.vita;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.DetailHealthActivity;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import vn.edu.tdmu.vita.utils.SessionManager;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HealthComparisonDeviceTest {
    @Test public void invalidHiddenMetricIsRevealedWhenSaving() {
        try(ActivityScenario<vn.edu.tdmu.vita.activities.AddHealthActivity> screen=ActivityScenario.launch(vn.edu.tdmu.vita.activities.AddHealthActivity.class)) {
            screen.onActivity(a->{
                ((android.widget.EditText)a.findViewById(R.id.et_add_sugar)).setText("9999");
                com.google.android.material.chip.ChipGroup group=a.findViewById(R.id.sp_health_metric_group);
                group.check(group.getChildAt(3).getId());
            });
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            screen.onActivity(a->a.findViewById(R.id.btn_add_health_save).performClick());
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            screen.onActivity(a->{
                assertEquals(android.view.View.VISIBLE,a.findViewById(R.id.til_add_sugar).getVisibility());
                assertTrue("Unexpected focus: " + a.getCurrentFocus(), a.findViewById(R.id.et_add_sugar).hasFocus());
            });
        }
    }
    private Context context; private long owner, other;
    @Before public void setUp() {
        context=ApplicationProvider.getApplicationContext(); assertTrue(context.getPackageName().endsWith(".verification"));
        UserDao users=new UserDao(context); User a=new User(); a.setUsername("compare_"+System.nanoTime());
        owner=users.register(a,"test-password"); User b=new User(); b.setUsername("other_"+System.nanoTime());
        other=users.register(b,"test-password"); new SessionManager(context).saveSession(owner,a.getUsername(),"");
    }
    @After public void cleanUp() {
        new UserDao(context).deleteProfileWithPassword(owner,"test-password");
        new UserDao(context).deleteProfileWithPassword(other,"test-password");
        new SessionManager(context).clearSession();
    }
    private long glucose(long user,String date,String time,float value,String meal) {
        HealthRecord r=new HealthRecord(0,user,date,null,null,null,null,value,null);
        r.setMeasuredTime(time); r.setMealContext(meal); return new HealthDao(context).insert(r);
    }
    @Test public void partialBloodPressureIsShownWithoutInventingMissingValue() {
        HealthRecord record=new HealthRecord(0,owner,"2026-10-19",null,110,null,null,null,null);
        long id=new HealthDao(context).insert(record);
        try(ActivityScenario<DetailHealthActivity> screen=ActivityScenario.launch(new Intent(context,DetailHealthActivity.class)
                .putExtra(DetailHealthActivity.EXTRA_RECORD_ID,id))) {
            screen.onActivity(a->assertTrue(((TextView)a.findViewById(R.id.tv_detail_bp_value)).getText().toString().contains("110")));
        }
    }
    @Test public void comparisonSkipsOtherMealsFutureMeasurementsAndOtherProfiles() {
        glucose(owner,"2026-10-17","07:00",95,"FASTING");
        glucose(owner,"2026-10-18","08:00",140,"AFTER_MEAL");
        long current=glucose(owner,"2026-10-19","09:00",100,"FASTING");
        glucose(owner,"2026-10-19","10:00",110,"FASTING");
        glucose(other,"2026-10-19","08:30",160,"FASTING");
        try(ActivityScenario<DetailHealthActivity> screen=ActivityScenario.launch(new Intent(context,DetailHealthActivity.class)
                .putExtra(DetailHealthActivity.EXTRA_RECORD_ID,current))) {
            screen.onActivity(a->{
                TextView previous=a.findViewById(R.id.tv_detail_sugar_previous);
                assertNotNull("The previous comparable measurement is missing",previous);
                assertTrue("The comparison must be visible on its measurement card", previous.isShown());
                assertTrue(previous.getText().toString(),previous.getText().toString().contains("95"));
                assertTrue(previous.getText().toString(),previous.getText().toString().contains("+5"));
                assertFalse(previous.getText().toString().contains("140"));
                assertFalse(previous.getText().toString().contains("160"));
            });
        }
    }
}
