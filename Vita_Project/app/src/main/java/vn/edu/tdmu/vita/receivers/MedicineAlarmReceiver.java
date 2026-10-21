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
import vn.edu.tdmu.vita.utils.SessionManager;
import vn.edu.tdmu.vita.utils.NotificationHelper;

/**
 * Bắn notification khi tới giờ + đặt lại báo thức cho cữ kế tiếp.
 */
public class MedicineAlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_MEDICINE_ID = "extra_medicine_id";

    public static final String EXTRA_DOSE_DATE = "dose_date";
    public static final String EXTRA_DOSE_TIME = "dose_time";

    @Override
    public void onReceive(Context context, Intent intent) {
        long medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, -1);
        if (medicineId <= 0) return;
        MedicineDao dao = new MedicineDao(context);
        Medicine medicine = dao.getById(medicineId, new SessionManager(context).getUserId());
        if (medicine == null || !medicine.isActive()
                || !SessionManager.matchesUser(medicine.getUserId(), new SessionManager(context).getUserId())) return;
        String date = intent.getStringExtra(EXTRA_DOSE_DATE);
        String time = intent.getStringExtra(EXTRA_DOSE_TIME);
        if (date == null) date = vn.edu.tdmu.vita.utils.DateTimeUtils.getTodayDate();
        if (time == null) time = medicine.getTime().split(",")[0];
        if (!java.util.Arrays.asList(medicine.getTime().split(",")).contains(time)) return;
        if (dao.getLogForDose(medicineId, medicine.getUserId(), date, time) == null) {
            NotificationHelper.showMedicineNotification(context, medicine, date, time);
        }
        scheduleDose(context, medicine, time);
    }

    public static void schedule(Context context, Medicine medicine) {
        if (medicine == null || !medicine.isActive()
                || !SessionManager.matchesUser(medicine.getUserId(), new SessionManager(context).getUserId())) return;
        cancelLegacy(context, medicine.getId());
        for (String time : medicine.getTime().split(",")) {
            PendingIntent existing = PendingIntent.getBroadcast(context,0,alarmIntent(context,medicine.getId(),time),
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (existing == null) scheduleDose(context, medicine, time);
        }
    }

    private static void scheduleDose(Context context, Medicine medicine, String time) {
        long triggerAt = MedicineSchedule.computeNextTriggerMillis(time, medicine.getDaysMask(), System.currentTimeMillis());
        if (triggerAt < 0) {
            cancel(context, medicine);
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = alarmIntent(context, medicine.getId(), time);
        intent.putExtra(EXTRA_MEDICINE_ID, medicine.getId());
        java.util.Date trigger = new java.util.Date(triggerAt);
        intent.putExtra(EXTRA_DOSE_DATE, new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(trigger));
        intent.putExtra(EXTRA_DOSE_TIME, new java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(trigger));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent,
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

    public static void cancel(Context context, Medicine medicine) {
        if (medicine == null) return;
        cancelLegacy(context, medicine.getId());
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        for (String time : medicine.getTime().split(",")) {
            Intent intent = alarmIntent(context, medicine.getId(), time);
            PendingIntent pending = PendingIntent.getBroadcast(context,0,intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pending != null) { manager.cancel(pending); pending.cancel(); }
        }
    }

    private static Intent alarmIntent(Context context, long medicineId, String time) {
        return new Intent(context, MedicineAlarmReceiver.class)
                .setData(android.net.Uri.parse("vita://alarm/" + medicineId + "/" + time));
    }

    private static void cancelLegacy(Context context, long medicineId) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        PendingIntent pending = PendingIntent.getBroadcast(context,(int)medicineId,
                new Intent(context, MedicineAlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pending != null) { manager.cancel(pending); pending.cancel(); }
    }
}
