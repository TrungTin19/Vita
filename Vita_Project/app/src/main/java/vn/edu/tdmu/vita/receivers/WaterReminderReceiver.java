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
 * Kiểm tra khung giờ nhắc và mục tiêu nước trong hồ sơ theo chu kỳ hai giờ hiện tại.
 */
public class WaterReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_DRINKED = "vn.edu.tdmu.vita.ACTION_WATER_DRINKED";
    public static final String EXTRA_AMOUNT = "water_amount";
    public static final String EXTRA_USER_ID = "water_user_id";
    private static final int NOTIFICATION_ID = 9001;
    private static final int REQUEST_CODE = 9001;

    @Override
    public void onReceive(Context context, Intent intent) {
        SessionManager session = new SessionManager(context);
        if (!session.isLoggedIn()) return;
        if (ACTION_DRINKED.equals(intent.getAction())) {
            long userId = session.getUserId();
            if (SessionManager.matchesUser(intent.getLongExtra(EXTRA_USER_ID, -1), userId)) {
                WaterDao waterDao = new WaterDao(context);
                int amount = intent.getIntExtra(EXTRA_AMOUNT, 250);
                if (amount < 50 || amount > 2000) return;
                long saved = waterDao.addWater(userId, DateTimeUtils.getTodayDate(), amount,
                        DateTimeUtils.getCurrentTimestamp());
                if (saved > 0) Toast.makeText(context, context.getString(vn.edu.tdmu.vita.R.string.water_added, amount), Toast.LENGTH_SHORT).show();
            }
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
            return;
        }
        vn.edu.tdmu.vita.models.User user = new vn.edu.tdmu.vita.database.UserDao(context).getUserById(session.getUserId());
        if (user == null) return;
        cancel(context);
        schedule(context);
        java.util.Calendar now = java.util.Calendar.getInstance();
        int minute = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE);
        int total = new WaterDao(context).getDailyTotal(user.getId(), DateTimeUtils.getTodayDate());
        if (vn.edu.tdmu.vita.utils.WaterReminderPolicy.shouldNotify(user.getWaterStart(), user.getWaterEnd(),
                minute, total, vn.edu.tdmu.vita.utils.HydrationGoal.forUserGoal(user != null ? user.getWaterGoalMl() : 0))) {
            NotificationHelper.showWaterReminder(context, NOTIFICATION_ID);
        } else {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
        }
    }

    public static void schedule(Context context) {
        if (!new SessionManager(context).isLoggedIn()) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = new Intent(context, WaterReminderReceiver.class);
        // Khi ứng dụng hoạt động lại, giữ lần kiểm tra đang chờ; mỗi lần nhắc sẽ lên lịch lần kế tiếp trong khung giờ cho phép.
        if (PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null) return;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        vn.edu.tdmu.vita.models.User user = new vn.edu.tdmu.vita.database.UserDao(context)
                .getUserById(new SessionManager(context).getUserId());
        if (user == null) { pendingIntent.cancel(); return; }
        long next = vn.edu.tdmu.vita.utils.WaterReminderPolicy.nextTriggerMillis(user.getWaterStart(),user.getWaterEnd(),user.getWaterTimes(),System.currentTimeMillis());
        if (next < 0) { pendingIntent.cancel(); return; }
        try {
            if (android.os.Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,pendingIntent);
            }
        } catch (SecurityException e) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,pendingIntent);
        }
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = new Intent(context, WaterReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }
}
