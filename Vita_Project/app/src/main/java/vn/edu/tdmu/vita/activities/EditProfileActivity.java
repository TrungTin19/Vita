package vn.edu.tdmu.vita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
    private Spinner spGender;
    private TextInputLayout tilHeight;
    private TextInputLayout tilWeight;
    private TextInputLayout tilWaterGoal;

    private TextInputEditText etFullname;
    private TextInputEditText etBirthYear;
    private TextInputEditText etHeight;
    private TextInputEditText etWeight;
    private TextInputEditText etWaterGoal;

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
        spGender = findViewById(R.id.sp_edit_gender);
        tilHeight = findViewById(R.id.til_edit_height);
        tilWeight = findViewById(R.id.til_edit_weight);
        tilWaterGoal = findViewById(R.id.til_edit_water_goal);

        etFullname = findViewById(R.id.et_edit_fullname);
        etBirthYear = findViewById(R.id.et_edit_birth_year);
        etHeight = findViewById(R.id.et_edit_height);
        etWeight = findViewById(R.id.et_edit_weight);
        etWaterGoal = findViewById(R.id.et_edit_water_goal);

        btnSave = findViewById(R.id.btn_edit_save);
        btnLogout = findViewById(R.id.btn_edit_logout);
    }

    private void setupSpinner() {
        String[] genders = new String[]{"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spGender.setAdapter(adapter);
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

        if ("Nữ".equalsIgnoreCase(currentUser.getGender())) {
            spGender.setSelection(1);
        } else if ("Khác".equalsIgnoreCase(currentUser.getGender())) {
            spGender.setSelection(2);
        } else {
            spGender.setSelection(0);
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

    private void handleSave() {
        String fullname = etFullname.getText() != null ? etFullname.getText().toString().trim() : "";
        String birthYearStr = etBirthYear.getText() != null ? etBirthYear.getText().toString().trim() : "";
        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String waterGoalStr = etWaterGoal.getText() != null ? etWaterGoal.getText().toString().trim() : "";
        String gender = spGender.getSelectedItem() != null ? spGender.getSelectedItem().toString() : "Nam";

        boolean hasError = false;

        // Height validation
        float height = 0f;
        if (heightStr.isEmpty()) {
            tilHeight.setError("Vui lòng nhập chiều cao");
            hasError = true;
        } else {
            try {
                height = Float.parseFloat(heightStr);
                if (!InputValidator.isValidHeight(height)) {
                    tilHeight.setError(String.format("Chiều cao hợp lệ từ %.0fcm đến %.0fcm",
                            InputValidator.MIN_HEIGHT_CM, InputValidator.MAX_HEIGHT_CM));
                    hasError = true;
                } else {
                    tilHeight.setError(null);
                }
            } catch (NumberFormatException e) {
                tilHeight.setError("Chiều cao không hợp lệ");
                hasError = true;
            }
        }

        // Weight validation
        float weight = 0f;
        if (weightStr.isEmpty()) {
            tilWeight.setError("Vui lòng nhập cân nặng");
            hasError = true;
        } else {
            try {
                weight = Float.parseFloat(weightStr);
                if (!InputValidator.isValidWeight(weight)) {
                    tilWeight.setError(String.format("Cân nặng hợp lệ từ %.0fkg đến %.0fkg",
                            InputValidator.MIN_WEIGHT_KG, InputValidator.MAX_WEIGHT_KG));
                    hasError = true;
                } else {
                    tilWeight.setError(null);
                }
            } catch (NumberFormatException e) {
                tilWeight.setError("Cân nặng không hợp lệ");
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
                    tilBirthYear.setError("Năm sinh từ 1900 đến " + currentYear);
                    hasError = true;
                } else {
                    tilBirthYear.setError(null);
                }
            } catch (NumberFormatException e) {
                tilBirthYear.setError("Năm sinh không hợp lệ");
                hasError = true;
            }
        }

        // Water goal validation
        int waterGoal = 2000;
        if (!waterGoalStr.isEmpty()) {
            try {
                waterGoal = Integer.parseInt(waterGoalStr);
                if (!InputValidator.isValidWaterGoal(waterGoal)) {
                    tilWaterGoal.setError(String.format("Mục tiêu nước từ %,d đến %,d ml / ngày",
                            InputValidator.MIN_WATER_GOAL_ML, InputValidator.MAX_WATER_GOAL_ML));
                    hasError = true;
                } else {
                    tilWaterGoal.setError(null);
                }
            } catch (NumberFormatException e) {
                tilWaterGoal.setError("Mục tiêu nước không hợp lệ");
                hasError = true;
            }
        }

        if (hasError) {
            return;
        }

        currentUser.setFullname(fullname);
        currentUser.setGender(gender);
        currentUser.setBirthYear(birthYear);
        currentUser.setHeightCm(height);
        currentUser.setBaseWeightKg(weight);
        currentUser.setWaterGoalMl(waterGoal);

        boolean success = userDao.updateProfile(currentUser);
        if (success) {
            sessionManager.saveSession(currentUser.getId(), currentUser.getUsername(), currentUser.getFullname());
            Toast.makeText(this, "Đã lưu thay đổi hồ sơ thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Không thể cập nhật hồ sơ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng Vita không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    sessionManager.clearSession();
                    Toast.makeText(EditProfileActivity.this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy bỏ", (dialog, which) -> dialog.dismiss())
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
