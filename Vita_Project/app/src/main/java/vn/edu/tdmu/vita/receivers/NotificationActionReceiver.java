package vn.edu.tdmu.vita.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.SessionManager;

/**
 * Ghi vào medicine_logs khi bấm nút trên thông báo, sau đó lên lịch cho ngày kế tiếp.
 */
public class NotificationActionReceiver extends BroadcastReceiver {

    public static final String ACTION_TAKEN = "vn.edu.tdmu.vita.ACTION_MEDICINE_TAKEN";
    public static final String ACTION_SKIPPED = "vn.edu.tdmu.vita.ACTION_MEDICINE_SKIPPED";
    public static final String EXTRA_MEDICINE_ID = "extra_medicine_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        long medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, -1);
        if (medicineId <= 0) return;
        String action = intent.getAction();
        if (!ACTION_TAKEN.equals(action) && !ACTION_SKIPPED.equals(action)) return;
        String status = ACTION_SKIPPED.equals(action) ? "SKIPPED" : "TAKEN";

        MedicineDao dao = new MedicineDao(context);
        Medicine medicine = dao.getById(medicineId, new SessionManager(context).getUserId());
        if (medicine == null || !SessionManager.matchesUser(
                medicine.getUserId(), new SessionManager(context).getUserId())) return;
        String date = intent.getStringExtra(MedicineAlarmReceiver.EXTRA_DOSE_DATE);
        String time = intent.getStringExtra(MedicineAlarmReceiver.EXTRA_DOSE_TIME);
        if (date == null || time == null) return;
        if (!medicine.isActive()
                || !java.util.Objects.equals(medicine.getName(), intent.getStringExtra("medicine_name"))
                || !java.util.Objects.equals(medicine.getDosage(), intent.getStringExtra("medicine_dosage"))
                || !java.util.Objects.equals(medicine.getTime(), intent.getStringExtra("medicine_times"))
                || medicine.getDaysMask() != intent.getIntExtra("medicine_days", -1)
                || !java.util.Arrays.asList(medicine.getTime().split(",")).contains(time)) {
            NotificationManagerCompat.from(context).cancel(vn.edu.tdmu.vita.utils.NotificationHelper.doseNotificationId(medicineId, date, time));
            Toast.makeText(context, "Lịch thuốc đã thay đổi. Hãy mở ứng dụng để kiểm tra cữ thuốc.", Toast.LENGTH_LONG).show();
            return;
        }
        String medicineName = medicine.getName();
        try {
            if (dao.logMedicineStatus(medicineId, medicine.getUserId(), date, time, status,
                    DateTimeUtils.getCurrentTimestamp()) <= 0) return;
        } catch (android.database.sqlite.SQLiteException error) {
            Toast.makeText(context, "Chưa lưu được cữ thuốc. Hãy thử lại trong ứng dụng.", Toast.LENGTH_LONG).show();
            return;
        }

        NotificationManagerCompat.from(context).cancel(vn.edu.tdmu.vita.utils.NotificationHelper.doseNotificationId(medicineId, date, time));

        String message = "TAKEN".equals(status)
                ? "Vita ghi nhận bạn đã uống " + medicineName + ". Chúc bạn mau khỏe!"
                : "Đã ghi nhận bỏ qua cữ thuốc này. Hãy chú ý giữ gìn sức khỏe nhé.";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
