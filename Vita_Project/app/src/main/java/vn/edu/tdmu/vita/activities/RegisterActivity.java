package vn.edu.tdmu.vita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

import vn.edu.tdmu.vita.MainActivity;
import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirm;
    private TextInputLayout tilFullname;
    private TextInputLayout tilBirthYear;
    private Spinner spGender;
    private TextInputLayout tilHeight;
    private TextInputLayout tilWeight;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirm;
    private TextInputEditText etFullname;
    private TextInputEditText etBirthYear;
    private TextInputEditText etHeight;
    private TextInputEditText etWeight;
    private MaterialButton btnSubmit;
    private TextView tvGotoLogin;

    private UserDao userDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupSpinner();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_register);
        tilUsername = findViewById(R.id.til_reg_username);
        tilPassword = findViewById(R.id.til_reg_password);
        tilConfirm = findViewById(R.id.til_reg_confirm);
        tilFullname = findViewById(R.id.til_reg_fullname);
        tilBirthYear = findViewById(R.id.til_reg_birth_year);
        spGender = findViewById(R.id.sp_reg_gender);
        tilHeight = findViewById(R.id.til_reg_height);
        tilWeight = findViewById(R.id.til_reg_weight);

        etUsername = findViewById(R.id.et_reg_username);
        etPassword = findViewById(R.id.et_reg_password);
        etConfirm = findViewById(R.id.et_reg_confirm);
        etFullname = findViewById(R.id.et_reg_fullname);
        etBirthYear = findViewById(R.id.et_reg_birth_year);
        etHeight = findViewById(R.id.et_reg_height);
        etWeight = findViewById(R.id.et_reg_weight);

        btnSubmit = findViewById(R.id.btn_reg_submit);
        tvGotoLogin = findViewById(R.id.tv_reg_goto_login);
    }

    private void setupSpinner() {
        String[] genders = new String[]{"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spGender.setAdapter(adapter);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        etUsername.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilUsername.setError(null);
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
            }
        });

        etConfirm.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilConfirm.setError(null);
            }
        });

        etBirthYear.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilBirthYear.setError(null);
            }
        });

        etHeight.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilHeight.setError(null);
            }
        });

        etWeight.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilWeight.setError(null);
            }
        });

        btnSubmit.setOnClickListener(v -> handleRegister());

        tvGotoLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirm = etConfirm.getText() != null ? etConfirm.getText().toString().trim() : "";
        String fullname = etFullname.getText() != null ? etFullname.getText().toString().trim() : "";
        String birthYearStr = etBirthYear.getText() != null ? etBirthYear.getText().toString().trim() : "";
        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String gender = spGender.getSelectedItem() != null ? spGender.getSelectedItem().toString() : "Nam";

        boolean hasError = false;

        // Validate username
        if (username.isEmpty()) {
            tilUsername.setError("Vui lòng nhập tên đăng nhập");
            hasError = true;
        } else if (username.length() < 3) {
            tilUsername.setError("Tên đăng nhập tối thiểu 3 ký tự");
            hasError = true;
        } else if (username.contains(" ")) {
            tilUsername.setError("Tên đăng nhập không được chứa khoảng trắng");
            hasError = true;
        }

        // Validate password
        if (password.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            hasError = true;
        } else if (!vn.edu.tdmu.vita.utils.InputValidator.isValidPassword(password)) {
            tilPassword.setError("Mật khẩu phải có tối thiểu 6 ký tự");
            hasError = true;
        }

        // Validate confirm password
        if (confirm.isEmpty()) {
            tilConfirm.setError("Vui lòng xác nhận lại mật khẩu");
            hasError = true;
        } else if (!password.equals(confirm)) {
            tilConfirm.setError("Mật khẩu xác nhận không trùng khớp");
            hasError = true;
        }

        // Validate height
        float height = 0f;
        if (heightStr.isEmpty()) {
            tilHeight.setError("Vui lòng nhập chiều cao");
            hasError = true;
        } else {
            try {
                height = Float.parseFloat(heightStr);
                if (!vn.edu.tdmu.vita.utils.InputValidator.isValidHeight(height)) {
                    tilHeight.setError(String.format("Chiều cao hợp lệ từ %.0fcm đến %.0fcm",
                            vn.edu.tdmu.vita.utils.InputValidator.MIN_HEIGHT_CM, vn.edu.tdmu.vita.utils.InputValidator.MAX_HEIGHT_CM));
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilHeight.setError("Chiều cao không hợp lệ");
                hasError = true;
            }
        }

        // Validate weight
        float weight = 0f;
        if (weightStr.isEmpty()) {
            tilWeight.setError("Vui lòng nhập cân nặng");
            hasError = true;
        } else {
            try {
                weight = Float.parseFloat(weightStr);
                if (!vn.edu.tdmu.vita.utils.InputValidator.isValidWeight(weight)) {
                    tilWeight.setError(String.format("Cân nặng hợp lệ từ %.0fkg đến %.0fkg",
                            vn.edu.tdmu.vita.utils.InputValidator.MIN_WEIGHT_KG, vn.edu.tdmu.vita.utils.InputValidator.MAX_WEIGHT_KG));
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilWeight.setError("Cân nặng không hợp lệ");
                hasError = true;
            }
        }

        // Validate birth year if provided
        int birthYear = 0;
        if (!birthYearStr.isEmpty()) {
            try {
                birthYear = Integer.parseInt(birthYearStr);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (!vn.edu.tdmu.vita.utils.InputValidator.isValidBirthYear(birthYear)) {
                    tilBirthYear.setError("Năm sinh từ 1900 đến " + currentYear);
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilBirthYear.setError("Năm sinh không hợp lệ");
                hasError = true;
            }
        }

        if (hasError) {
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setFullname(fullname);
        newUser.setBirthYear(birthYear > 0 ? birthYear : 2000);
        newUser.setGender(gender);
        newUser.setHeightCm(height);
        newUser.setBaseWeightKg(weight);
        newUser.setWaterGoalMl(2000);
        newUser.setBmiStandard("ASIAN");

        long userId = userDao.register(newUser, password);
        if (userId == -1) {
            tilUsername.setError("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác");
            return;
        }

        // Save session and enter main app
        sessionManager.saveSession(userId, username, fullname);
        Toast.makeText(this, "Đăng ký thành công! Chào mừng bạn đến với Vita.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
