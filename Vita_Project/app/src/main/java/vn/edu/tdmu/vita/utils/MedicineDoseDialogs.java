package vn.edu.tdmu.vita.utils;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.FragmentActivity;
import java.util.List;
import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.MedicineLog;

public final class MedicineDoseDialogs {
    private MedicineDoseDialogs() {}

    public static int label(MedicineDoseState state) {
        switch (state) {
            case TAKEN: return R.string.dose_taken;
            case SKIPPED: return R.string.dose_skipped;
            case UNCONFIRMED: return R.string.dose_unconfirmed;
            default: return R.string.dose_upcoming;
        }
    }

    public static void edit(FragmentActivity activity, long owner, Medicine medicine, String date, String time, Runnable changed) {
        if (!SessionManager.matchesUser(owner, new SessionManager(activity).getUserId())) return;
        MedicineLog log = new MedicineDao(activity).getLogForDose(medicine.getId(), owner, date, time);
        boolean taken = log != null && "TAKEN".equals(log.getStatus());
        String nextStatus = taken ? "SKIPPED" : "TAKEN";
        String title = log == null ? activity.getString(R.string.figma_dose_title, time, medicine.getName())
                : activity.getString(R.string.figma_dose_edit_title);
        String message = log == null ? activity.getString(R.string.figma_dose_pending)
                : activity.getString(R.string.figma_dose_edit_message, activity.getString(taken ? R.string.dose_taken : R.string.dose_skipped)
                        + " · " + (log.getLoggedAt() == null ? "—" : log.getLoggedAt()));
        AppCompatDialog dialog = VitaDialog.create(activity, title, message, log == null ? R.string.dose_taken
                : taken ? R.string.figma_dose_change_skipped : R.string.figma_dose_change_taken, false);
        dialog.findViewById(android.R.id.button1).setOnClickListener(v -> {
            save(activity, owner, medicine.getId(), date, time, nextStatus, changed); dialog.dismiss();
        });
        android.widget.TextView extra = dialog.findViewById(android.R.id.button3);
        extra.setVisibility(android.view.View.VISIBLE);
        extra.setText(log == null ? R.string.dose_skipped : R.string.figma_dose_clear);
        extra.setOnClickListener(v -> {
            save(activity, owner, medicine.getId(), date, time, log == null ? "SKIPPED" : null, changed); dialog.dismiss();
        });
        dialog.show();
    }

    public static void save(FragmentActivity activity, long owner, long medicineId, String date, String time, String status, Runnable changed) {
        if (activity.isFinishing() || !SessionManager.matchesUser(owner, new SessionManager(activity).getUserId())) return;
        MedicineDao dao = new MedicineDao(activity);
        if (dao.getById(medicineId, owner) == null) return;
        try {
            boolean saved;
            if (status == null) {
                saved = dao.getLogForDose(medicineId, owner, date, time) == null
                        || dao.clearDoseStatus(medicineId, owner, date, time);
            } else saved = dao.logMedicineStatus(medicineId, owner, date, time, status, DateTimeUtils.getCurrentTimestamp()) > 0;
            if (!saved) { Toast.makeText(activity, R.string.add_med_error_update, Toast.LENGTH_SHORT).show(); return; }
            androidx.core.app.NotificationManagerCompat.from(activity).cancel(NotificationHelper.doseNotificationId(medicineId, date, time));
            if (changed != null) changed.run();
            Toast.makeText(activity, R.string.dose_saved, Toast.LENGTH_SHORT).show();
        } catch (android.database.sqlite.SQLiteException | IllegalArgumentException error) {
            Toast.makeText(activity, R.string.add_med_error_update, Toast.LENGTH_SHORT).show();
        }
    }

    public static void history(FragmentActivity activity, long owner, Medicine medicine, Runnable changed) {
        if (!SessionManager.matchesUser(owner, new SessionManager(activity).getUserId())) return;
        List<MedicineLog> logs = new MedicineDao(activity).getHistory(medicine.getId(), owner);
        android.app.Dialog history = new android.app.Dialog(activity); history.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        android.widget.LinearLayout root = new android.widget.LinearLayout(activity); root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setBackgroundColor(androidx.core.content.ContextCompat.getColor(activity, R.color.vita_bg));
        int gutter = Math.round(24 * activity.getResources().getDisplayMetrics().density);
        com.google.android.material.appbar.MaterialToolbar toolbar = (com.google.android.material.appbar.MaterialToolbar)
                android.view.LayoutInflater.from(activity).inflate(R.layout.view_page_toolbar, root, false);
        ((android.widget.TextView) toolbar.findViewById(R.id.tv_page_title)).setText(medicine.getName());
        toolbar.setNavigationOnClickListener(v -> history.dismiss()); root.addView(toolbar);
        android.widget.ScrollView scroll = new android.widget.ScrollView(activity);
        root.addView(scroll, new android.widget.LinearLayout.LayoutParams(-1, 0, 1));
        android.widget.LinearLayout body = new android.widget.LinearLayout(activity); body.setOrientation(android.widget.LinearLayout.VERTICAL); body.setPadding(gutter, gutter / 3, gutter, gutter); scroll.addView(body);
        addText(activity, body, activity.getString(medicine.isActive() ? R.string.figma_reminder_on : R.string.figma_reminder_off), 11, R.color.vita_teal, false);
        android.widget.TextView reminder = (android.widget.TextView) body.getChildAt(0);
        android.graphics.drawable.GradientDrawable reminderPill = new android.graphics.drawable.GradientDrawable(); reminderPill.setColor(androidx.core.content.ContextCompat.getColor(activity, R.color.vita_mint)); reminderPill.setCornerRadius(gutter / 2);
        reminder.setBackground(reminderPill); reminder.setPadding(gutter / 3, gutter / 3, gutter / 3, gutter / 3); reminder.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-2, -2));
        addText(activity, body, medicine.getDosage() == null ? "" : medicine.getDosage(), 22, R.color.vita_text_primary, true);
        addText(activity, body, medicine.getTime().replace(",", " · ") + "\n" + vn.edu.tdmu.vita.adapters.MedicineAdapter.formatDaysMask(activity, medicine.getDaysMask()), 16, R.color.vita_text_secondary, false);
        com.google.android.material.button.MaterialButton schedule = new com.google.android.material.button.MaterialButton(activity);
        schedule.setInsetTop(0); schedule.setInsetBottom(0); schedule.setTextSize(16);
        schedule.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(activity, R.color.vita_teal))); schedule.setTextColor(android.graphics.Color.WHITE);
        schedule.setText(R.string.figma_edit_schedule); schedule.setAllCaps(false); schedule.setCornerRadius(gutter / 2);
        body.addView(schedule, new android.widget.LinearLayout.LayoutParams(-1, Math.round(52 * activity.getResources().getDisplayMetrics().density)));
        schedule.setOnClickListener(v -> { history.dismiss(); activity.startActivity(new android.content.Intent(activity, vn.edu.tdmu.vita.activities.AddMedicineActivity.class).putExtra(vn.edu.tdmu.vita.activities.AddMedicineActivity.EXTRA_MEDICINE_ID, medicine.getId())); });
        addText(activity, body, activity.getString(R.string.figma_dose_history), 20, R.color.vita_text_primary, true);
        if (logs.isEmpty()) addText(activity, body, activity.getString(R.string.med_history_empty), 14, R.color.vita_text_secondary, false);
        for (MedicineLog log : logs) {
            android.widget.LinearLayout card = new android.widget.LinearLayout(activity); card.setOrientation(android.widget.LinearLayout.VERTICAL); card.setPadding(gutter * 5 / 6, 0, gutter * 5 / 6, gutter / 3);
            android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable(); background.setColor(android.graphics.Color.WHITE); background.setCornerRadius(gutter * 5 / 6); card.setBackground(background);
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(-1, -2); params.bottomMargin = gutter * 2 / 3; body.addView(card, params);
            addText(activity, card, DateTimeUtils.formatDisplayDate(log.getDate()) + " · " + log.getScheduledTime(), 16, R.color.vita_text_primary, true);
            addText(activity, card, activity.getString("TAKEN".equals(log.getStatus()) ? R.string.dose_taken : R.string.dose_skipped), 11, R.color.vita_teal, false);
            android.widget.TextView status = (android.widget.TextView) card.getChildAt(1);
            status.setBackgroundResource("TAKEN".equals(log.getStatus()) ? R.drawable.bg_badge_good : R.drawable.bg_badge_danger);
            status.setPadding(gutter / 3, gutter / 6, gutter / 3, gutter / 6);
            status.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-2, -2));
            String name = log.getMedicineNameSnapshot() == null ? medicine.getName() : log.getMedicineNameSnapshot();
            String dose = log.getDosageSnapshot() == null ? "" : log.getDosageSnapshot();
            addText(activity, card, name + " · " + dose + "\n" + activity.getString(R.string.dose_recorded_at, log.getLoggedAt() == null ? "—" : log.getLoggedAt())
                    + (log.isSnapshotInherited() ? "\n" + activity.getString(R.string.dose_legacy_snapshot) : ""), 14, R.color.vita_text_secondary, false);
            card.setFocusable(true); card.setOnClickListener(v -> edit(activity, owner, medicine, log.getDate(), log.getScheduledTime(), () -> { if (changed != null) changed.run(); history.dismiss(); history(activity, owner, medicine, changed); }));
        }
        addText(activity, body, activity.getString(R.string.figma_history_snapshot_help), 14, R.color.vita_text_secondary, false);
        com.google.android.material.button.MaterialButton delete = new com.google.android.material.button.MaterialButton(activity);
        delete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(activity, R.color.vita_status_danger_bg)));
        delete.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.vita_status_danger));
        delete.setCornerRadius(gutter / 2); delete.setInsetTop(0); delete.setInsetBottom(0);
        delete.setMinHeight(Math.round(52 * activity.getResources().getDisplayMetrics().density));
        delete.setText(R.string.figma_delete_medicine_action);
        body.addView(delete, new android.widget.LinearLayout.LayoutParams(-1, -2));
        delete.setOnClickListener(v -> delete(activity, owner, medicine, () -> {
            history.dismiss(); if (changed != null) changed.run();
        }));
        history.setContentView(root); history.show();
        if (history.getWindow() != null) {
            history.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            history.getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(activity, R.color.vita_bg));
            history.getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(activity, R.color.vita_surface));
            history.getWindow().setBackgroundDrawableResource(R.color.vita_bg);
            history.getWindow().getDecorView().setPadding(0, 0, 0, 0); history.getWindow().setLayout(-1, -1);
        }
    }

    public static void delete(FragmentActivity activity, long owner, Medicine medicine, Runnable changed) {
        AppCompatDialog dialog = VitaDialog.create(activity, activity.getString(R.string.figma_delete_medicine_title),
                activity.getString(R.string.figma_delete_medicine_message, medicine.getName()), R.string.figma_delete_medicine_action, true);
        dialog.findViewById(android.R.id.button1).setOnClickListener(v -> {
            if (!SessionManager.matchesUser(owner, new SessionManager(activity).getUserId())) { dialog.dismiss(); return; }
            vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver.cancel(activity, medicine);
            boolean deleted = new MedicineDao(activity).delete(medicine.getId(), owner);
            if (deleted) {
                if (changed != null) changed.run();
                Toast.makeText(activity, R.string.reminder_delete_success, Toast.LENGTH_SHORT).show();
            } else {
                vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver.schedule(activity, medicine);
                Toast.makeText(activity, R.string.reminder_delete_error, Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    private static void addText(FragmentActivity activity, android.widget.LinearLayout parent, String value, int size, int color, boolean bold) {
        android.widget.TextView text = new android.widget.TextView(activity); text.setText(value); text.setTextSize(size);
        text.setTextColor(androidx.core.content.ContextCompat.getColor(activity, color)); text.setIncludeFontPadding(false);
        if (bold) text.setTypeface(null, android.graphics.Typeface.BOLD);
        int space = Math.round(12 * activity.getResources().getDisplayMetrics().density); text.setPadding(0, space, 0, space); parent.addView(text);
    }
}
