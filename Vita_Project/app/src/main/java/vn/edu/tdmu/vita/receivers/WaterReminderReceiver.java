package vn.edu.tdmu.vita.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import vn.edu.tdmu.vita.database.WaterDao;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.NotificationHelper;
import vn.edu.tdmu.vita.utils.SessionManager;

/**
 * Nhắc uống nước mỗi 2h + nút "+250 ml" ghi thẳng vào nhật ký.
 */
public class WaterReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_DRINKED = "vn.edu.tdmu.vita.ACTION_WATER_DRINKED";
    private static final int NOTIFICATION_ID = 9001;
    private static final int REQUEST_CODE = 9001;
    private static final long INTERVAL_MS = 2L * 60 * 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_DRINKED.equals(intent.getAction())) {
            SessionManager session = new SessionManager(context);
            long userId = session.getUserId();
            if (userId > 0) {
                WaterDao waterDao = new WaterDao(context);
                waterDao.addWater(userId, DateTimeUtils.getTodayDate(), 250,
                        DateTimeUtils.getCurrentTimestamp());
                Toast.makeText(context, "Đã ghi nhận 250 ml nước!", Toast.LENGTH_SHORT).show();
            }
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
            return;
        }
        NotificationHelper.showWaterReminder(context, NOTIFICATION_ID);
    }

    public static void schedule(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = new Intent(context, WaterReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + INTERVAL_MS, INTERVAL_MS, pendingIntent);
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = new Intent(context, WaterReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
