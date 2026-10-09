package vn.edu.tdmu.vita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvFullname;
    private TextView tvUsername;
    private TextView tvBirthYear;
    private TextView tvGender;
    private TextView tvHeight;
    private TextView tvBaseWeight;
    private TextView tvWaterGoal;
    private RadioGroup rgBmiStandard;
    private RadioButton rbAsian;
    private RadioButton rbWho;
    private MaterialButton btnEdit;

    private UserDao userDao;
    private SessionManager sessionManager;
    private User currentUser;
    private boolean isInitialCheck = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_profile);
        tvFullname = findViewById(R.id.tv_profile_fullname);
        tvUsername = findViewById(R.id.tv_profile_username);
        tvBirthYear = findViewById(R.id.tv_profile_birth_year);
        tvGender = findViewById(R.id.tv_profile_gender);
        tvHeight = findViewById(R.id.tv_profile_height);
        tvBaseWeight = findViewById(R.id.tv_profile_base_weight);
        tvWaterGoal = findViewById(R.id.tv_profile_water_goal);
        rgBmiStandard = findViewById(R.id.rg_bmi_standard);
        rbAsian = findViewById(R.id.rb_bmi_asian);
        rbWho = findViewById(R.id.rb_bmi_who);
        btnEdit = findViewById(R.id.btn_profile_edit);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        rgBmiStandard.setOnCheckedChangeListener((group, checkedId) -> {
            if (isInitialCheck || currentUser == null) {
                return;
            }
            String selectedStandard = (checkedId == R.id.rb_bmi_who) ? "WHO" : "ASIAN";
            userDao.updateBmiStandard(currentUser.getId(), selectedStandard);
            currentUser.setBmiStandard(selectedStandard);

            String message = (checkedId == R.id.rb_bmi_who)
                    ? "Đã đổi sang Chuẩn Quốc tế (WHO)"
                    : "Đã đổi sang Chuẩn Châu Á (IDI & WPRO)";
            Snackbar.make(rgBmiStandard, message, Snackbar.LENGTH_SHORT).show();
        });

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            finish();
            return;
        }

        currentUser = userDao.getUserById(userId);
        if (currentUser == null) {
            return;
        }

        String displayName = (currentUser.getFullname() != null && !currentUser.getFullname().trim().isEmpty())
                ? currentUser.getFullname() : currentUser.getUsername();
        tvFullname.setText(displayName);
        tvUsername.setText("@" + currentUser.getUsername());

        // Birth year & age
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int age = currentYear - currentUser.getBirthYear();
        if (currentUser.getBirthYear() > 0 && age >= 0 && age <= 120) {
            tvBirthYear.setText(String.format(Locale.getDefault(), "• Năm sinh: %d (%d tuổi)", currentUser.getBirthYear(), age));
        } else {
            tvBirthYear.setText("• Năm sinh: Chưa cập nhật");
        }

        tvGender.setText("• Giới tính: " + (currentUser.getGender() != null ? currentUser.getGender() : "Chưa rõ"));
        tvHeight.setText(String.format(Locale.getDefault(), "• Chiều cao: %.1f cm", currentUser.getHeightCm()));
        tvBaseWeight.setText(String.format(Locale.getDefault(), "• Cân nặng ban đầu: %.1f kg", currentUser.getBaseWeightKg()));
        tvWaterGoal.setText(String.format(Locale.getDefault(), "• Mục tiêu nước: %,d ml/ngày", currentUser.getWaterGoalMl()));

        // BMI Standard radio selection
        isInitialCheck = true;
        if ("WHO".equalsIgnoreCase(currentUser.getBmiStandard())) {
            rbWho.setChecked(true);
        } else {
            rbAsian.setChecked(true);
        }
        isInitialCheck = false;
    }
}
