package vn.edu.tdmu.vita.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

    private NotificationHelper() {}

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) return;
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
}
