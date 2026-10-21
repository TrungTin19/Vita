package vn.edu.tdmu.vita;
import android.app.NotificationManager;
import android.content.*;
import android.view.View;
import android.widget.*;
import androidx.test.core.app.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.*;
import vn.edu.tdmu.vita.activities.*;
import vn.edu.tdmu.vita.adapters.MedicineAdapter;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import vn.edu.tdmu.vita.receivers.*;
import vn.edu.tdmu.vita.utils.*;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class ReviewImprovementsDeviceTest {
    private Context context; private long owner; private SessionManager session;
    @Before public void setUp() {
        context=ApplicationProvider.getApplicationContext();
        assertTrue("Use isolated verification app",context.getPackageName().endsWith(".verification"));
        User user=new User(); user.setUsername("review_"+System.nanoTime()); user.setHeightCm(170); user.setBaseWeightKg(60);
        owner=new UserDao(context).register(user,"test-password"); assertTrue(owner>0);
        session=new SessionManager(context); session.saveSession(owner,"review","Test");
    }
    @After public void cleanup() {
        for(Medicine med:new MedicineDao(context).getAllByUserId(owner)) MedicineAlarmReceiver.cancel(context,med);
        try(DatabaseHelper helper=new DatabaseHelper(context)) {helper.getWritableDatabase().delete("users","id=?",new String[]{String.valueOf(owner)});}
        session.clearSession(); context.getSystemService(NotificationManager.class).cancelAll(); WaterReminderReceiver.cancel(context);
    }
    @Test public void resumeScheduling_doesNotReplaceAnOverdueDose() throws Exception {
        Medicine med=new Medicine(0,owner,"Test","1","07:00,12:00,19:00",127,true);
        med.setId(new MedicineDao(context).insert(med));
        MedicineAlarmReceiver.schedule(context,med);
        Intent overdue=new Intent(context,MedicineAlarmReceiver.class).setData(android.net.Uri.parse("vita://alarm/"+med.getId()+"/12:00"))
                .putExtra(MedicineAlarmReceiver.EXTRA_MEDICINE_ID,med.getId()).putExtra(MedicineAlarmReceiver.EXTRA_DOSE_DATE,"2000-01-01").putExtra(MedicineAlarmReceiver.EXTRA_DOSE_TIME,"12:00");
        android.app.PendingIntent pending=android.app.PendingIntent.getBroadcast(context,0,overdue,android.app.PendingIntent.FLAG_UPDATE_CURRENT|android.app.PendingIntent.FLAG_IMMUTABLE);
        MedicineAlarmReceiver.schedule(context,med);
        pending.send();
        int expected=NotificationHelper.doseNotificationId(med.getId(),"2000-01-01","12:00");
        boolean shown=false;
        for(int attempt=0;attempt<30 && !shown;attempt++) {
            for(android.service.notification.StatusBarNotification n:context.getSystemService(NotificationManager.class).getActiveNotifications()) if(n.getId()==expected) shown=true;
            if(!shown) Thread.sleep(100);
        }
        assertTrue("Resume replaced the overdue dose identity",shown);
    }
    @Test public void schedule_createsIndependentDoseAlarmsAndCancelsEveryTime() {
        Medicine med=new Medicine(0,owner,"Test","1","07:00,12:00,19:00",127,true);
        med.setId(new MedicineDao(context).insert(med));
        MedicineAlarmReceiver.schedule(context,med);
        for(String time:med.getTime().split(",")) {
            Intent alarm=new Intent(context,MedicineAlarmReceiver.class).setData(android.net.Uri.parse("vita://alarm/"+med.getId()+"/"+time));
            assertNotNull(android.app.PendingIntent.getBroadcast(context,0,alarm,android.app.PendingIntent.FLAG_NO_CREATE|android.app.PendingIntent.FLAG_IMMUTABLE));
        }
        MedicineAlarmReceiver.cancel(context,med);
        for(String time:med.getTime().split(",")) {
            Intent alarm=new Intent(context,MedicineAlarmReceiver.class).setData(android.net.Uri.parse("vita://alarm/"+med.getId()+"/"+time));
            assertNull(android.app.PendingIntent.getBroadcast(context,0,alarm,android.app.PendingIntent.FLAG_NO_CREATE|android.app.PendingIntent.FLAG_IMMUTABLE));
        }
    }
    @Test public void delayedDoseAction_keepsOriginalDateAndIndependentTimes() {
        MedicineDao dao=new MedicineDao(context);
        long id=dao.insert(new Medicine(0,owner,"Test","1","07:00,12:00,19:00",127,true));
        Intent action=new Intent(NotificationActionReceiver.ACTION_TAKEN).putExtra(NotificationActionReceiver.EXTRA_MEDICINE_ID,id)
                .putExtra(MedicineAlarmReceiver.EXTRA_DOSE_DATE,"2026-01-01").putExtra(MedicineAlarmReceiver.EXTRA_DOSE_TIME,"19:00");
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync(()->new NotificationActionReceiver().onReceive(context,action));
        assertEquals("TAKEN",dao.getLogForDose(id,owner,"2026-01-01","19:00").getStatus());
        assertNull(dao.getLogForDose(id,owner,"2026-01-01","07:00"));
        assertNull(dao.getLogForDose(id,owner,DateTimeUtils.getTodayDate(),"19:00"));
    }
    @Test public void waterAction_customAmountAndReachedGoalSuppressesReminder() {
        UserDao users=new UserDao(context); User user=users.getUserById(owner);
        user.setWaterAmount(350); user.setWaterGoalMl(350); users.updateWaterSettings(owner,350,user.getWaterStart(),user.getWaterEnd(),350,"");
        Intent action=new Intent(WaterReminderReceiver.ACTION_DRINKED).putExtra(WaterReminderReceiver.EXTRA_USER_ID,owner).putExtra(WaterReminderReceiver.EXTRA_AMOUNT,350);
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync(()->new WaterReminderReceiver().onReceive(context,action));
        assertEquals(350,new WaterDao(context).getDailyTotal(owner,DateTimeUtils.getTodayDate()));
        new WaterReminderReceiver().onReceive(context,new Intent());
        for(android.service.notification.StatusBarNotification n:context.getSystemService(NotificationManager.class).getActiveNotifications()) assertNotEquals(9001,n.getId());
    }
    @Test public void medicineEditor_keepsTimesAfterRecreation() {
        Medicine medicine=new Medicine(0,owner,"Test","1","07:00,12:00,19:00",127,true);
        MedicineDao dao=new MedicineDao(context); long id=dao.insert(medicine); medicine.setId(id);
        Intent intent=new Intent(context,AddMedicineActivity.class).putExtra(AddMedicineActivity.EXTRA_MEDICINE_ID,id);
        try(ActivityScenario<AddMedicineActivity> activity=ActivityScenario.launch(intent)) {
            activity.recreate();
            activity.onActivity(screen->{
                assertEquals(3,((com.google.android.material.chip.ChipGroup)screen.findViewById(R.id.chips_med_times)).getChildCount());
            });
        }
    }
    @Test public void waterSettingsScreen_savesCustomPreferences() {
        try(ActivityScenario<EditProfileActivity> activity=ActivityScenario.launch(new Intent(context,EditProfileActivity.class).putExtra(EditProfileActivity.EXTRA_WATER_SETTINGS_ONLY,true))) {
            activity.onActivity(screen->{
                ((TextView)screen.findViewById(R.id.et_edit_water_start)).setText("08:00");
                ((TextView)screen.findViewById(R.id.et_edit_water_end)).setText("20:00");
                ((TextView)screen.findViewById(R.id.et_edit_water_amount)).setText("350");
                screen.findViewById(R.id.btn_edit_save).performClick();
            });
        }
        User saved=new UserDao(context).getUserById(owner);
        assertEquals("08:00",saved.getWaterStart()); assertEquals(350,saved.getWaterAmount());
    }
    @Test public void healthEditor_keepsMealAndTime() {
        HealthRecord record=new HealthRecord(0,owner,DateTimeUtils.getTodayDate(),null,null,null,null,100f,null);
        record.setMeasuredTime("07:00"); record.setMealContext("FASTING");
        long id=new HealthDao(context).insert(record);
        try(ActivityScenario<AddHealthActivity> activity=ActivityScenario.launch(new Intent(context,AddHealthActivity.class).putExtra(AddHealthActivity.EXTRA_RECORD_ID,id))) {
            activity.recreate(); activity.onActivity(screen->{
                assertEquals("07:00",((TextView)screen.findViewById(R.id.et_health_time)).getText().toString());
                assertEquals(1,((Spinner)screen.findViewById(R.id.sp_meal_context)).getSelectedItemPosition());
            });
        }

    }
}
