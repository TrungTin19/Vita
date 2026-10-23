package vn.edu.tdmu.vita.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

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
    private vn.edu.tdmu.vita.utils.UnsavedChangesGuard unsavedGuard;
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
    private MaterialButton btnSave;
    private MaterialButton btnLogout;

    private UserDao userDao;
    private SessionManager sessionManager;
    private User currentUser;
    private long formUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);
        formUserId = savedInstanceState == null ? sessionManager.getUserId() : savedInstanceState.getLong("form_user_id", -1);
        if (!SessionManager.matchesUser(formUserId, sessionManager.getUserId())) { finish(); return; }

        waterSettingsOnly = getIntent().getBooleanExtra(EXTRA_WATER_SETTINGS_ONLY, false);
        initViews();
        setupSpinner();
        loadCurrentProfile();
        if (currentUser == null) return;
        findViewById(R.id.group_profile_fields).setVisibility(waterSettingsOnly ? android.view.View.GONE : android.view.View.VISIBLE);
        findViewById(R.id.group_water_settings).setVisibility(waterSettingsOnly ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.group_profile_actions).setVisibility(android.view.View.GONE);
        if (waterSettingsOnly) { ((android.widget.TextView) findViewById(R.id.title_edit_profile)).setText(R.string.figma_water_settings_title); btnSave.setText(R.string.figma_water_settings_save); }
        setupListeners();
        setupWaterSchedule(savedInstanceState);
        unsavedGuard = vn.edu.tdmu.vita.utils.UnsavedChangesGuard.attach(this, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> unsavedGuard.requestExit());
    }

    @Override protected void onResume() {
        super.onResume();
        if (!SessionManager.matchesUser(formUserId, new SessionManager(this).getUserId())) finish();
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
        if (getResources().getConfiguration().fontScale > 1.3f) {
            android.widget.LinearLayout row = (android.widget.LinearLayout) findViewById(R.id.til_edit_water_start).getParent();
            row.setOrientation(android.widget.LinearLayout.VERTICAL);
            for (int index = 0; index < row.getChildCount(); index++) {
                android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) row.getChildAt(index).getLayoutParams();
                params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                params.weight = 0;
                params.setMarginStart(0); params.setMarginEnd(0);
            }
        }
        etWaterAmount = findViewById(R.id.et_edit_water_amount);
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

        // Tự xóa thông báo lỗi khi người dùng nhập
        etBirthYear.addTextChangedListener(new SimpleTextWatcher(() -> tilBirthYear.setError(null)));
        etHeight.addTextChangedListener(new SimpleTextWatcher(() -> tilHeight.setError(null)));
        etWeight.addTextChangedListener(new SimpleTextWatcher(() -> tilWeight.setError(null)));
        etWaterGoal.addTextChangedListener(new SimpleTextWatcher(() -> tilWaterGoal.setError(null)));
        etWaterAmount.addTextChangedListener(new SimpleTextWatcher(() ->
                ((TextInputLayout) findViewById(R.id.til_edit_water_amount)).setError(null)));

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
        modes.setOnCheckedChangeListener((group, checked) -> {
            findViewById(R.id.group_water_custom_times).setVisibility(checked == R.id.rb_water_custom_times ? android.view.View.VISIBLE : android.view.View.GONE);
            renderSchedulePreview();
        });
        boolean custom = state == null ? !selectedWaterTimes.isEmpty() : state.getBoolean("water_custom_draft");
        modes.check(custom ? R.id.rb_water_custom_times : R.id.rb_water_every_two_hours);
        findViewById(R.id.btn_add_water_time).setOnClickListener(v -> pickWaterTime(null));
        renderWaterTimes();
        Runnable windowChanged = () -> {
            ((TextInputLayout) findViewById(R.id.til_edit_water_start)).setError(null);
            ((TextInputLayout) findViewById(R.id.til_edit_water_end)).setError(null);
            renderSchedulePreview();
        };
        etWaterStart.addTextChangedListener(new SimpleTextWatcher(windowChanged));
        etWaterEnd.addTextChangedListener(new SimpleTextWatcher(windowChanged));
        renderSchedulePreview();
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

    private void renderSchedulePreview() {
        android.widget.TextView preview = findViewById(R.id.tv_water_schedule_preview);
        boolean custom = ((android.widget.RadioGroup) findViewById(R.id.rg_water_schedule)).getCheckedRadioButtonId() == R.id.rb_water_custom_times;
        preview.setVisibility(custom ? android.view.View.GONE : android.view.View.VISIBLE);
        String start = etWaterStart.getText().toString(), end = etWaterEnd.getText().toString();
        if (!vn.edu.tdmu.vita.utils.WaterReminderPolicy.validSettings(start, end, 250)) { preview.setText(""); return; }
        int from = Integer.parseInt(start.substring(0, 2)) * 60 + Integer.parseInt(start.substring(3));
        int until = Integer.parseInt(end.substring(0, 2)) * 60 + Integer.parseInt(end.substring(3));
        StringBuilder slots = new StringBuilder(getString(R.string.figma_water_preview_title));
        for (int offset = 0; offset <= (until - from + 1440) % 1440; offset += 120) {
            int minute = (from + offset) % 1440;
            slots.append(offset == 0 ? "\n\n" : " · ").append(String.format(java.util.Locale.US, "%02d:%02d", minute / 60, minute % 60));
        }
        preview.setText(slots);
    }

    private void renderWaterTimes() {
        com.google.android.material.chip.ChipGroup group = findViewById(R.id.chips_water_times);
        group.removeAllViews();
        findViewById(R.id.tv_water_times_error).setVisibility(android.view.View.GONE);
        if (selectedWaterTimes.isEmpty()) return;
        for (String time : selectedWaterTimes.split(",")) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(time); chip.setEnsureMinTouchTargetSize(true);
            chip.setCloseIconSize(24 * getResources().getDisplayMetrics().density);
            chip.setCloseIconStartPadding(12 * getResources().getDisplayMetrics().density);
            chip.setCloseIconEndPadding(12 * getResources().getDisplayMetrics().density);
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
        state.putLong("form_user_id", formUserId);
        state.putString("water_times_draft", selectedWaterTimes);
        state.putBoolean("water_custom_draft", ((android.widget.RadioGroup)findViewById(R.id.rg_water_schedule)).getCheckedRadioButtonId() == R.id.rb_water_custom_times);
        if (unsavedGuard != null) unsavedGuard.saveState(state);
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

        // Kiểm tra chiều cao
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

        // Kiểm tra cân nặng
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

        // Kiểm tra năm sinh
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
        if (!vn.edu.tdmu.vita.utils.WaterReminderPolicy.validSettings(start, end, 250))
            tilEnd.setError(getString(R.string.water_window_invalid));
        if (amount < 50 || amount > 2000) tilAmount.setError(getString(R.string.water_entry_invalid));
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
        if (userDao.updateWaterSettings(currentUser.getId(), goal, start, end, amount, times)) {
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
        new vn.edu.tdmu.vita.utils.ProfileAccountActions(this, currentUser).showLogoutConfirmDialog();
    }

    private void showDeleteConfirmDialog() {
        new vn.edu.tdmu.vita.utils.ProfileAccountActions(this, currentUser).showDeleteConfirmDialog();
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
