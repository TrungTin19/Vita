package vn.edu.tdmu.vita.fragments;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.activities.AddMedicineActivity;
import vn.edu.tdmu.vita.adapters.MedicineAdapter;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.database.WaterDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.HydrationGoal;
import vn.edu.tdmu.vita.utils.SessionManager;
import vn.edu.tdmu.vita.utils.NotificationHelper;

public class ReminderFragment extends Fragment {

    private int quickAddMl = 250;

    private TextView tvWaterProgress;
    private ProgressBar pbWaterProgress;
    private MaterialButton btnQuickAddWater;
    private RecyclerView rvMedicines;
    private LinearLayout layoutMedicineEmpty;
    private MaterialButton fabAddMedicine;

    private MedicineDao medicineDao;
    private WaterDao waterDao;
    private UserDao userDao;
    private SessionManager sessionManager;

    private List<Medicine> medicines;
    private MedicineAdapter adapter;

    private int waterGoalMl = 2000;
    private static boolean exactAlarmPromptShown = false;

    private ActivityResultLauncher<Intent> medicineFormLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        medicineFormLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> loadMedicines()
        );
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> updateNotificationState()
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        medicineDao = new MedicineDao(requireContext());
        waterDao = new WaterDao(requireContext());
        userDao = new UserDao(requireContext());

        initViews(view);
        setupRecyclerView();
        setupListeners();
        ((com.google.android.material.button.MaterialButtonToggleGroup) view.findViewById(R.id.tabs_reminder))
                .addOnButtonCheckedListener((group, checkedId, checked) -> { if (checked) showMedicineTab(checkedId == R.id.tab_medicine_list); });
        showMedicineTab(false);
        vn.edu.tdmu.vita.receivers.WaterReminderReceiver.schedule(requireContext());
        loadWaterProgress(false);
        loadMedicines();
        updateNotificationState();
        requestNotificationPermissionIfNeeded();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWaterProgress(false);
        loadMedicines();
        promptExactAlarmIfNeeded();
        updateNotificationState();
    }

    private void promptExactAlarmIfNeeded() {
        if (exactAlarmPromptShown) return;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) return;
        android.app.AlarmManager alarmManager = requireContext()
                .getSystemService(android.app.AlarmManager.class);
        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()
                && getView() != null) {
            exactAlarmPromptShown = true;
            Snackbar.make(getView(), getString(R.string.exact_alarm_needed),
                            Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.exact_alarm_open_settings), v -> {
                        Intent intent = new Intent(
                                android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intent);
                    })
                    .show();
        }
    }

    private void initViews(View view) {
        // ToggleGroup giữ nhãn trên một dòng khi thêm các nút lựa chọn.
        for (int id : new int[]{R.id.tab_today_doses, R.id.tab_medicine_list}) {
            TextView tab = view.findViewById(id);
            tab.setSingleLine(false);
            tab.setMaxLines(3);
            tab.setEllipsize(null);
        }
        tvWaterProgress = view.findViewById(R.id.tv_water_progress_text);
        pbWaterProgress = view.findViewById(R.id.pb_water_progress);
        btnQuickAddWater = view.findViewById(R.id.btn_quick_add_water);
        rvMedicines = view.findViewById(R.id.rv_medicines);
        layoutMedicineEmpty = view.findViewById(R.id.layout_medicine_empty);
        fabAddMedicine = view.findViewById(R.id.fab_add_medicine);
    }

    private void setupRecyclerView() {
        medicines = new ArrayList<>();
        adapter = new MedicineAdapter(medicines);
        adapter.setOnSwitchChangeListener(this::handleSwitchChanged);
        adapter.setHistoryListener(medicine -> vn.edu.tdmu.vita.utils.MedicineDoseDialogs.history(
                requireActivity(), sessionManager.getUserId(), medicine, this::loadMedicines));
        rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMedicines.setAdapter(adapter);
        registerForContextMenu(rvMedicines);
    }

    private void setupListeners() {
        requireView().findViewById(R.id.btn_reminder_water_journal).setOnClickListener(v ->
                requireView().findViewById(R.id.tv_water_progress_text).performClick());
        requireView().findViewById(R.id.btn_reminder_profile).setOnClickListener(v -> startActivity(new Intent(requireContext(), vn.edu.tdmu.vita.activities.ProfileActivity.class)));
        requireView().findViewById(R.id.btn_water_settings).setOnClickListener(v -> startActivity(new Intent(requireContext(), vn.edu.tdmu.vita.activities.EditProfileActivity.class)
                .putExtra(vn.edu.tdmu.vita.activities.EditProfileActivity.EXTRA_WATER_SETTINGS_ONLY, true)));
        requireView().findViewById(R.id.tv_water_progress_text).setOnClickListener(v ->
                vn.edu.tdmu.vita.utils.WaterJournalDialog.show(requireActivity(), sessionManager.getUserId(), DateTimeUtils.getTodayDate(), () -> loadWaterProgress(true)));
        requireView().findViewById(R.id.btn_water_other_amount).setOnClickListener(v ->
                vn.edu.tdmu.vita.utils.WaterJournalDialog.showAddAmount(requireActivity(), sessionManager.getUserId(), () -> loadWaterProgress(true)));
        btnQuickAddWater.setOnClickListener(v -> handleQuickAddWater());
        fabAddMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddMedicineActivity.class);
            medicineFormLauncher.launch(intent);
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("vita_permissions", 0);
            if (!prefs.getBoolean("notification_requested", false)) {
                prefs.edit().putBoolean("notification_requested", true).apply();
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void updateNotificationState() {
        if (getView() == null) return;
        View banner = getView().findViewById(R.id.layout_notification_permission);
        boolean notifications = NotificationHelper.canNotify(requireContext());
        android.app.AlarmManager alarms = requireContext().getSystemService(android.app.AlarmManager.class);
        boolean exact = Build.VERSION.SDK_INT < 31 || (alarms != null && alarms.canScheduleExactAlarms());
        banner.setVisibility(notifications && exact ? View.GONE : View.VISIBLE);
        ((TextView) getView().findViewById(R.id.tv_notification_permission)).setText(
                notifications ? R.string.alarm_inexact_help : R.string.reminder_notifications_disabled);
        getView().findViewById(R.id.btn_notification_settings).setOnClickListener(v -> {
            Intent settings;
            if (notifications && !exact) {
                startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        android.net.Uri.parse("package:" + requireContext().getPackageName()))); return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                settings.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
            } else {
                settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.parse("package:" + requireContext().getPackageName()));
            }
            startActivity(settings);
        });
    }

    public void loadWaterProgress(boolean animate) {
        long userId = sessionManager.getUserId();
        if (userId <= 0) return;

        User user = userDao.getUserById(userId);
        waterGoalMl = HydrationGoal.forUserGoal(user != null ? user.getWaterGoalMl() : 0);
        quickAddMl = user != null ? user.getWaterAmount() : 250;
        btnQuickAddWater.setText(getString(R.string.reminder_btn_add_water, quickAddMl));
        TextView adjustment = requireView().findViewById(R.id.tv_water_adjustment);
        adjustment.setText(getString(R.string.water_remaining, Math.max(0, waterGoalMl - waterDao.getDailyTotal(userId, DateTimeUtils.getTodayDate()))));

        int total = waterDao.getDailyTotal(userId, DateTimeUtils.getTodayDate());
        int oldProgress = pbWaterProgress.getProgress();
        pbWaterProgress.setMax(waterGoalMl);
        tvWaterProgress.setText(String.format(Locale.getDefault(),
                getString(R.string.reminder_water_progress_format),
                total, waterGoalMl, waterGoalMl > 0 ? Math.round(total * 100f / waterGoalMl) : 0));

        int target = Math.min(total, waterGoalMl);
        if (animate) {
            ObjectAnimator animator = ObjectAnimator.ofInt(pbWaterProgress, "progress", oldProgress, target);
            animator.setDuration(400);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        } else {
            pbWaterProgress.setProgress(target);
        }
    }

    private void handleQuickAddWater() {
        vn.edu.tdmu.vita.utils.WaterJournalDialog.addQuickWater(requireActivity(), sessionManager.getUserId(), () -> loadWaterProgress(true));
    }

    public void refreshToday() {
        if (getView() == null) return;
        loadWaterProgress(false);
        loadMedicines();
    }

    private void loadMedicines() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) return;

        medicines.clear();
        medicines.addAll(medicineDao.getAllByUserId(userId));
        ((TextView) requireView().findViewById(R.id.tv_reminder_today_date)).setText(new java.text.SimpleDateFormat("dd/MM", Locale.getDefault()).format(new java.util.Date()));
        renderDoseGroups(userId);
        adapter.notifyDataSetChanged();

        showMedicineTab(((com.google.android.material.button.MaterialButtonToggleGroup) requireView().findViewById(R.id.tabs_reminder))
                .getCheckedButtonId() == R.id.tab_medicine_list);
    }

    private void showMedicineTab(boolean list) {
        if (getView() == null || medicines == null) return;
        requireView().findViewById(R.id.layout_today_doses).setVisibility(list ? View.GONE : View.VISIBLE);
        requireView().findViewById(R.id.layout_today_heading).setVisibility(list ? View.GONE : View.VISIBLE);
        requireView().findViewById(R.id.tv_dose_help).setVisibility(list ? View.GONE : View.VISIBLE);
        requireView().findViewById(R.id.tv_medicine_list_heading).setVisibility(View.GONE);
        fabAddMedicine.setVisibility(list ? View.VISIBLE : View.GONE);
        requireView().findViewById(R.id.tv_reminder_list_help).setVisibility(list ? View.VISIBLE : View.GONE);
        ((TextView) requireView().findViewById(R.id.tv_reminder_subtitle)).setText(list ? R.string.figma_medicine_list_subtitle : R.string.figma_reminder_subtitle);
        requireView().findViewById(R.id.cv_water_card).setVisibility(list ? View.GONE : View.VISIBLE);
        if (!list) {
            rvMedicines.setVisibility(View.GONE);
            layoutMedicineEmpty.setVisibility(View.GONE);
        } else if (medicines.isEmpty()) {
            rvMedicines.setVisibility(View.GONE);
            layoutMedicineEmpty.setVisibility(View.VISIBLE);
        } else {
            rvMedicines.setVisibility(View.VISIBLE);
            layoutMedicineEmpty.setVisibility(View.GONE);
        }
    }

    private static final class DoseRow {
        final Medicine medicine;
        final String time;
        final vn.edu.tdmu.vita.models.MedicineLog log;
        DoseRow(Medicine medicine, String time, vn.edu.tdmu.vita.models.MedicineLog log) {
            this.medicine = medicine; this.time = time; this.log = log;
        }
    }

    private void renderDoseGroups(long userId) {
        android.widget.LinearLayout parent = requireView().findViewById(R.id.layout_today_doses);
        parent.removeAllViews();
        String today = DateTimeUtils.getTodayDate();
        java.util.Calendar now = java.util.Calendar.getInstance();
        int bit = (now.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7;
        java.util.List<DoseRow> doses = new java.util.ArrayList<>();
        for (Medicine medicine : medicines) {
            java.util.Map<String, vn.edu.tdmu.vita.models.MedicineLog> logs = new java.util.HashMap<>();
            for (vn.edu.tdmu.vita.models.MedicineLog log : medicineDao.getLogsForDay(medicine.getId(), userId, today))
                logs.putIfAbsent(log.getScheduledTime(), log);
            java.util.Set<String> times = new java.util.TreeSet<>(logs.keySet());
            if ((medicine.getDaysMask() & (1 << bit)) != 0) java.util.Collections.addAll(times, medicine.getTime().split(","));
            for (String time : times) doses.add(new DoseRow(medicine, time, logs.get(time)));
        }
        doses.sort(java.util.Comparator.comparing(row -> row.time));
        for (DoseRow dose : doses) {
            vn.edu.tdmu.vita.utils.MedicineDoseState group = vn.edu.tdmu.vita.utils.MedicineDoseState.classify(today, dose.time,
                    dose.log == null ? null : dose.log.getStatus(), now.getTimeInMillis());
            View row = getLayoutInflater().inflate(R.layout.item_medicine_dose, parent, false);
            if (getResources().getConfiguration().fontScale > 1.3f) {
                android.widget.LinearLayout heading = (android.widget.LinearLayout) row.findViewById(R.id.tv_dose_time).getParent();
                heading.setOrientation(android.widget.LinearLayout.VERTICAL);
                android.widget.LinearLayout.LayoutParams nameParams = (android.widget.LinearLayout.LayoutParams) row.findViewById(R.id.tv_dose_name).getLayoutParams();
                nameParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                nameParams.weight = 0;
            }
            String name = dose.log != null && dose.log.getMedicineNameSnapshot() != null ? dose.log.getMedicineNameSnapshot() : dose.medicine.getName();
            String dosage = dose.log == null ? dose.medicine.getDosage() : dose.log.getDosageSnapshot();
            ((TextView) row.findViewById(R.id.tv_dose_time)).setText(dose.time);
            ((TextView) row.findViewById(R.id.tv_dose_name)).setText(name);
            ((TextView) row.findViewById(R.id.tv_dose_dosage)).setText(getString(R.string.figma_dose_instructions, dosage == null ? "" : dosage));
            TextView status = row.findViewById(R.id.tv_med_item_status);
            String recorded = dose.log == null ? null : dose.log.getLoggedAt();
            status.setText(group == vn.edu.tdmu.vita.utils.MedicineDoseState.TAKEN && recorded != null && recorded.length() >= 16
                    ? getString(R.string.figma_dose_taken_at, recorded.substring(11, 16))
                    : getString(vn.edu.tdmu.vita.utils.MedicineDoseDialogs.label(group)));
            android.graphics.drawable.GradientDrawable pill = new android.graphics.drawable.GradientDrawable();
            pill.setColor(ContextCompat.getColor(requireContext(), group == vn.edu.tdmu.vita.utils.MedicineDoseState.UNCONFIRMED
                    ? R.color.vita_status_warning_bg : group == vn.edu.tdmu.vita.utils.MedicineDoseState.UPCOMING ? R.color.vita_water_bg : group == vn.edu.tdmu.vita.utils.MedicineDoseState.SKIPPED ? R.color.vita_soft : R.color.vita_mint));
            pill.setCornerRadius(12 * getResources().getDisplayMetrics().density); status.setBackground(pill);
            status.setTextColor(ContextCompat.getColor(requireContext(), group == vn.edu.tdmu.vita.utils.MedicineDoseState.UNCONFIRMED ? R.color.vita_status_warning : group == vn.edu.tdmu.vita.utils.MedicineDoseState.UPCOMING ? R.color.vita_water : group == vn.edu.tdmu.vita.utils.MedicineDoseState.SKIPPED ? R.color.vita_text_secondary : R.color.vita_teal));
            row.setOnClickListener(v -> vn.edu.tdmu.vita.utils.MedicineDoseDialogs.edit(requireActivity(), userId, dose.medicine, today, dose.time, this::loadMedicines));
            parent.addView(row);
        }
        if (doses.isEmpty()) {
            TextView empty = new TextView(requireContext()); empty.setText(R.string.dose_today_empty); empty.setTextSize(16); parent.addView(empty);
        }
    }

    private void handleSwitchChanged(Medicine medicine, int position, boolean isActive) {        medicine.setActive(isActive);
        boolean saved = medicineDao.toggleActive(medicine.getId(), sessionManager.getUserId(), isActive);
        if (!saved) {
            medicine.setActive(!isActive);
            adapter.notifyItemChanged(position);
            return;
        }
        if (isActive) {
            MedicineAlarmReceiver.schedule(requireContext(), medicine);
        } else {
            MedicineAlarmReceiver.cancel(requireContext(), medicine);
        }
        View rootView = getView();
        if (rootView != null) {
            int msgRes = isActive ? R.string.reminder_alarm_on : R.string.reminder_alarm_off;
            Snackbar.make(rootView, getString(msgRes, medicine.getName()), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.reminder_undo), v -> {
                        boolean revert = !isActive;
                        medicine.setActive(revert);
                        if (medicineDao.toggleActive(medicine.getId(), sessionManager.getUserId(), revert)) {
                            if (revert) {
                                MedicineAlarmReceiver.schedule(requireContext(), medicine);
                            } else {
                                MedicineAlarmReceiver.cancel(requireContext(), medicine);
                            }
                            adapter.notifyItemChanged(position);
                        }
                    })
                    .show();
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = adapter.getContextMenuPosition();
        if (position < 0 || position >= medicines.size()) {
            return super.onContextItemSelected(item);
        }

        Medicine medicine = medicines.get(position);
        int itemId = item.getItemId();

        if (itemId == R.id.menu_medicine_edit) {
            Intent intent = new Intent(requireContext(), AddMedicineActivity.class);
            intent.putExtra(AddMedicineActivity.EXTRA_MEDICINE_ID, medicine.getId());
            medicineFormLauncher.launch(intent);
            return true;
        } else if (itemId == R.id.menu_medicine_delete) {
            vn.edu.tdmu.vita.utils.MedicineDoseDialogs.delete(requireActivity(), sessionManager.getUserId(), medicine, this::loadMedicines);
            return true;
        }

        return super.onContextItemSelected(item);
    }

}
