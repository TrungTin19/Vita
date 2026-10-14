package vn.edu.tdmu.vita.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.utils.DateTimeUtils;

/**
 * Ghi medicine_logs khi bấm nút trên notification rồi đặt lại lịch ngày kế tiếp.
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
        String status = ACTION_SKIPPED.equals(action) ? "SKIPPED" : "TAKEN";

        MedicineDao dao = new MedicineDao(context);
        Medicine medicine = dao.getById(medicineId);
        String medicineName = medicine != null ? medicine.getName() : "thuốc";
        dao.logMedicineStatus(medicineId, DateTimeUtils.getTodayDate(), status,
                DateTimeUtils.getCurrentTimestamp());

        NotificationManagerCompat.from(context).cancel((int) medicineId);
        if (medicine != null) MedicineAlarmReceiver.schedule(context, medicine);

        String message = "TAKEN".equals(status)
                ? "Vita ghi nhận bạn đã uống " + medicineName + ". Chúc bạn mau khỏe!"
                : "Đã ghi nhận bỏ qua cữ thuốc này. Hãy chú ý giữ gìn sức khỏe nhé.";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
