package vn.edu.tdmu.vita.activities;

import android.content.Intent;
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

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.InputValidator;
import vn.edu.tdmu.vita.utils.SessionManager;

public class EditProfileActivity extends AppCompatActivity {

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

        initViews();
        setupSpinner();
        loadCurrentProfile();
        setupListeners();
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
    }

    private void pickTime(TextInputEditText field) {
        String time = field.getText().toString();
        new android.app.TimePickerDialog(this, (view, hour, minute) ->
                field.setText(String.format(java.util.Locale.US, "%02d:%02d", hour, minute)),
                Integer.parseInt(time.substring(0, 2)), Integer.parseInt(time.substring(3)), true).show();
    }

    private void handleSave() {
        if (currentUser == null || sessionManager.getUserId() != currentUser.getId()) return;
        int amount;
        try { amount = Integer.parseInt(etWaterAmount.getText().toString()); }
        catch (NumberFormatException e) { amount = 0; }
        String start = etWaterStart.getText().toString(), end = etWaterEnd.getText().toString();
        if (!vn.edu.tdmu.vita.utils.WaterReminderPolicy.validSettings(start, end, amount)) {
            etWaterAmount.setError(getString(R.string.water_settings_invalid));
            return;
        }
        String fullname = etFullname.getText() != null ? etFullname.getText().toString().trim() : "";
        String birthYearStr = etBirthYear.getText() != null ? etBirthYear.getText().toString().trim() : "";
        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String waterGoalStr = etWaterGoal.getText() != null ? etWaterGoal.getText().toString().trim() : "";
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
        int birthYear = currentUser.getBirthYear();
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

        // Water goal validation
        int waterGoal = 2000;
        if (!waterGoalStr.isEmpty()) {
            try {
                waterGoal = Integer.parseInt(waterGoalStr);
                if (!InputValidator.isValidWaterGoal(waterGoal)) {
                    tilWaterGoal.setError(getString(R.string.edit_profile_error_invalid_water_goal,
                            InputValidator.MIN_WATER_GOAL_ML, InputValidator.MAX_WATER_GOAL_ML));
                    hasError = true;
                } else {
                    tilWaterGoal.setError(null);
                }
            } catch (NumberFormatException e) {
                tilWaterGoal.setError(getString(R.string.edit_profile_error_nan_water_goal));
                hasError = true;
            }
        }

        if (hasError) {
            return;
        }

        currentUser.setWaterStart(start);
        currentUser.setWaterEnd(end);
        currentUser.setWaterAmount(amount);
        currentUser.setWeatherAdjustmentEnabled(switchWeather.isChecked());
        currentUser.setFullname(fullname);
        currentUser.setGender(gender);
        currentUser.setBirthYear(birthYear);
        currentUser.setHeightCm(height);
        currentUser.setBaseWeightKg(weight);
        currentUser.setWaterGoalMl(waterGoal);

        boolean success = userDao.updateProfile(currentUser);
        if (success) {
            vn.edu.tdmu.vita.receivers.WaterReminderReceiver.cancel(this);
            vn.edu.tdmu.vita.receivers.WaterReminderReceiver.schedule(this);
            androidx.core.app.NotificationManagerCompat.from(this).cancel(9001);
            sessionManager.saveSession(currentUser.getId(), currentUser.getUsername(), currentUser.getFullname());
            Toast.makeText(this, R.string.edit_profile_save_success, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.edit_profile_save_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_profile_logout_dialog_title)
                .setMessage(R.string.edit_profile_logout_dialog_msg)
                .setPositiveButton(R.string.edit_profile_btn_logout, (dialog, which) -> {
                    for (vn.edu.tdmu.vita.models.Medicine medicine :
                            new vn.edu.tdmu.vita.database.MedicineDao(this).getActiveMedicines(sessionManager.getUserId())) {
                        vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver.cancel(this, medicine);
                    }
                    vn.edu.tdmu.vita.receivers.WaterReminderReceiver.cancel(this);
                    androidx.core.app.NotificationManagerCompat.from(this).cancelAll();
                    sessionManager.clearSession();
                    Toast.makeText(EditProfileActivity.this, R.string.edit_profile_logout_toast, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .show();
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
