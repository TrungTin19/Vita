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
                    ? getString(R.string.profile_snackbar_standard_who)
                    : getString(R.string.profile_snackbar_standard_asian);
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
            tvBirthYear.setText(getString(R.string.profile_age_format, currentUser.getBirthYear(), age));
        } else {
            tvBirthYear.setText(getString(R.string.profile_not_updated));
        }

        tvGender.setText(currentUser.getGender() != null ? currentUser.getGender() : getString(R.string.profile_unknown));
        tvHeight.setText(getString(R.string.profile_height_format, currentUser.getHeightCm()));
        tvBaseWeight.setText(getString(R.string.profile_weight_format, currentUser.getBaseWeightKg()));
        tvWaterGoal.setText(getString(R.string.profile_water_goal_format, currentUser.getWaterGoalMl()));

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

            if (colorRes == R.color.vita_status_good) {
                tvBmiCategory.setBackgroundResource(R.drawable.bg_badge_good);
            } else if (colorRes == R.color.vita_status_warning) {
                tvBmiCategory.setBackgroundResource(R.drawable.bg_badge_warning);
            } else if (colorRes == R.color.vita_status_danger) {
                tvBmiCategory.setBackgroundResource(R.drawable.bg_badge_danger);
            } else {
                tvBmiCategory.setBackgroundResource(R.drawable.bg_badge_neutral);
            }

            double[] healthyRange = BmiUtils.getHealthyWeightRange(heightCm, standard);
            tvHealthyWeight.setText(getString(R.string.profile_healthy_weight_format, healthyRange[0], healthyRange[1]));
        } else {
            tvBmiValue.setText("--");
            tvBmiCategory.setText(getString(R.string.profile_bmi_no_metrics));
            tvBmiCategory.setTextColor(ContextCompat.getColor(this, R.color.vita_text_secondary));
            tvBmiCategory.setBackgroundResource(R.drawable.bg_badge_neutral);
            tvHealthyWeight.setText(getString(R.string.profile_healthy_weight_prompt));
        }
    }
}

