package vn.edu.tdmu.vita.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.models.Medicine;

/**
 * Khôi phục báo thức sau khi khởi động lại máy (các báo thức trong RAM bị xóa).
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean clockChanged = Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action);
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action) && !clockChanged) return;
        MedicineDao dao = new MedicineDao(context);
        List<Medicine> active = dao.getActiveMedicines(new vn.edu.tdmu.vita.utils.SessionManager(context).getUserId());
        for (Medicine medicine : active) {
            if (clockChanged) MedicineAlarmReceiver.cancel(context, medicine);
            MedicineAlarmReceiver.schedule(context, medicine);
        }
        if (clockChanged) WaterReminderReceiver.cancel(context);
        WaterReminderReceiver.schedule(context);
    }
}
