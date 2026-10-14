package vn.edu.tdmu.vita.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.utils.MedicineSchedule;
import vn.edu.tdmu.vita.utils.NotificationHelper;

/**
 * Bắn notification khi tới giờ + đặt lại báo thức cho cữ kế tiếp.
 */
public class MedicineAlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_MEDICINE_ID = "extra_medicine_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        long medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, -1);
        if (medicineId <= 0) return;
        MedicineDao dao = new MedicineDao(context);
        Medicine medicine = dao.getById(medicineId);
        if (medicine == null || !medicine.isActive()) return;
        NotificationHelper.showMedicineNotification(context, medicine);
        schedule(context, medicine); // one-shot nên đặt lại ngay, quẹt bỏ cũng không mất lịch
    }

    public static void schedule(Context context, Medicine medicine) {
        if (medicine == null || !medicine.isActive()) return;
        long triggerAt = MedicineSchedule.computeNextTriggerMillis(
                medicine.getTime(), medicine.getDaysMask(), System.currentTimeMillis());
        if (triggerAt < 0) {
            cancel(context, medicine.getId());
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = new Intent(context, MedicineAlarmReceiver.class);
        intent.putExtra(EXTRA_MEDICINE_ID, medicine.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (int) medicine.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            }
        } catch (SecurityException e) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    public static void cancel(Context context, long medicineId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = new Intent(context, MedicineAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (int) medicineId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
