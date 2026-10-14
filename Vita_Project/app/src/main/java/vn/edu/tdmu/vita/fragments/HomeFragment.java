package vn.edu.tdmu.vita.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import vn.edu.tdmu.vita.models.WeatherData;
import vn.edu.tdmu.vita.network.WeatherClient;
import vn.edu.tdmu.vita.utils.BMICalculator;
import vn.edu.tdmu.vita.utils.SessionManager;

public class HomeFragment extends Fragment {

    private static final String PREFS_HOME = "vita_home";
    private static final String KEY_CITY = "city_position";

    private static final String[] CITY_NAMES = {
            "TP. Hồ Chí Minh", "Bình Dương", "Hà Nội", "Đà Nẵng", "Cần Thơ"};
    private static final double[] CITY_LAT = {10.82, 11.16, 21.02, 16.05, 10.04};
    private static final double[] CITY_LON = {106.63, 106.65, 105.83, 108.20, 105.78};

    private TextView tvGreeting;
    private TextView tvBmiValue;
    private TextView tvBmiBadge;
    private TextView tvBmiInfo;
    private TextView tvBmiStandard;
    private Spinner spCity;
    private TextView tvTemp;
    private TextView tvHumidity;
    private TextView tvAqi;
    private TextView tvAqiLabel;
    private TextView tvUpdated;
    private TextView tvOffline;
    private TextView tvAdvice;

    private UserDao userDao;
    private HealthDao healthDao;
    private SessionManager sessionManager;

    private boolean spinnerInit = true;

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
        spCity = view.findViewById(R.id.sp_home_city);
        tvTemp = view.findViewById(R.id.tv_home_temp);
        tvHumidity = view.findViewById(R.id.tv_home_humidity);
        tvAqi = view.findViewById(R.id.tv_home_aqi);
        tvAqiLabel = view.findViewById(R.id.tv_home_aqi_label);
        tvUpdated = view.findViewById(R.id.tv_home_updated);
        tvOffline = view.findViewById(R.id.tv_home_offline);
        tvAdvice = view.findViewById(R.id.tv_home_advice_text);

        setupCitySpinner();
        loadBmiCard();
        fetchWeather(currentCity());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBmiCard();
        fetchWeather(currentCity());
    }

    private void setupCitySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, CITY_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCity.setAdapter(adapter);
        spCity.setSelection(currentCity());
        spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (spinnerInit) {
                    spinnerInit = false;
                    return;
                }
                saveCity(position);
                fetchWeather(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int currentCity() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_HOME, 0);
        int pos = prefs.getInt(KEY_CITY, 0);
        return (pos >= 0 && pos < CITY_NAMES.length) ? pos : 0;
    }

    private void saveCity(int position) {
        requireContext().getSharedPreferences(PREFS_HOME, 0).edit()
                .putInt(KEY_CITY, position).apply();
    }

    private void loadBmiCard() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) return;
        User user = userDao.getUserById(userId);
        if (user == null) return;

        String name = user.getFullname() != null && !user.getFullname().trim().isEmpty()
                ? user.getFullname() : user.getUsername();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greetRes = hour < 11 ? R.string.home_greeting_morning
                : (hour < 18 ? R.string.home_greeting_afternoon : R.string.home_greeting_evening);
        tvGreeting.setText(getString(greetRes, name));

        float height = user.getHeightCm();
        Float latestWeight = healthDao.getLatestWeight(userId);
        float weight = (latestWeight != null) ? latestWeight : user.getBaseWeightKg();
        boolean isInitial = latestWeight == null;

        if (height > 0 && weight > 0) {
            BMICalculator.BmiResult result = BMICalculator.calculateAndClassify(
                    weight, height, user.getBmiStandard());
            tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", result.getBmi()));
            tvBmiBadge.setText(result.getCategory());
            int colorRes = result.getColorRes();
            tvBmiValue.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
            tvBmiBadge.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
            tvBmiBadge.setBackgroundResource(badgeBg(colorRes));
            String suffix = isInitial ? getString(R.string.home_bmi_info_initial_suffix) : "";
            tvBmiInfo.setText(String.format(Locale.getDefault(),
                    getString(R.string.home_bmi_info_format), height, weight, suffix));
            String standard = BMICalculator.STANDARD_WHO.equals(user.getBmiStandard())
                    ? getString(R.string.home_bmi_standard_who)
                    : getString(R.string.home_bmi_standard_asian);
            tvBmiStandard.setText(String.format(Locale.getDefault(),
                    getString(R.string.home_bmi_standard_format), standard));
        } else {
            tvBmiValue.setText("--");
            tvBmiBadge.setText("");
            tvBmiInfo.setText(getString(R.string.home_bmi_no_data));
        }
    }

    private int badgeBg(int colorRes) {
        if (colorRes == R.color.vita_status_good) return R.drawable.bg_badge_good;
        if (colorRes == R.color.vita_status_warning) return R.drawable.bg_badge_warning;
        if (colorRes == R.color.vita_status_danger) return R.drawable.bg_badge_danger;
        return R.drawable.bg_badge_rounded;
    }

    private void fetchWeather(int cityPos) {
        WeatherClient.fetchWeather(requireContext(), CITY_LAT[cityPos], CITY_LON[cityPos],
                CITY_NAMES[cityPos], new WeatherClient.Callback() {
                    @Override
                    public void onSuccess(WeatherData data) {
                        if (!isAdded()) return;
                        renderWeather(data);
                    }

                    @Override
                    public void onError() {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), getString(R.string.home_weather_error),
                                Toast.LENGTH_SHORT).show();
                        tvAdvice.setText(getString(R.string.home_advice_good));
                    }
                });
    }

    private void renderWeather(WeatherData data) {
        tvTemp.setText(String.format(Locale.US, getString(R.string.home_temp_format),
                data.getTemperature()));
        tvHumidity.setText(String.format(Locale.getDefault(),
                getString(R.string.home_humidity_format), data.getHumidity()));
        tvUpdated.setText(String.format(Locale.getDefault(),
                getString(R.string.home_updated_format), shortTime(data.getUpdatedAt())));
        tvOffline.setVisibility(data.isOffline() ? View.VISIBLE : View.GONE);

        int aqi = data.getAqi();
        if (aqi < 0) {
            tvAqi.setText("--");
            tvAqiLabel.setText(getString(R.string.home_aqi_unknown));
            tvAqi.setTextColor(ContextCompat.getColor(requireContext(), R.color.vita_text_secondary));
        } else {
            tvAqi.setText(String.format(Locale.getDefault(),
                    getString(R.string.home_aqi_format), aqi));
            int colorRes;
            int labelRes;
            if (aqi <= 50) {
                colorRes = R.color.vita_status_good;
                labelRes = R.string.home_aqi_good;
            } else if (aqi <= 100) {
                colorRes = R.color.vita_aqi_moderate;
                labelRes = R.string.home_aqi_moderate;
            } else if (aqi <= 150) {
                colorRes = R.color.vita_status_warning;
                labelRes = R.string.home_aqi_sensitive;
            } else {
                colorRes = R.color.vita_status_danger;
                labelRes = R.string.home_aqi_bad;
            }
            tvAqi.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
            tvAqiLabel.setText(getString(labelRes));
        }

        if (data.getTemperature() >= 33) {
            tvAdvice.setText(String.format(Locale.getDefault(),
                    getString(R.string.home_advice_heat), data.getTemperature()));
        } else if (aqi > 100) {
            tvAdvice.setText(String.format(Locale.getDefault(),
                    getString(R.string.home_advice_air), aqi));
        } else {
            tvAdvice.setText(getString(R.string.home_advice_good));
        }
    }

    private String shortTime(String timestamp) {
        if (timestamp != null && timestamp.length() >= 16) {
            return timestamp.substring(11, 16);
        }
        return timestamp == null ? "" : timestamp;
    }
}
