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
    private static final int MED_CHANNEL_VERSION = 2;

    public static final String WATER_CHANNEL_ID = "vita_water_channel";
    private static final String WATER_CHANNEL_NAME = "Nhắc nhở uống nước Vita";

    private NotificationHelper() {}

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_CHANNEL, Context.MODE_PRIVATE);
        if (prefs.getInt(KEY_MED_CHANNEL_V, 0) < MED_CHANNEL_VERSION) {
            // ponytail: channel tạo 1 lần và hệ thống giữ mãi; xóa để bản cũ nhận cấu hình mới.
            manager.deleteNotificationChannel(CHANNEL_ID);
            prefs.edit().putInt(KEY_MED_CHANNEL_V, MED_CHANNEL_VERSION).apply();
        }
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Báo thức nhắc uống thuốc đúng giờ của Vita");
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 250, 100, 250});
        manager.createNotificationChannel(channel);
    }

    public static void showMedicineNotification(Context context, Medicine medicine) {
        ensureChannel(context);
        int notificationId = (int) medicine.getId();

        PendingIntent takenIntent = actionPendingIntent(context, medicine.getId(),
                NotificationActionReceiver.ACTION_TAKEN, (int) (medicine.getId() * 100 + 1));
        PendingIntent skippedIntent = actionPendingIntent(context, medicine.getId(),
                NotificationActionReceiver.ACTION_SKIPPED, (int) (medicine.getId() * 100 + 2));

        String dosage = medicine.getDosage() == null || medicine.getDosage().trim().isEmpty()
                ? "đúng liều" : medicine.getDosage();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_medicines)
                .setContentTitle("Đã đến giờ uống thuốc: " + medicine.getName())
                .setContentText("Liều dùng: " + dosage + ". Bạn đã uống chưa?")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        "Liều dùng: " + dosage + ". Hãy uống thuốc đúng giờ nhé!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(false)
                .setOngoing(false)
                .setColor(0xFF00897B)
                .addAction(R.drawable.ic_check, "Đã uống", takenIntent)
                .addAction(R.drawable.ic_cancel, "Bỏ qua", skippedIntent);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private static PendingIntent actionPendingIntent(Context context, long medicineId,
                                                     String action, int requestCode) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(action);
        intent.putExtra(NotificationActionReceiver.EXTRA_MEDICINE_ID, medicineId);
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void showWaterReminder(Context context, int notificationId) {
        ensureWaterChannel(context);

        Intent openApp = new Intent(context, vn.edu.tdmu.vita.activities.MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent drinked = new Intent(context,
                vn.edu.tdmu.vita.receivers.WaterReminderReceiver.class);
        drinked.setAction(vn.edu.tdmu.vita.receivers.WaterReminderReceiver.ACTION_DRINKED);
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
                        context.getString(R.string.water_reminder_action), drinkedIntent);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private static void ensureWaterChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null || manager.getNotificationChannel(WATER_CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(
                WATER_CHANNEL_ID, WATER_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Nhắc uống nước định kỳ mỗi 2 giờ của Vita");
        manager.createNotificationChannel(channel);
    }
}
