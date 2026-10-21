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
import vn.edu.tdmu.vita.database.HealthDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.BMICalculator;
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
        boolean bmiScreen = this instanceof BmiActivity;
        findViewById(R.id.group_profile_summary).setVisibility(bmiScreen ? android.view.View.GONE : android.view.View.VISIBLE);
        findViewById(R.id.group_profile_bmi).setVisibility(bmiScreen ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.group_profile_account_actions).setVisibility(bmiScreen ? android.view.View.GONE : android.view.View.VISIBLE);
        if (bmiScreen) toolbar.setTitle(R.string.figma_profile_bmi_title);
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
        findViewById(R.id.btn_profile_open_bmi).setOnClickListener(v -> startActivity(new Intent(this, BmiActivity.class)));
        findViewById(R.id.btn_bmi_add_weight).setOnClickListener(v -> startActivity(new Intent(this, AddHealthActivity.class).putExtra(AddHealthActivity.EXTRA_METRIC_GROUP, 3)));
        findViewById(R.id.btn_profile_logout).setOnClickListener(v -> new vn.edu.tdmu.vita.utils.ProfileAccountActions(this, currentUser).showLogoutConfirmDialog());
        findViewById(R.id.btn_profile_delete).setOnClickListener(v -> new vn.edu.tdmu.vita.utils.ProfileAccountActions(this, currentUser).showDeleteConfirmDialog());

        rgBmiStandard.setOnCheckedChangeListener((group, checkedId) -> {
            if (isInitialCheck || currentUser == null) {
                return;
            }
            String selectedStandard = (checkedId == R.id.rb_bmi_who)
                    ? BMICalculator.STANDARD_WHO
                    : BMICalculator.STANDARD_ASIAN;
            userDao.updateBmiStandard(currentUser.getId(), selectedStandard);
            currentUser.setBmiStandard(selectedStandard);

            renderBmiCard(selectedStandard);

            String message = (checkedId == R.id.rb_bmi_who)
                    ? getString(R.string.profile_snackbar_standard_who)
                    : getString(R.string.profile_snackbar_standard_asian);
            Snackbar.make(rgBmiStandard, message, Snackbar.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_profile_water_settings).setOnClickListener(v -> startActivity(
                new Intent(this, EditProfileActivity.class).putExtra(EditProfileActivity.EXTRA_WATER_SETTINGS_ONLY, true)));
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
        tvUsername.setText("@" + currentUser.getUsername() + " · " + getString(R.string.figma_profile_on_device));

        // Birth year & age
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int age = currentYear - currentUser.getBirthYear();
        if (currentUser.getBirthYear() > 0 && age >= 0 && age <= 120) {
            tvBirthYear.setText(getString(R.string.figma_profile_age, currentUser.getBirthYear(), age));
        } else {
            tvBirthYear.setText(getString(R.string.profile_not_updated));
        }

        tvGender.setText(currentUser.getGender() != null ? currentUser.getGender() : getString(R.string.profile_unknown));
        tvHeight.setText(getString(R.string.profile_height_format, currentUser.getHeightCm()));
        tvBaseWeight.setText(getString(R.string.profile_weight_format, currentUser.getBaseWeightKg()));
        tvWaterGoal.setText(getString(R.string.profile_water_goal_format, currentUser.getWaterGoalMl()));
        ((TextView) findViewById(R.id.tv_profile_water_schedule)).setText(getString(R.string.profile_water_schedule,
                currentUser.getWaterStart(), currentUser.getWaterEnd(), currentUser.getWaterAmount()) + "\n"
                + (currentUser.getWaterTimes().isEmpty() ? getString(R.string.water_every_two_hours)
                : getString(R.string.water_custom_summary, currentUser.getWaterTimes().replace(",", " · "))));

        String standard = (currentUser.getBmiStandard() != null && !currentUser.getBmiStandard().trim().isEmpty())
                ? currentUser.getBmiStandard() : BMICalculator.STANDARD_ASIAN;

        // Render BMI Card
        renderBmiCard(standard);

        ((MaterialButton) findViewById(R.id.btn_profile_open_bmi)).setText("BMI " + tvBmiValue.getText() + " · " + tvBmiCategory.getText());

        // BMI Standard radio selection
        isInitialCheck = true;
        if (BMICalculator.STANDARD_WHO.equalsIgnoreCase(standard)) {
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

        float heightCm = currentUser.getHeightCm();
        vn.edu.tdmu.vita.models.HealthRecord latest = new HealthDao(this).getLatestWeightRecord(currentUser.getId());
        float weightKg = BMICalculator.currentWeight(latest == null ? null : latest.getWeightKg(), currentUser.getBaseWeightKg());
        String when = latest == null ? "" : vn.edu.tdmu.vita.utils.DateTimeUtils.formatDisplayDate(latest.getDate()) + " · "
                + (latest.getMeasuredTime() == null ? getString(R.string.measurement_time_unknown) : latest.getMeasuredTime());
        ((TextView) findViewById(R.id.tv_profile_latest_weight)).setText(latest == null ? "—" : getString(R.string.profile_weight_format, latest.getWeightKg()));
        ((TextView) findViewById(R.id.tv_profile_weight_source)).setText(latest == null
                ? getString(R.string.figma_bmi_source_initial, heightCm, weightKg)
                : getString(R.string.figma_bmi_source_log, heightCm, weightKg, when));

        if (heightCm > 0 && weightKg > 0) {
            BMICalculator.BmiResult result = BMICalculator.calculateAndClassify(weightKg, heightCm, standard,
                    currentUser.getBirthYear(), Calendar.getInstance().get(Calendar.YEAR));
            float bmi = result.getBmi();
            tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));

            String category = result.getCategory();
            boolean adultClassification = !category.isEmpty();
            tvBmiCategory.setText(adultClassification ? category : getString(R.string.bmi_numeric_only));
            int colorRes = result.getColorRes();
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

            tvHealthyWeight.setText(adultClassification
                    ? getString(R.string.figma_bmi_range_value,
                            result.getMinHealthyWeight(), result.getMaxHealthyWeight())
                    : getString(R.string.bmi_outside_adult_scope));
        } else {
            tvBmiValue.setText("--");
            tvBmiCategory.setText(getString(R.string.profile_bmi_no_metrics));
            tvBmiCategory.setTextColor(ContextCompat.getColor(this, R.color.vita_text_secondary));
            tvBmiCategory.setBackgroundResource(R.drawable.bg_badge_neutral);
            tvHealthyWeight.setText(getString(R.string.profile_healthy_weight_prompt));
        }
    }
}
