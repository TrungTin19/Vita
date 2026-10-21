package vn.edu.tdmu.vita;

import android.content.Context;
import android.widget.TextView;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.MainActivity;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import vn.edu.tdmu.vita.utils.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HomeDashboardDeviceTest {
    @Test public void dashboardUsesOwnedDataAndSharesQuickWaterAction() {
        Context context=ApplicationProvider.getApplicationContext(); assertTrue(context.getPackageName().endsWith(".verification"));
        User user=new User(); user.setUsername("dashboard_"+System.nanoTime()); user.setHeightCm(170); user.setBaseWeightKg(60);
        UserDao users=new UserDao(context); long owner=users.register(user,"test-password");
        new SessionManager(context).saveSession(owner,user.getUsername(),"");
        WaterDao water=new WaterDao(context); water.addWater(owner,DateTimeUtils.getTodayDate(),600,DateTimeUtils.getCurrentTimestamp());
        new MedicineDao(context).insert(new Medicine(0,owner,"Dashboard medicine","1 tablet","07:00,19:00",127,true));
        HealthRecord record=new HealthRecord(0,owner,DateTimeUtils.getTodayDate(),65f,null,null,null,null,null);
        record.setMeasuredTime("07:00"); new HealthDao(context).insert(record);
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            screen.onActivity(a->{
                TextView progress=a.findViewById(context.getResources().getIdentifier("tv_home_water_progress","id",context.getPackageName()));
                assertNotNull("Home does not show today's water",progress); assertTrue(progress.getText().toString().contains("600"));
                TextView next=a.findViewById(context.getResources().getIdentifier("tv_home_next_dose","id",context.getPackageName()));
                assertTrue(next.getText().toString().contains("Dashboard medicine"));
                a.findViewById(context.getResources().getIdentifier("btn_home_add_water","id",context.getPackageName())).performClick();
                assertEquals(850,water.getDailyTotal(owner,DateTimeUtils.getTodayDate()));
            });
        } finally { users.deleteProfileWithPassword(owner,"test-password"); new SessionManager(context).clearSession(); }
    }
}
