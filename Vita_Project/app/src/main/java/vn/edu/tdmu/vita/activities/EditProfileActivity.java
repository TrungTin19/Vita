package vn.edu.tdmu.vita.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.List;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.InputValidator;
import vn.edu.tdmu.vita.utils.SessionManager;

public class EditProfileActivity extends AppCompatActivity {
    public static final String EXTRA_WATER_SETTINGS_ONLY = "water_settings_only";
    private boolean waterSettingsOnly;
    private String selectedWaterTimes = "";

    private MaterialToolbar toolbar;
    private TextInputLayout tilFullname;
    private TextInputLayout tilBirthYear;
    private AutoCompleteTextView actGender;
    private TextInputLayout tilHeight;
    private TextInputLayout tilWeight;
    private TextInputLayout tilWaterGoal;

    private TextInputEditText etFullname;
    private TextInputEditText etBirthYear;
    private TextInputEditText etHeight;
    private TextInputEditText etWeight;
    private TextInputEditText etWaterGoal;

    private TextInputEditText etWaterStart, etWaterEnd, etWaterAmount;
    private com.google.android.material.materialswitch.MaterialSwitch switchWeather;
    private MaterialButton btnSave;
    private MaterialButton btnLogout;

    private UserDao userDao;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);

        waterSettingsOnly = getIntent().getBooleanExtra(EXTRA_WATER_SETTINGS_ONLY, false);
        initViews();
        setupSpinner();
        loadCurrentProfile();
        if (currentUser == null) return;
        findViewById(R.id.group_profile_fields).setVisibility(waterSettingsOnly ? android.view.View.GONE : android.view.View.VISIBLE);
        findViewById(R.id.group_water_settings).setVisibility(waterSettingsOnly ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.group_profile_actions).setVisibility(waterSettingsOnly ? android.view.View.GONE : android.view.View.VISIBLE);
        if (waterSettingsOnly) toolbar.setTitle(R.string.water_settings);
        setupListeners();
        setupWaterSchedule(savedInstanceState);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_edit_profile);
        tilFullname = findViewById(R.id.til_edit_fullname);
        tilBirthYear = findViewById(R.id.til_edit_birth_year);
        actGender = findViewById(R.id.act_edit_gender);
        tilHeight = findViewById(R.id.til_edit_height);
        tilWeight = findViewById(R.id.til_edit_weight);
        tilWaterGoal = findViewById(R.id.til_edit_water_goal);

        etFullname = findViewById(R.id.et_edit_fullname);
        etBirthYear = findViewById(R.id.et_edit_birth_year);
        etHeight = findViewById(R.id.et_edit_height);
        etWeight = findViewById(R.id.et_edit_weight);
        etWaterGoal = findViewById(R.id.et_edit_water_goal);

        etWaterStart = findViewById(R.id.et_edit_water_start);
        etWaterEnd = findViewById(R.id.et_edit_water_end);
        etWaterStart.setKeyListener(null);
        etWaterEnd.setKeyListener(null);
        etWaterStart.setFocusableInTouchMode(true);
        etWaterEnd.setFocusableInTouchMode(true);
        etWaterStart.setShowSoftInputOnFocus(false);
        etWaterEnd.setShowSoftInputOnFocus(false);
        etWaterAmount = findViewById(R.id.et_edit_water_amount);
        switchWeather = findViewById(R.id.switch_water_weather);
        etWaterStart.setOnClickListener(v -> pickTime(etWaterStart));
        etWaterEnd.setOnClickListener(v -> pickTime(etWaterEnd));
        btnSave = findViewById(R.id.btn_edit_save);
        btnLogout = findViewById(R.id.btn_edit_logout);
    }

    private void setupSpinner() {
        String[] genders = new String[]{"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        actGender.setAdapter(adapter);
    }

    private void loadCurrentProfile() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            finish();
            return;
        }

        currentUser = userDao.getUserById(userId);
        if (currentUser == null) {
            finish();
            return;
        }

        etWaterStart.setText(currentUser.getWaterStart());
        etWaterEnd.setText(currentUser.getWaterEnd());
        etWaterAmount.setText(String.valueOf(currentUser.getWaterAmount()));
        switchWeather.setChecked(currentUser.isWeatherAdjustmentEnabled());
        if (currentUser.getFullname() != null) {
            etFullname.setText(currentUser.getFullname());
        }
        if (currentUser.getBirthYear() > 0) {
            etBirthYear.setText(String.valueOf(currentUser.getBirthYear()));
        }
        if (currentUser.getHeightCm() > 0) {
            etHeight.setText(String.valueOf(currentUser.getHeightCm()));
        }
        if (currentUser.getBaseWeightKg() > 0) {
            etWeight.setText(String.valueOf(currentUser.getBaseWeightKg()));
        }
        if (currentUser.getWaterGoalMl() > 0) {
            etWaterGoal.setText(String.valueOf(currentUser.getWaterGoalMl()));
        }

        if (currentUser.getGender() != null && !currentUser.getGender().isEmpty()) {
            actGender.setText(currentUser.getGender(), false);
        } else {
            actGender.setText("Nam", false);
        }
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        // Clear errors automatically as user types
        etBirthYear.addTextChangedListener(new SimpleTextWatcher(() -> tilBirthYear.setError(null)));
        etHeight.addTextChangedListener(new SimpleTextWatcher(() -> tilHeight.setError(null)));
        etWeight.addTextChangedListener(new SimpleTextWatcher(() -> tilWeight.setError(null)));
        etWaterGoal.addTextChangedListener(new SimpleTextWatcher(() -> tilWaterGoal.setError(null)));

        btnSave.setOnClickListener(v -> handleSave());
        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
        findViewById(R.id.btn_edit_delete).setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void pickTime(TextInputEditText field) {
        String time = field.getText().toString();
        new android.app.TimePickerDialog(this, (view, hour, minute) ->
                field.setText(String.format(java.util.Locale.US, "%02d:%02d", hour, minute)),
                Integer.parseInt(time.substring(0, 2)), Integer.parseInt(time.substring(3)), true).show();
    }

    private void setupWaterSchedule(Bundle state) {
        selectedWaterTimes = state == null ? currentUser.getWaterTimes() : state.getString("water_times_draft", "");
        android.widget.RadioGroup modes = findViewById(R.id.rg_water_schedule);
        modes.setOnCheckedChangeListener((group, checked) -> findViewById(R.id.group_water_custom_times)
                .setVisibility(checked == R.id.rb_water_custom_times ? android.view.View.VISIBLE : android.view.View.GONE));
        boolean custom = state == null ? !selectedWaterTimes.isEmpty() : state.getBoolean("water_custom_draft");
        modes.check(custom ? R.id.rb_water_custom_times : R.id.rb_water_every_two_hours);
        findViewById(R.id.btn_add_water_time).setOnClickListener(v -> pickWaterTime(null));
        renderWaterTimes();
    }

    private void pickWaterTime(String editing) {
        String seed = editing == null ? etWaterStart.getText().toString() : editing;
        new android.app.TimePickerDialog(this, (picker, hour, minute) -> {
            java.util.List<String> times = new java.util.ArrayList<>();
            if (!selectedWaterTimes.isEmpty()) times.addAll(java.util.Arrays.asList(selectedWaterTimes.split(",")));
            if (editing != null) times.remove(editing);
            times.add(String.format(java.util.Locale.US, "%02d:%02d", hour, minute));
            selectedWaterTimes = vn.edu.tdmu.vita.utils.MedicineSchedule.normalizeTimes(android.text.TextUtils.join(",", times));
            renderWaterTimes();
        }, Integer.parseInt(seed.substring(0, 2)), Integer.parseInt(seed.substring(3)), true).show();
    }

    private void renderWaterTimes() {
        com.google.android.material.chip.ChipGroup group = findViewById(R.id.chips_water_times);
        group.removeAllViews();
        findViewById(R.id.tv_water_times_error).setVisibility(android.view.View.GONE);
        if (selectedWaterTimes.isEmpty()) return;
        for (String time : selectedWaterTimes.split(",")) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(time); chip.setEnsureMinTouchTargetSize(true);
            chip.setCloseIconResource(R.drawable.ic_cancel); chip.setCloseIconVisible(true);
            chip.setCloseIconContentDescription(getString(R.string.water_remove_time, time));
            chip.setOnClickListener(v -> pickWaterTime(time));
            chip.setOnCloseIconClickListener(v -> {
                java.util.List<String> times = new java.util.ArrayList<>(java.util.Arrays.asList(selectedWaterTimes.split(",")));
                times.remove(time); selectedWaterTimes = android.text.TextUtils.join(",", times); renderWaterTimes();
            });
            group.addView(chip);
        }
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("water_times_draft", selectedWaterTimes);
        state.putBoolean("water_custom_draft", ((android.widget.RadioGroup)findViewById(R.id.rg_water_schedule)).getCheckedRadioButtonId() == R.id.rb_water_custom_times);
        super.onSaveInstanceState(state);
    }

    private void handleSave() {
        if (currentUser == null || sessionManager.getUserId() != currentUser.getId()) return;
        if (waterSettingsOnly) { saveWaterSettings(); return; }
        String fullname = etFullname.getText() != null ? etFullname.getText().toString().trim() : "";
        String birthYearStr = etBirthYear.getText() != null ? etBirthYear.getText().toString().trim() : "";
        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String gender = actGender.getText() != null && !actGender.getText().toString().trim().isEmpty()
                ? actGender.getText().toString().trim() : "Nam";

        boolean hasError = false;

        // Height validation
        float height = 0f;
        if (heightStr.isEmpty()) {
            tilHeight.setError(getString(R.string.register_error_empty_height));
            hasError = true;
        } else {
            try {
                height = Float.parseFloat(heightStr);
                if (!InputValidator.isValidHeight(height)) {
                    tilHeight.setError(getString(R.string.register_error_invalid_height,
                            InputValidator.MIN_HEIGHT_CM, InputValidator.MAX_HEIGHT_CM));
                    hasError = true;
                } else {
                    tilHeight.setError(null);
                }
            } catch (NumberFormatException e) {
                tilHeight.setError(getString(R.string.register_error_nan_height));
                hasError = true;
            }
        }

        // Weight validation
        float weight = 0f;
        if (weightStr.isEmpty()) {
            tilWeight.setError(getString(R.string.register_error_empty_weight));
            hasError = true;
        } else {
            try {
                weight = Float.parseFloat(weightStr);
                if (!InputValidator.isValidWeight(weight)) {
                    tilWeight.setError(getString(R.string.register_error_invalid_weight,
                            InputValidator.MIN_WEIGHT_KG, InputValidator.MAX_WEIGHT_KG));
                    hasError = true;
                } else {
                    tilWeight.setError(null);
                }
            } catch (NumberFormatException e) {
                tilWeight.setError(getString(R.string.register_error_nan_weight));
                hasError = true;
            }
        }

        // Birth year validation
        int birthYear = 0;
        if (!birthYearStr.isEmpty()) {
            try {
                birthYear = Integer.parseInt(birthYearStr);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (!InputValidator.isValidBirthYear(birthYear)) {
                    tilBirthYear.setError(getString(R.string.register_error_invalid_birth_year, currentYear));
                    hasError = true;
                } else {
                    tilBirthYear.setError(null);
                }
            } catch (NumberFormatException e) {
                tilBirthYear.setError(getString(R.string.register_error_nan_birth_year));
                hasError = true;
            }
        }

        if (hasError) {
            focusFirstError(tilBirthYear, tilHeight, tilWeight);
            return;
        }

        currentUser.setFullname(fullname);
        currentUser.setGender(gender);
        currentUser.setBirthYear(birthYear);
        currentUser.setHeightCm(height);
        currentUser.setBaseWeightKg(weight);

        boolean success = userDao.updateProfile(currentUser);
        if (success) {
            showSaved();
        } else {
            Toast.makeText(this, R.string.edit_profile_save_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveWaterSettings() {
        TextInputLayout tilStart = findViewById(R.id.til_edit_water_start);
        TextInputLayout tilEnd = findViewById(R.id.til_edit_water_end);
        TextInputLayout tilAmount = findViewById(R.id.til_edit_water_amount);
        tilWaterGoal.setError(null); tilStart.setError(null); tilEnd.setError(null); tilAmount.setError(null);
        int goal = 0, amount = 0;
        try { goal = Integer.parseInt(etWaterGoal.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        try { amount = Integer.parseInt(etWaterAmount.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        String start = etWaterStart.getText().toString(), end = etWaterEnd.getText().toString();
        if (!InputValidator.isValidWaterGoal(goal)) tilWaterGoal.setError(getString(R.string.edit_profile_error_invalid_water_goal,
                InputValidator.MIN_WATER_GOAL_ML, InputValidator.MAX_WATER_GOAL_ML));
        if (!vn.edu.tdmu.vita.utils.WaterReminderPolicy.validSettings(start, end, amount)) {
            (start.equals(end) ? tilEnd : tilAmount).setError(getString(R.string.water_settings_invalid));
        }
        if (tilWaterGoal.getError() != null || tilEnd.getError() != null || tilAmount.getError() != null) {
            focusFirstError(tilWaterGoal, tilStart, tilEnd, tilAmount); return;
        }
        String times = "";
        boolean custom = ((android.widget.RadioGroup)findViewById(R.id.rg_water_schedule)).getCheckedRadioButtonId() == R.id.rb_water_custom_times;
        try {
            if (custom && selectedWaterTimes.isEmpty()) throw new IllegalArgumentException("Choose a time");
            times = vn.edu.tdmu.vita.utils.WaterReminderPolicy.normalizeTimes(start, end, custom ? selectedWaterTimes : "");
        } catch (IllegalArgumentException invalid) {
            android.widget.TextView error = findViewById(R.id.tv_water_times_error);
            error.setText(getString(R.string.water_times_error, start, end)); error.setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.btn_add_water_time).requestFocus(); return;
        }
        if (userDao.updateWaterSettings(currentUser.getId(), goal, start, end, amount, switchWeather.isChecked(), times)) {
            vn.edu.tdmu.vita.receivers.WaterReminderReceiver.cancel(this);
            vn.edu.tdmu.vita.receivers.WaterReminderReceiver.schedule(this);
            androidx.core.app.NotificationManagerCompat.from(this).cancel(9001);
            showSaved();
        } else Toast.makeText(this, R.string.edit_profile_save_error, Toast.LENGTH_SHORT).show();
    }

    private void showSaved() {
        User saved = userDao.getUserById(currentUser.getId());
        sessionManager.saveSession(saved.getId(), saved.getUsername(), saved.getFullname());
        Toast.makeText(this, R.string.edit_profile_save_success, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK); finish();
    }

    private void focusFirstError(TextInputLayout... fields) {
        for (TextInputLayout field : fields) if (field.getError() != null && field.getEditText() != null) {
            field.getEditText().requestFocus(); return;
        }
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_profile_logout_dialog_title)
                .setMessage(R.string.edit_profile_logout_dialog_msg)
                .setPositiveButton(R.string.edit_profile_btn_logout, (dialog, which) -> {
                    if (!ownsSession()) { finish(); return; }
                    finishSession(new MedicineDao(this).getAllByUserId(currentUser.getId()), R.string.edit_profile_logout_toast);
                })
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean ownsSession() {
        return currentUser != null && sessionManager.getUserId() == currentUser.getId();
    }

    private void showDeleteConfirmDialog() {
        if (!ownsSession()) { finish(); return; }
        android.view.View form = getLayoutInflater().inflate(R.layout.dialog_delete_profile, null);
        TextInputLayout passwordLayout = form.findViewById(R.id.til_delete_profile_password);
        TextInputEditText password = form.findViewById(R.id.et_delete_profile_password);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.profile_delete_title)
                .setMessage(R.string.profile_delete_message)
                .setView(form)
                .setPositiveButton(R.string.profile_delete_confirm, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .create();
        dialog.setOnDismissListener(ignored -> password.setText(null));
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!ownsSession()) { dialog.dismiss(); finish(); return; }
            String entered = password.getText() == null ? "" : password.getText().toString();
            try {
                List<Medicine> medicines = new MedicineDao(this).getAllByUserId(currentUser.getId());
                if (!userDao.deleteProfileWithPassword(currentUser.getId(), entered)) {
                    passwordLayout.setError(getString(R.string.profile_delete_password_error));
                    password.requestFocus();
                    return;
                }
                dialog.dismiss();
                finishSession(medicines, R.string.profile_delete_success);
            } catch (SQLiteException error) {
                Toast.makeText(this, R.string.profile_delete_error, Toast.LENGTH_LONG).show();
            }
        }));
        dialog.show();
    }

    private void finishSession(List<Medicine> medicines, int message) {
        for (Medicine medicine : medicines) {
            vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver.cancel(this, medicine);
        }
        vn.edu.tdmu.vita.receivers.WaterReminderReceiver.cancel(this);
        androidx.core.app.NotificationManagerCompat.from(this).cancelAll();
        sessionManager.clearSession();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
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
