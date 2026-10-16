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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private FloatingActionButton fabAddMedicine;

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
        adapter.setOnQuickLogListener(this::handleQuickLog);
        adapter.setHistoryListener(medicine -> {
            java.util.List<vn.edu.tdmu.vita.models.MedicineLog> logs = medicineDao.getHistory(medicine.getId());
            if (logs.isEmpty()) {
                new AlertDialog.Builder(requireContext()).setTitle(medicine.getName())
                        .setMessage(R.string.med_history_empty).setPositiveButton(android.R.string.ok, null).show();
                return;
            }
            String[] lines = new String[logs.size()];
            for (int i = 0; i < logs.size(); i++) {
                vn.edu.tdmu.vita.models.MedicineLog log = logs.get(i);
                lines[i] = DateTimeUtils.formatDisplayDate(log.getDate()) + " " + log.getScheduledTime()
                        + " — " + getString("TAKEN".equals(log.getStatus())
                        ? R.string.reminder_quick_taken : R.string.reminder_quick_skipped);
            }
            new AlertDialog.Builder(requireContext()).setTitle(medicine.getName())
                    .setItems(lines, null).setPositiveButton(android.R.string.ok, null).show();
        });
        rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMedicines.setAdapter(adapter);
        registerForContextMenu(rvMedicines);
    }

    private void setupListeners() {
        requireView().findViewById(R.id.btn_water_settings).setOnClickListener(v -> startActivity(new Intent(requireContext(), vn.edu.tdmu.vita.activities.EditProfileActivity.class)));
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
        banner.setVisibility(NotificationHelper.canNotify(requireContext()) ? View.GONE : View.VISIBLE);
        getView().findViewById(R.id.btn_notification_settings).setOnClickListener(v -> {
            Intent settings;
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

    private void loadWaterProgress(boolean animate) {
        long userId = sessionManager.getUserId();
        if (userId <= 0) return;

        User user = userDao.getUserById(userId);
        int baseGoal = user != null && user.getWaterGoalMl() > 0 ? user.getWaterGoalMl() : 2000;
        waterGoalMl = HydrationGoal.current(requireContext(), user);
        quickAddMl = user != null ? user.getWaterAmount() : 250;
        btnQuickAddWater.setText(getString(R.string.reminder_btn_add_water, quickAddMl));
        TextView adjustment = requireView().findViewById(R.id.tv_water_adjustment);
        adjustment.setText(waterGoalMl > baseGoal ? R.string.water_heat_adjustment : R.string.water_base_goal);

        int total = waterDao.getDailyTotal(userId, DateTimeUtils.getTodayDate());
        int oldProgress = pbWaterProgress.getProgress();
        pbWaterProgress.setMax(waterGoalMl);
        tvWaterProgress.setText(String.format(Locale.getDefault(),
                getString(R.string.reminder_water_progress_format),
                total, waterGoalMl, waterGoalMl > 0 ? total * 100 / waterGoalMl : 0));

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
        long userId = sessionManager.getUserId();
        if (userId <= 0) return;

        String today = DateTimeUtils.getTodayDate();
        int before = waterDao.getDailyTotal(userId, today);
        long id = waterDao.addWater(userId, today, quickAddMl, DateTimeUtils.getCurrentTimestamp());
        if (id <= 0) {
            Toast.makeText(requireContext(), getString(R.string.add_med_error_save), Toast.LENGTH_SHORT).show();
            return;
        }
        loadWaterProgress(true);
        int after = before + quickAddMl;
        if (after >= waterGoalMl) androidx.core.app.NotificationManagerCompat.from(requireContext()).cancel(9001);
        if (before < waterGoalMl && after >= waterGoalMl) {
            Toast.makeText(requireContext(), getString(R.string.reminder_water_reached), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMedicines() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) return;

        medicines.clear();
        medicines.addAll(medicineDao.getAllByUserId(userId));
        java.util.Map<String, vn.edu.tdmu.vita.models.MedicineLog> logs = new java.util.HashMap<>();
        String today = vn.edu.tdmu.vita.utils.DateTimeUtils.getTodayDate();
        for (Medicine medicine : medicines) {
            for (String time : medicine.getTime().split(",")) {
                vn.edu.tdmu.vita.models.MedicineLog log = medicineDao.getLogForDose(medicine.getId(), today, time);
                if (log != null) logs.put(medicine.getId() + "@" + time, log);
            }
        }
        adapter.setTodayLogs(logs);
        adapter.notifyDataSetChanged();

        if (medicines.isEmpty()) {
            rvMedicines.setVisibility(View.GONE);
            layoutMedicineEmpty.setVisibility(View.VISIBLE);
        } else {
            rvMedicines.setVisibility(View.VISIBLE);
            layoutMedicineEmpty.setVisibility(View.GONE);
        }
    }

    private void handleQuickLog(Medicine medicine, String time, boolean taken) {
        String status = taken ? "TAKEN" : "SKIPPED";
        medicineDao.logMedicineStatus(medicine.getId(),
                vn.edu.tdmu.vita.utils.DateTimeUtils.getTodayDate(), time, status,
                vn.edu.tdmu.vita.utils.DateTimeUtils.getCurrentTimestamp());
        loadMedicines();
        int msgRes = taken ? R.string.reminder_quick_taken_toast
                : R.string.reminder_quick_skipped_toast;
        String message = taken ? getString(msgRes, medicine.getName()) : getString(msgRes);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void handleSwitchChanged(Medicine medicine, int position, boolean isActive) {        medicine.setActive(isActive);
        boolean saved = medicineDao.toggleActive(medicine.getId(), isActive);
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
                        if (medicineDao.toggleActive(medicine.getId(), revert)) {
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
            showDeleteConfirmation(medicine, position);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void showDeleteConfirmation(Medicine medicine, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.reminder_delete_dialog_title))
                .setMessage(getString(R.string.reminder_delete_dialog_msg, medicine.getName()))
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .setPositiveButton(getString(R.string.btn_delete), (dialog, which) -> {
                    MedicineAlarmReceiver.cancel(requireContext(), medicine);
                    boolean deleted = medicineDao.delete(medicine.getId());
                    if (deleted) {
                        medicines.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, medicines.size() - position);
                        if (medicines.isEmpty()) {
                            rvMedicines.setVisibility(View.GONE);
                            layoutMedicineEmpty.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(requireContext(), getString(R.string.reminder_delete_success),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        MedicineAlarmReceiver.schedule(requireContext(), medicine);
                        Toast.makeText(requireContext(), getString(R.string.reminder_delete_error),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
