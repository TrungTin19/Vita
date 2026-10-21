package vn.edu.tdmu.vita.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.receivers.NotificationActionReceiver;

/**
 * Kênh + builder thông báo nhắc thuốc (2 nút Đã uống / Bỏ qua).
 */
public final class NotificationHelper {

    public static final String CHANNEL_ID = "vita_medicine_channel";
    private static final String CHANNEL_NAME = "Nhắc nhở uống thuốc Vita";
    private static final String PREFS_CHANNEL = "vita_channels";
    private static final String KEY_MED_CHANNEL_V = "med_channel_v";
    private static final int MED_CHANNEL_VERSION = 3;

    public static final String WATER_CHANNEL_ID = "vita_water_channel";
    private static final String WATER_CHANNEL_NAME = "Nhắc nhở uống nước Vita";

    private NotificationHelper() {}

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_CHANNEL, Context.MODE_PRIVATE);
        if (prefs.getInt(KEY_MED_CHANNEL_V, 0) < MED_CHANNEL_VERSION) {
            // Android giữ cấu hình notification channel sau khi tạo, nên cần xóa channel cũ để áp dụng cấu hình mới.
            manager.deleteNotificationChannel(CHANNEL_ID);
            prefs.edit().putInt(KEY_MED_CHANNEL_V, MED_CHANNEL_VERSION).apply();
        }
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Báo thức nhắc uống thuốc đúng giờ của Vita");
        android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .build();
        channel.setSound(android.media.RingtoneManager.getDefaultUri(
                android.media.RingtoneManager.TYPE_NOTIFICATION), audioAttributes);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 250, 100, 250});
        manager.createNotificationChannel(channel);
    }

    public static void showMedicineNotification(Context context, Medicine medicine, String date, String time) {
        if (!canNotify(context)) return;
        ensureChannel(context);
        int notificationId = doseNotificationId(medicine.getId(), date, time);

        PendingIntent takenIntent = actionPendingIntent(context, medicine.getId(),
                NotificationActionReceiver.ACTION_TAKEN, date, time);
        PendingIntent skippedIntent = actionPendingIntent(context, medicine.getId(),
                NotificationActionReceiver.ACTION_SKIPPED, date, time);

        Intent openReminder = new Intent(context,
                vn.edu.tdmu.vita.activities.MainActivity.class);
        openReminder.putExtra(vn.edu.tdmu.vita.activities.MainActivity.EXTRA_TAB,
                vn.edu.tdmu.vita.activities.MainActivity.TAB_REMINDER);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) medicine.getId(),
                openReminder, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String dosage = medicine.getDosage() == null || medicine.getDosage().trim().isEmpty()
                ? "đúng liều" : medicine.getDosage();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_medicines)
                .setContentTitle(time + " • " + medicine.getName())
                .setContentText("Liều dùng: " + dosage + ". Bạn đã uống chưa?")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        "Liều dùng: " + dosage + ". Hãy uống thuốc đúng giờ nhé!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .setColor(0xFF00897B)
                .addAction(R.drawable.ic_check, "Đã uống", takenIntent)
                .addAction(R.drawable.ic_cancel, "Bỏ qua", skippedIntent);

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException revoked) {
            // Notification access can be revoked after canNotify().
            return;
        }
    }

    private static PendingIntent actionPendingIntent(Context context, long medicineId,
                                                     String action, String date, String time) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(action);
        intent.setData(android.net.Uri.parse("vita://dose/" + medicineId + "/" + date + "/" + time));
        intent.putExtra(vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver.EXTRA_DOSE_DATE, date);
        intent.putExtra(vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver.EXTRA_DOSE_TIME, time);
        intent.putExtra(NotificationActionReceiver.EXTRA_MEDICINE_ID, medicineId);
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static int doseNotificationId(long medicineId, String date, String time) {
        return (medicineId + "/" + date + "/" + time).hashCode();
    }

    public static void showWaterReminder(Context context, int notificationId) {
        if (!canNotify(context)) return;
        ensureWaterChannel(context);
        vn.edu.tdmu.vita.models.User user = new vn.edu.tdmu.vita.database.UserDao(context)
                .getUserById(new SessionManager(context).getUserId());
        if (user == null) return;
        int amount = user.getWaterAmount();

        Intent openApp = new Intent(context, vn.edu.tdmu.vita.activities.MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent drinked = new Intent(context,
                vn.edu.tdmu.vita.receivers.WaterReminderReceiver.class);
        drinked.setAction(vn.edu.tdmu.vita.receivers.WaterReminderReceiver.ACTION_DRINKED);
        drinked.putExtra(vn.edu.tdmu.vita.receivers.WaterReminderReceiver.EXTRA_USER_ID,
                new SessionManager(context).getUserId());
        drinked.putExtra(vn.edu.tdmu.vita.receivers.WaterReminderReceiver.EXTRA_AMOUNT, amount);
        PendingIntent drinkedIntent = PendingIntent.getBroadcast(context, 9002, drinked,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WATER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_water_drop)
                .setContentTitle(context.getString(R.string.water_reminder_title))
                .setContentText(context.getString(R.string.water_reminder_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setColor(0xFF00ACC1)
                .addAction(R.drawable.ic_water_drop,
                        context.getString(R.string.water_reminder_action, amount), drinkedIntent);

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException revoked) {
            // Notification access can be revoked after canNotify().
            return;
        }
    }

    public static boolean canNotify(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
                && (Build.VERSION.SDK_INT < 33 || androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED);
    }

    private static void ensureWaterChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null || manager.getNotificationChannel(WATER_CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(
                WATER_CHANNEL_ID, WATER_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.water_channel_description));
        manager.createNotificationChannel(channel);
    }
}
