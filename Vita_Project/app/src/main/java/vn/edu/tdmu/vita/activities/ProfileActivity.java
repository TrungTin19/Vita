package vn.edu.tdmu.vita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.BmiUtils;
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
    private TextView tvBmiValue;
    private TextView tvBmiCategory;
    private TextView tvHealthyWeight;
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
        tvBmiValue = findViewById(R.id.tv_profile_bmi_value);
        tvBmiCategory = findViewById(R.id.tv_profile_bmi_category);
        tvHealthyWeight = findViewById(R.id.tv_profile_healthy_weight);
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
            String selectedStandard = (checkedId == R.id.rb_bmi_who)
                    ? BmiUtils.STANDARD_WHO
                    : BmiUtils.STANDARD_ASIAN;
            userDao.updateBmiStandard(currentUser.getId(), selectedStandard);
            currentUser.setBmiStandard(selectedStandard);

            renderBmiCard(selectedStandard);

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
            tvBirthYear.setText(String.format(Locale.getDefault(), "%d (%d tuổi)", currentUser.getBirthYear(), age));
        } else {
            tvBirthYear.setText("Chưa cập nhật");
        }

        tvGender.setText(currentUser.getGender() != null ? currentUser.getGender() : "Chưa rõ");
        tvHeight.setText(String.format(Locale.getDefault(), "%.1f cm", currentUser.getHeightCm()));
        tvBaseWeight.setText(String.format(Locale.getDefault(), "%.1f kg", currentUser.getBaseWeightKg()));
        tvWaterGoal.setText(String.format(Locale.getDefault(), "%,d ml / ngày", currentUser.getWaterGoalMl()));

        String standard = (currentUser.getBmiStandard() != null && !currentUser.getBmiStandard().trim().isEmpty())
                ? currentUser.getBmiStandard() : BmiUtils.STANDARD_ASIAN;

        // Render BMI Card
        renderBmiCard(standard);

        // BMI Standard radio selection
        isInitialCheck = true;
        if (BmiUtils.STANDARD_WHO.equalsIgnoreCase(standard)) {
            rbWho.setChecked(true);
        } else {
            rbAsian.setChecked(true);
        }
        isInitialCheck = false;
    }

    private void renderBmiCard(String standard) {
        if (currentUser == null) {
            return;
        }

        double heightCm = currentUser.getHeightCm();
        double weightKg = currentUser.getBaseWeightKg();

        if (heightCm > 0 && weightKg > 0) {
            double bmi = BmiUtils.calculateBmi(heightCm, weightKg);
            tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));

            String category = BmiUtils.classifyBmi(bmi, standard);
            tvBmiCategory.setText(category);
            int colorRes = BmiUtils.getStatusColorRes(bmi, standard);
            tvBmiCategory.setTextColor(ContextCompat.getColor(this, colorRes));

            double[] healthyRange = BmiUtils.getHealthyWeightRange(heightCm, standard);
            tvHealthyWeight.setText(String.format(Locale.getDefault(),
                    "Khoảng cân nặng lý tưởng nên duy trì: %.1f kg – %.1f kg", healthyRange[0], healthyRange[1]));
        } else {
            tvBmiValue.setText("--");
            tvBmiCategory.setText("Chưa có số đo");
            tvBmiCategory.setTextColor(ContextCompat.getColor(this, R.color.vita_text_secondary));
            tvHealthyWeight.setText("Vui lòng cập nhật chiều cao và cân nặng để tính BMI.");
        }
    }
}

