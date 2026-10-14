package vn.edu.tdmu.vita.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.models.Medicine;

/**
 * Khôi phục báo thức sau reboot (Alarm trong RAM bị xóa sạch).
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        MedicineDao dao = new MedicineDao(context);
        List<Medicine> active = dao.getAllActiveMedicinesAcrossAllUsers();
        for (Medicine medicine : active) {
            MedicineAlarmReceiver.schedule(context, medicine);
        }
    }
}
