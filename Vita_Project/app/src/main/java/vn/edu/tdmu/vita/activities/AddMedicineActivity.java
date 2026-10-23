package vn.edu.tdmu.vita.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver;
import vn.edu.tdmu.vita.utils.SessionManager;

public class AddMedicineActivity extends AppCompatActivity {
    private vn.edu.tdmu.vita.utils.UnsavedChangesGuard unsavedGuard;

    private long formUserId;

    public static final String EXTRA_MEDICINE_ID = "extra_medicine_id";

    private MaterialToolbar toolbar;
    private TextInputLayout tilName;
    private TextInputEditText etName;
    private TextInputEditText etDosage;
    private TextInputLayout tilTime;
    private TextInputEditText etTime;
    private CheckBox[] dayButtons;
    private MaterialCheckBox cbAllDays;
    private MaterialButton btnSave;

    private MedicineDao medicineDao;
    private SessionManager sessionManager;

    private long medicineId = -1;
    private String selectedTime;
    private int daysMask = 127;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        sessionManager = new SessionManager(this);
        formUserId = savedInstanceState == null ? sessionManager.getUserId() : savedInstanceState.getLong("form_user_id", -1);
        if (!SessionManager.matchesUser(formUserId, sessionManager.getUserId())) {
            finish();
            return;
        }

        medicineDao = new MedicineDao(this);

        initViews();
        setupListeners();
        loadIntentData();
        if (savedInstanceState != null) {
            selectedTime = savedInstanceState.getString("times", selectedTime);
            daysMask = savedInstanceState.getInt("days", daysMask);
            renderTimes();
            refreshDayButtons();
        }
        unsavedGuard = vn.edu.tdmu.vita.utils.UnsavedChangesGuard.attach(this, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> unsavedGuard.requestExit());
    }

    @Override protected void onResume() {
        super.onResume();
        if (!SessionManager.matchesUser(formUserId, new SessionManager(this).getUserId())) finish();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_add_medicine);
        tilName = findViewById(R.id.til_med_name);
        etName = findViewById(R.id.et_med_name);
        etDosage = findViewById(R.id.et_med_dosage);
        tilTime = findViewById(R.id.til_med_time);
        etTime = findViewById(R.id.et_med_time);
        etTime.setKeyListener(null);
        dayButtons = new CheckBox[]{
                findViewById(R.id.btn_day_mon),
                findViewById(R.id.btn_day_tue),
                findViewById(R.id.btn_day_wed),
                findViewById(R.id.btn_day_thu),
                findViewById(R.id.btn_day_fri),
                findViewById(R.id.btn_day_sat),
                findViewById(R.id.btn_day_sun)
        };
        cbAllDays = findViewById(R.id.cb_med_all_days);
        btnSave = findViewById(R.id.btn_med_save);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        etTime.setOnClickListener(v -> showTimePicker());
        tilTime.setStartIconOnClickListener(v -> showTimePicker());
        tilTime.setOnClickListener(v -> showTimePicker());

        etName.addTextChangedListener(new SimpleTextWatcher(() -> tilName.setError(null)));

        for (int i = 0; i < dayButtons.length; i++) {
            final int bit = i;
            dayButtons[i].setOnClickListener(v -> {
                daysMask ^= (1 << bit);
                refreshDayButtons();
            });
        }
        cbAllDays.setOnCheckedChangeListener((buttonView, isChecked) -> {
            daysMask = isChecked ? 127 : 0;
            refreshDayButtons();
        });

        btnSave.setOnClickListener(v -> handleSave());
    }

    private void loadIntentData() {
        medicineId = getIntent().getLongExtra(EXTRA_MEDICINE_ID, -1);

        if (medicineId > 0) {
            ((android.widget.TextView) findViewById(R.id.title_add_medicine)).setText(R.string.add_med_title_edit);
            Medicine medicine = medicineDao.getById(medicineId, formUserId);
            if (medicine != null && medicine.getUserId() == sessionManager.getUserId()) {
                etName.setText(medicine.getName());
                if (medicine.getDosage() != null) etDosage.setText(medicine.getDosage());
                selectedTime = medicine.getTime();
                daysMask = medicine.getDaysMask();
            } else {
                finish();
                return;
            }
        } else {
            ((android.widget.TextView) findViewById(R.id.title_add_medicine)).setText(R.string.add_med_title_add);
            selectedTime = nextHourDefault();
            daysMask = 127;
        }
        renderTimes();
        refreshDayButtons();
    }

    private String nextHourDefault() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 0);
        return String.format(Locale.US, "%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    private void showTimePicker() { showTimePicker(null); }

    private void showTimePicker(String editing) {
        int hour = 8;
        int minute = 0;
        if (editing != null && editing.matches("\\d{1,2}:\\d{2}")) {
            String[] parts = editing.split(":");
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }
        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, h, m) -> {
                    java.util.List<String> times = new java.util.ArrayList<>();
                    if (selectedTime != null && !selectedTime.isEmpty()) times.addAll(java.util.Arrays.asList(selectedTime.split(",")));
                    if (editing != null) times.remove(editing);
                    times.add(String.format(Locale.US, "%02d:%02d", h, m));
                    selectedTime = vn.edu.tdmu.vita.utils.MedicineSchedule.normalizeTimes(android.text.TextUtils.join(",", times));
                    renderTimes();
                }, hour, minute, true);
        dialog.show();
    }

    private void renderTimes() {
        etTime.setText(R.string.figma_add_time);
        com.google.android.material.chip.ChipGroup group = findViewById(R.id.chips_med_times);
        group.removeAllViews();
        if (selectedTime == null || selectedTime.isEmpty()) return;
        for (String time : selectedTime.split(",")) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(time);
            chip.setEnsureMinTouchTargetSize(true);
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.vita_surface)));
            chip.setChipStrokeWidth(0); chip.setTextSize(12); chip.setMinWidth(Math.round(116 * getResources().getDisplayMetrics().density));
            chip.setCloseIconStartPadding(12 * getResources().getDisplayMetrics().density);
            chip.setCloseIconEndPadding(12 * getResources().getDisplayMetrics().density);
            chip.setCloseIconSize(24 * getResources().getDisplayMetrics().density); chip.setTextColor(ContextCompat.getColor(this, R.color.vita_text_primary));
            chip.setCloseIconResource(R.drawable.ic_cancel);
            chip.setCloseIconVisible(true);
            chip.setCloseIconContentDescription(getString(R.string.med_remove_time, time));
            chip.setOnClickListener(v -> showTimePicker(time));
            chip.setOnCloseIconClickListener(v -> {
                java.util.List<String> times = new java.util.ArrayList<>(java.util.Arrays.asList(selectedTime.split(",")));
                times.remove(time);
                selectedTime = android.text.TextUtils.join(",", times);
                renderTimes();
            });
            group.addView(chip);
        }
    }

    private void refreshDayButtons() {
        for (int i = 0; i < dayButtons.length; i++) {
            boolean selected = (daysMask & (1 << i)) != 0;
            CheckBox btn = dayButtons[i];
            if (selected) {
                btn.setBackgroundResource(R.drawable.bg_day_selected);
                btn.setTextColor(ContextCompat.getColor(this, R.color.vita_teal));
            } else {
                btn.setBackgroundResource(R.drawable.bg_day_unselected);
                btn.setTextColor(ContextCompat.getColor(this, R.color.vita_teal));
            }
            btn.setSelected(selected);
            btn.setChecked(selected);
            btn.setContentDescription(getResources().getStringArray(R.array.medicine_weekday_names)[i]);
        }
        cbAllDays.setOnCheckedChangeListener(null);
        cbAllDays.setChecked(daysMask == 127);
        cbAllDays.setOnCheckedChangeListener((buttonView, isChecked) -> {
            daysMask = isChecked ? 127 : 0;
            refreshDayButtons();
        });
    }

    private void handleSave() {
        if (!SessionManager.matchesUser(formUserId, sessionManager.getUserId())) { finish(); return; }
        String name = getText(etName);
        String dosage = getText(etDosage);

        if (name.isEmpty()) {
            tilName.setError(getString(R.string.add_med_error_empty_name));
            return;
        }
        if (selectedTime == null || selectedTime.isEmpty()) {
            tilTime.setError(getString(R.string.med_time_required));
            return;
        }
        tilTime.setError(null);
        if (daysMask == 0) {
            Toast.makeText(this, getString(R.string.add_med_error_no_days), Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = sessionManager.getUserId();
        boolean success;
        Medicine medicine;

        if (medicineId > 0) {
            medicine = medicineDao.getById(medicineId, formUserId);
            if (medicine == null || medicine.getUserId() != userId) {
                Toast.makeText(this, getString(R.string.add_med_error_update), Toast.LENGTH_SHORT).show();
                return;
            }
            MedicineAlarmReceiver.cancel(this, medicine);
            medicine.setName(name);
            medicine.setDosage(dosage.isEmpty() ? null : dosage);
            medicine.setTime(selectedTime);
            medicine.setDaysMask(daysMask);
            success = medicineDao.update(medicine, formUserId);
            if (!success) MedicineAlarmReceiver.schedule(this, medicineDao.getById(medicineId, formUserId));
            if (success) {
                Toast.makeText(this, getString(R.string.add_med_success_edit), Toast.LENGTH_SHORT).show();
            }
        } else {
            medicine = new Medicine(0, userId, name, dosage.isEmpty() ? null : dosage,
                    selectedTime, daysMask, true);
            long id = medicineDao.insert(medicine);
            success = id > 0;
            if (success) {
                medicine.setId(id);
                Toast.makeText(this, getString(R.string.add_med_success_add, name, selectedTime),
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (success) {
            if (medicine.isActive()) {
                MedicineAlarmReceiver.schedule(this, medicine);
            } else {
                MedicineAlarmReceiver.cancel(this, medicine);
            }
            setResult(RESULT_OK);
            finish();
        } else if (medicineId <= 0) {
            Toast.makeText(this, getString(R.string.add_med_error_save), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putLong("form_user_id", formUserId);
        if (unsavedGuard != null) unsavedGuard.saveState(state);
        super.onSaveInstanceState(state);
        state.putString("times", selectedTime);
        state.putInt("days", daysMask);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onTextChanged;

        SimpleTextWatcher(Runnable onTextChanged) {
            this.onTextChanged = onTextChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (onTextChanged != null) {
                onTextChanged.run();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }
}
