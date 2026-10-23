package vn.edu.tdmu.vita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

import vn.edu.tdmu.vita.activities.MainActivity;
import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private boolean profileStep;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirm;
    private TextInputLayout tilFullname;
    private TextInputLayout tilBirthYear;
    private AutoCompleteTextView actGender;
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
        showStep(savedInstanceState != null && savedInstanceState.getBoolean("register_profile_step"));
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { navigateBack(); }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_register);
        tilUsername = findViewById(R.id.til_reg_username);
        tilPassword = findViewById(R.id.til_reg_password);
        tilConfirm = findViewById(R.id.til_reg_confirm);
        tilFullname = findViewById(R.id.til_reg_fullname);
        tilBirthYear = findViewById(R.id.til_reg_birth_year);
        actGender = findViewById(R.id.act_reg_gender);
        tilHeight = findViewById(R.id.til_reg_height);
        tilWeight = findViewById(R.id.til_reg_weight);

        etUsername = findViewById(R.id.et_reg_username);
        etPassword = findViewById(R.id.et_reg_password);
        etConfirm = findViewById(R.id.et_reg_confirm);
        etFullname = findViewById(R.id.et_reg_fullname);
        etBirthYear = findViewById(R.id.et_reg_birth_year);
        etHeight = findViewById(R.id.et_reg_height);
        etWeight = findViewById(R.id.et_reg_weight);

        if (getResources().getConfiguration().fontScale > 1.3f) {
            android.widget.LinearLayout row = (android.widget.LinearLayout) tilHeight.getParent();
            row.setOrientation(android.widget.LinearLayout.VERTICAL);
            for (int index = 0; index < row.getChildCount(); index++) {
                android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) row.getChildAt(index).getLayoutParams();
                params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                params.weight = 0;
                params.setMarginStart(0); params.setMarginEnd(0);
            }
        }
        btnSubmit = findViewById(R.id.btn_reg_submit);
        tvGotoLogin = findViewById(R.id.tv_reg_goto_login);
    }

    private void setupSpinner() {
        String[] genders = new String[]{"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        actGender.setAdapter(adapter);
        actGender.setText("Nam", false);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> navigateBack());

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
        String gender = actGender.getText() != null && !actGender.getText().toString().trim().isEmpty()
                ? actGender.getText().toString().trim() : "Nam";

        boolean hasError = false;

        // Kiểm tra tên đăng nhập
        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.register_error_empty_username));
            hasError = true;
        } else if (username.length() < 3) {
            tilUsername.setError(getString(R.string.register_error_short_username));
            hasError = true;
        } else if (username.contains(" ")) {
            tilUsername.setError(getString(R.string.register_error_space_username));
            hasError = true;
        }

        // Kiểm tra mật khẩu
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.register_error_empty_password));
            hasError = true;
        } else if (!vn.edu.tdmu.vita.utils.InputValidator.isValidPassword(password)) {
            tilPassword.setError(getString(R.string.register_error_short_password));
            hasError = true;
        }

        // Kiểm tra mật khẩu xác nhận
        if (confirm.isEmpty()) {
            tilConfirm.setError(getString(R.string.register_error_empty_confirm));
            hasError = true;
        } else if (!password.equals(confirm)) {
            tilConfirm.setError(getString(R.string.register_error_mismatch_confirm));
            hasError = true;
        }

        if (!profileStep) {
            if (!hasError) showStep(true);
            return;
        }

        // Kiểm tra chiều cao
        float height = 0f;
        if (heightStr.isEmpty()) {
            tilHeight.setError(getString(R.string.register_error_empty_height));
            hasError = true;
        } else {
            try {
                height = Float.parseFloat(heightStr);
                if (!vn.edu.tdmu.vita.utils.InputValidator.isValidHeight(height)) {
                    tilHeight.setError(getString(R.string.register_error_invalid_height,
                            vn.edu.tdmu.vita.utils.InputValidator.MIN_HEIGHT_CM, vn.edu.tdmu.vita.utils.InputValidator.MAX_HEIGHT_CM));
                    hasError = true;
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
                if (!vn.edu.tdmu.vita.utils.InputValidator.isValidWeight(weight)) {
                    tilWeight.setError(getString(R.string.register_error_invalid_weight,
                            vn.edu.tdmu.vita.utils.InputValidator.MIN_WEIGHT_KG, vn.edu.tdmu.vita.utils.InputValidator.MAX_WEIGHT_KG));
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilWeight.setError(getString(R.string.register_error_nan_weight));
                hasError = true;
            }
        }

        // Kiểm tra năm sinh nếu người dùng đã nhập
        int birthYear = 0;
        if (!birthYearStr.isEmpty()) {
            try {
                birthYear = Integer.parseInt(birthYearStr);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (!vn.edu.tdmu.vita.utils.InputValidator.isValidBirthYear(birthYear)) {
                    tilBirthYear.setError(getString(R.string.register_error_invalid_birth_year, currentYear));
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilBirthYear.setError(getString(R.string.register_error_nan_birth_year));
                hasError = true;
            }
        }

        if (hasError) {
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setFullname(fullname);
        newUser.setBirthYear(birthYear);
        newUser.setGender(gender);
        newUser.setHeightCm(height);
        newUser.setBaseWeightKg(weight);
        newUser.setWaterGoalMl(2000);
        newUser.setBmiStandard("ASIAN");

        long userId = userDao.register(newUser, password);
        if (userId == -1) {
            showStep(false);
            tilUsername.setError(getString(R.string.register_error_username_exists));
            return;
        }

        // Lưu phiên đăng nhập và vào ứng dụng
        sessionManager.saveSession(userId, username, fullname);
        Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showStep(boolean profile) {
        profileStep = profile;
        findViewById(R.id.group_register_account).setVisibility(profile ? android.view.View.GONE : android.view.View.VISIBLE);
        findViewById(R.id.group_register_profile).setVisibility(profile ? android.view.View.VISIBLE : android.view.View.GONE);
        ((TextView) findViewById(R.id.tv_register_step)).setText(profile ? R.string.figma_register_step_two : R.string.figma_register_step_one);
        ((TextView) findViewById(R.id.tv_register_heading)).setText(profile ? R.string.figma_register_background_title : R.string.figma_register_title);
        ((TextView) findViewById(R.id.title_register)).setText(profile ? R.string.figma_register_background : R.string.register_toolbar_title);
        btnSubmit.setText(profile ? R.string.figma_register_finish : R.string.figma_register_next);
        findViewById(R.id.tv_register_note).setVisibility(profile ? android.view.View.GONE : android.view.View.VISIBLE);
        findViewById(R.id.tv_reg_goto_login).setVisibility(android.view.View.GONE);
        ((android.widget.ScrollView) findViewById(R.id.scroll_register)).scrollTo(0, 0);
    }

    private void navigateBack() {
        if (profileStep) showStep(false); else finish();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean("register_profile_step", profileStep);
        super.onSaveInstanceState(state);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
