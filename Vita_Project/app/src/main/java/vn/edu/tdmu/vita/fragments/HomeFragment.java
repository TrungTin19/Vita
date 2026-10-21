package vn.edu.tdmu.vita.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.HealthDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.BMICalculator;
import vn.edu.tdmu.vita.utils.SessionManager;

public class HomeFragment extends Fragment {
    private TextView tvGreeting;
    private TextView tvBmiValue;
    private TextView tvBmiBadge;
    private TextView tvBmiInfo;
    private TextView tvBmiStandard;
    private UserDao userDao;
    private HealthDao healthDao;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        userDao = new UserDao(requireContext());
        healthDao = new HealthDao(requireContext());
        tvGreeting = view.findViewById(R.id.tv_home_greeting);
        tvBmiValue = view.findViewById(R.id.tv_home_bmi_value);
        tvBmiBadge = view.findViewById(R.id.tv_home_bmi_badge);
        tvBmiInfo = view.findViewById(R.id.tv_home_bmi_info);
        tvBmiStandard = view.findViewById(R.id.tv_home_bmi_standard);
        if (getResources().getConfiguration().fontScale > 1.3f) {
            for (int firstId : new int[]{R.id.tv_home_water_progress, R.id.btn_home_add_water}) {
                android.widget.LinearLayout row = (android.widget.LinearLayout) view.findViewById(firstId).getParent();
                row.setOrientation(android.widget.LinearLayout.VERTICAL);
                for (int i = 0; i < row.getChildCount(); i++) {
                    View child = row.getChildAt(i);
                    android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) child.getLayoutParams();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.weight = 0;
                    params.setMarginEnd(0);
                    if (i > 0) params.topMargin = getResources().getDimensionPixelSize(R.dimen.spacing_sm);
                    child.setLayoutParams(params);
                }
            }
        }
        view.findViewById(R.id.btn_home_add_health).setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), vn.edu.tdmu.vita.activities.AddHealthActivity.class)));
        view.findViewById(R.id.btn_home_reminders).setOnClickListener(v ->
                ((com.google.android.material.bottomnavigation.BottomNavigationView) requireActivity().findViewById(R.id.bottom_nav_main)).setSelectedItemId(R.id.nav_reminder));
        view.findViewById(R.id.btn_home_add_water).setOnClickListener(v ->
                vn.edu.tdmu.vita.utils.WaterJournalDialog.addQuickWater(requireActivity(), sessionManager.getUserId(), this::refreshDashboard));
        view.findViewById(R.id.btn_home_custom_water).setOnClickListener(v ->
                vn.edu.tdmu.vita.utils.WaterJournalDialog.showAddAmount(requireActivity(), sessionManager.getUserId(), this::refreshDashboard));
        view.findViewById(R.id.btn_home_water_journal).setOnClickListener(v ->
                vn.edu.tdmu.vita.utils.WaterJournalDialog.show(requireActivity(), sessionManager.getUserId(), vn.edu.tdmu.vita.utils.DateTimeUtils.getTodayDate(), this::refreshDashboard));
        view.findViewById(R.id.cv_home_bmi).setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), vn.edu.tdmu.vita.activities.BmiActivity.class)));
        loadBmiCard();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBmiCard();
    }

    public void refreshDashboard() { if (getView() != null) loadBmiCard(); }

    private void loadBmiCard() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) return;
        User user = userDao.getUserById(userId);
        if (user == null) return;
        loadSummary(user);

        String name = user.getFullname() != null && !user.getFullname().trim().isEmpty()
                ? user.getFullname() : user.getUsername();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greetRes = hour < 11 ? R.string.home_greeting_morning
                : (hour < 18 ? R.string.home_greeting_afternoon : R.string.home_greeting_evening);
        tvGreeting.setText(getString(greetRes, name));

        float height = user.getHeightCm();
        Float latestWeight = healthDao.getLatestWeight(userId);
        float weight = BMICalculator.currentWeight(latestWeight, user.getBaseWeightKg());
        boolean isInitial = latestWeight == null;

        if (height > 0 && weight > 0) {
            BMICalculator.BmiResult result = BMICalculator.calculateAndClassify(
                    weight, height, user.getBmiStandard(), user.getBirthYear(),
                    Calendar.getInstance().get(Calendar.YEAR));
            tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", result.getBmi()));
            boolean adultClassification = !result.getCategory().isEmpty();
            tvBmiBadge.setText(adultClassification ? result.getCategory() : getString(R.string.bmi_numeric_only));
            int colorRes = result.getColorRes();
            tvBmiValue.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
            tvBmiBadge.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
            tvBmiBadge.setBackgroundResource(badgeBg(colorRes));
            String suffix = isInitial ? getString(R.string.home_bmi_info_initial_suffix) : "";
            tvBmiInfo.setText(String.format(Locale.getDefault(),
                    getString(R.string.home_bmi_info_format), height, weight, suffix));
            if (!isInitial) tvBmiInfo.append("\n" + vn.edu.tdmu.vita.utils.HealthDisplay.measurementLabel(requireContext(), healthDao.getLatestWeightRecord(userId)));
            String standard = BMICalculator.STANDARD_WHO.equals(user.getBmiStandard())
                    ? getString(R.string.home_bmi_standard_who)
                    : getString(R.string.home_bmi_standard_asian);
            tvBmiStandard.setText(adultClassification
                    ? getString(R.string.home_bmi_standard_format, standard)
                            + "\n" + getString(R.string.bmi_adult_scope)
                    : getString(R.string.bmi_outside_adult_scope));
        } else {
            tvBmiValue.setText("--");
            tvBmiBadge.setText("");
            tvBmiInfo.setText(getString(R.string.home_bmi_no_data));
            tvBmiStandard.setText("");
        }
    }

    private void loadSummary(User user) {
        long owner = sessionManager.getUserId();
        vn.edu.tdmu.vita.database.MedicineDao.NextDose next = new vn.edu.tdmu.vita.database.MedicineDao(requireContext())
                .getNextUnloggedDose(owner, System.currentTimeMillis());
        ((TextView) requireView().findViewById(R.id.tv_home_next_time)).setText(next == null ? "—" : next.time);
        ((TextView) requireView().findViewById(R.id.tv_home_next_dose)).setText(next == null
                ? getString(R.string.home_no_next_dose) : next.medicine.getName());
        ((TextView) requireView().findViewById(R.id.tv_home_next_info)).setText(next == null ? ""
                : vn.edu.tdmu.vita.utils.DateTimeUtils.formatDisplayDate(next.date) + " · "
                        + (next.medicine.getDosage() == null ? "" : next.medicine.getDosage()));
        int total = new vn.edu.tdmu.vita.database.WaterDao(requireContext()).getDailyTotal(owner,
                vn.edu.tdmu.vita.utils.DateTimeUtils.getTodayDate());
        int goal = vn.edu.tdmu.vita.utils.HydrationGoal.forUserGoal(user.getWaterGoalMl());
        ((TextView) requireView().findViewById(R.id.tv_home_water_progress)).setText(String.format(Locale.getDefault(), "%,d", total));
        ((TextView) requireView().findViewById(R.id.tv_home_water_goal)).setText(getString(R.string.figma_home_goal, goal, Math.max(0, goal - total)));
        android.widget.ProgressBar progress = requireView().findViewById(R.id.pb_home_water);
        progress.setMax(goal); progress.setProgress(total);
        ((TextView) requireView().findViewById(R.id.tv_home_water_percent)).setText(getString(R.string.figma_home_percent, Math.round(100f * total / goal)));
        ((TextView) requireView().findViewById(R.id.btn_home_add_water)).setText(getString(R.string.figma_home_quick_water, user.getWaterAmount()));
        java.util.List<vn.edu.tdmu.vita.models.HealthRecord> records = healthDao.getAllByUserId(owner, null);
        vn.edu.tdmu.vita.models.HealthRecord latest = records.isEmpty() ? null : records.get(0);
        TextView summary = requireView().findViewById(R.id.tv_home_latest_record);
        StringBuilder text = new StringBuilder(getString(R.string.home_latest_record));
        if (latest == null) text.append('\n').append(getString(R.string.home_no_record));
        else {
            text.append('\n').append(vn.edu.tdmu.vita.utils.HealthDisplay.measurementLabel(requireContext(), latest));
            if (latest.getSystolic() != null || latest.getDiastolic() != null)
                text.append('\n').append(latest.getSystolic() == null ? "—" : latest.getSystolic()).append('/')
                        .append(latest.getDiastolic() == null ? "—" : latest.getDiastolic()).append(" mmHg");
            if (latest.getWeightKg() != null) text.append('\n').append(latest.getWeightKg()).append(" kg");
            if (latest.getHeartRate() != null) text.append('\n').append(latest.getHeartRate()).append(" bpm");
            if (latest.getBloodSugar() != null) text.append('\n').append(latest.getBloodSugar()).append(" mg/dL");
            if (latest.getNote() != null && !latest.getNote().trim().isEmpty()) text.append("\nGhi chú: ").append(latest.getNote());
        }
        summary.setText(text);
        View open = requireView().findViewById(R.id.btn_home_latest_record);
        open.setVisibility(latest == null ? View.GONE : View.VISIBLE);
        open.setOnClickListener(v -> startActivity(new android.content.Intent(requireContext(), vn.edu.tdmu.vita.activities.DetailHealthActivity.class)
                .putExtra(vn.edu.tdmu.vita.activities.DetailHealthActivity.EXTRA_RECORD_ID, latest.getId())));
    }

    private int badgeBg(int colorRes) {
        if (colorRes == R.color.vita_status_good) return R.drawable.bg_badge_good;
        if (colorRes == R.color.vita_status_warning) return R.drawable.bg_badge_warning;
        if (colorRes == R.color.vita_status_danger) return R.drawable.bg_badge_danger;
        return R.drawable.bg_badge_rounded;
    }
}
